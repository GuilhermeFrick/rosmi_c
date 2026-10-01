/*!
 * \file      TestRosmiTask.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with RosmiTask unity test functions
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "uTest.h"
#include "RosmiTask.h"
#include "TestRosmi.h"
#include "TestRosmiAlloc.h"
#include "TestRosmiFixture.h"

/*! \addtogroup  TestRosmiTaskPrivate ROSMI Task Test Private
 *  \ingroup TestRosmi
 * @{
 */
static void TestTaskEveryThreadCountGivesSameResult(void);
static void TestTaskCoversEverySegment(void);
static void TestTaskInvalid(void);
static void TestTaskNoMemory(void);
static bool TestTaskRun(const RosmiImage_t *image, int32_t seg_rows, int32_t seg_cols, int32_t threads,
                        RosmiLabel_t *total);
/*! @}*/ // End of TestRosmiTaskPrivate

/*!
 * \brief     Function to test all RosmiTask functions
 */
void TestRosmiTask(void)
{
    SetUp();

    TestTaskEveryThreadCountGivesSameResult();
    TestTaskCoversEverySegment();
    TestTaskInvalid();
    TestTaskNoMemory();

    TearDown();
}
/*!
 *  \brief  Tests that the thread count never changes the labeling result
 *  \details The sum of the local components is a deterministic property of
 *           the image and the segmentation. If it moved with the number of
 *           threads, the static split would be dropping or duplicating
 *           segments.
 */
static void TestTaskEveryThreadCountGivesSameResult(void)
{
    static const char *const rows[] = {"##..##..", ".#...#..", ".##..#..", "....#...",
                                       "#...#...", "#...#...", "#####...", "........"};
    RosmiImage_t             image  = {0};
    RosmiLabel_t             single = 0;

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, rows, 8));
    EXPECT_TRUE(TestTaskRun(&image, 4, 4, 1, &single));

    for (int32_t threads = 2; threads <= 32; threads *= 2)
    {
        RosmiLabel_t other = 0;

        EXPECT_TRUE(TestTaskRun(&image, 4, 4, threads, &other));
        EXPECT_EQ(single, other);
    }

    TestImageDestroy(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that the interleaved split leaves no segment behind
 *  \details Every segment of this image holds exactly one object, so the sum
 *           of the local counts has to equal the segment count. A segment
 *           skipped by the split would show up as a smaller sum.
 */
static void TestTaskCoversEverySegment(void)
{
    static const char *const rows[] = {"#.#.#.", "......", "#.#.#.", "......", "#.#.#.", "......"};
    RosmiImage_t             image  = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, rows, 6));

    /* 3 x 3 segments of 2 x 2, one object in the top left corner of each */
    for (int32_t threads = 1; threads <= 9; threads++)
    {
        RosmiLabel_t total = 0;

        EXPECT_TRUE(TestTaskRun(&image, 3, 3, threads, &total));
        EXPECT_EQ(9, total);
    }

    TestImageDestroy(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that invalid configurations are rejected
 */
static void TestTaskInvalid(void)
{
    static const char *const rows[] = {"##", "##"};
    RosmiImage_t             image   = {0};
    RosmiBorders_t           borders = {0};
    RosmiTaskConfig_t        config  = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, rows, 2));
    EXPECT_EQ(ROSMI_RET_OK, RosmiBordersInitialize(&borders, 2, 2));

    config.image     = &image;
    config.seg_rows  = 1;
    config.seg_cols  = 1;
    config.tile_rows = 2;
    config.tile_cols = 2;
    config.conn      = ROSMI_CONN_4;
    config.borders   = &borders;

    EXPECT_EQ(ROSMI_INV_PARAM, RosmiTaskLabelSegments(NULL, 1));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiTaskLabelSegments(&config, 0));

    config.image = NULL;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiTaskLabelSegments(&config, 1));
    config.image = &image;

    config.borders = NULL;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiTaskLabelSegments(&config, 1));
    config.borders = &borders;

    config.seg_rows = 0;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiTaskLabelSegments(&config, 1));
    config.seg_rows = 1;

    config.tile_rows = 0;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiTaskLabelSegments(&config, 1));

    RosmiBordersDeinitialize(&borders);
    TestImageDestroy(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that a failed worker allocation leaks nothing
 */
static void TestTaskNoMemory(void)
{
    static const char *const rows[] = {"##", "##"};
    RosmiImage_t             image   = {0};
    RosmiBorders_t           borders = {0};
    RosmiTaskConfig_t        config  = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, rows, 2));
    EXPECT_EQ(ROSMI_RET_OK, RosmiBordersInitialize(&borders, 2, 2));

    config.image     = &image;
    config.seg_rows  = 1;
    config.seg_cols  = 1;
    config.tile_rows = 2;
    config.tile_cols = 2;
    config.conn      = ROSMI_CONN_4;
    config.borders   = &borders;

    for (int32_t budget = 0; budget < 2; budget++)
    {
        TestAllocFailAfter(budget);
        EXPECT_EQ(ROSMI_MALLOC_ERROR, RosmiTaskLabelSegments(&config, 1));
        TestAllocUnlimited();
    }

    RosmiBordersDeinitialize(&borders);
    TestImageDestroy(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief      Labels every segment and returns the sum of the local counts
 *  \param[in]  image: Image to label
 *  \param[in]  seg_rows: Segments down, N
 *  \param[in]  seg_cols: Segments across, M
 *  \param[in]  threads: Threads to use
 *  \param[out] total: Receives the sum of the local component counts
 *  \return     true when the run succeeded
 */
static bool TestTaskRun(const RosmiImage_t *image, int32_t seg_rows, int32_t seg_cols, int32_t threads,
                        RosmiLabel_t *total)
{
    const int32_t     segments = seg_rows * seg_cols;
    RosmiBorders_t *  borders  = (RosmiBorders_t *)RosmiMalloc((size_t)segments * sizeof(RosmiBorders_t));
    double *          times    = (double *)RosmiMalloc((size_t)segments * sizeof(double));
    RosmiTaskConfig_t config   = {0};
    bool              ret      = false;
    int32_t           ready    = 0;

    do
    {
        if ((borders == NULL) || (times == NULL))
        {
            break;
        }
        for (int32_t s = 0; s < segments; s++)
        {
            if (RosmiBordersInitialize(&borders[s], image->height / seg_rows, image->width / seg_cols)
                != ROSMI_RET_OK)
            {
                break;
            }
            ready++;
        }
        if (ready != segments)
        {
            break;
        }

        config.image     = image;
        config.seg_rows  = seg_rows;
        config.seg_cols  = seg_cols;
        config.tile_rows = image->height / seg_rows;
        config.tile_cols = image->width / seg_cols;
        config.conn      = ROSMI_CONN_4;
        config.borders   = borders;
        config.durations = times;
        config.results   = NULL;

        if (RosmiTaskLabelSegments(&config, threads) != ROSMI_RET_OK)
        {
            break;
        }

        *total = 0;
        for (int32_t s = 0; s < segments; s++)
        {
            *total += borders[s].components;
        }
        ret = true;
    } while (0);

    for (int32_t s = 0; s < ready; s++)
    {
        RosmiBordersDeinitialize(&borders[s]);
    }
    RosmiFree(borders);
    RosmiFree(times);
    return ret;
}
