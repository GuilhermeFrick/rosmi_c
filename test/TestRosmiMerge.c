/*!
 * \file      TestRosmiMerge.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with RosmiMerge unity test functions
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "uTest.h"
#include "RosmiLabel.h"
#include "RosmiMerge.h"
#include "RosmiUnionFind.h"
#include "TestRosmi.h"
#include "TestRosmiAlloc.h"
#include "TestRosmiFixture.h"

/*! \addtogroup  TestRosmiMergePrivate ROSMI Merge Test Private
 *  \ingroup TestRosmi
 * @{
 */
static void TestMergeCrossingObject(void);
static void TestMergeFourSegmentCorner(void);
static void TestMergeInvariantOverSegmentation(void);
static void TestMergeOffsets(void);
static void TestMergeInvalid(void);

static RosmiLabel_t TestMergeWholeImage(const RosmiImage_t *image, RosmiConnectivity_e conn);
static RosmiLabel_t TestMergePipeline(const RosmiImage_t *image, int32_t seg_rows, int32_t seg_cols,
                                      RosmiConnectivity_e conn);
/*! @}*/ // End of TestRosmiMergePrivate

/*!
 * \brief     Function to test all RosmiMerge functions
 */
void TestRosmiMerge(void)
{
    SetUp();

    TestMergeCrossingObject();
    TestMergeFourSegmentCorner();
    TestMergeInvariantOverSegmentation();
    TestMergeOffsets();
    TestMergeInvalid();

    TearDown();
}
/*!
 *  \brief  Tests the core requirement: an object crossing a border counts once
 *  \details This is the miniature of the whole assignment. The image has four
 *           objects, one of them straddling the vertical border, so the sum
 *           of the local counts is five and the right answer is four.
 */
static void TestMergeCrossingObject(void)
{
    static const char *const rows[] = {"##......", "...##...", "......#.", ".#....#."};
    RosmiImage_t             image  = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, rows, 4));

    /* Whole image: the reference answer */
    EXPECT_EQ(4, TestMergeWholeImage(&image, ROSMI_CONN_4));
    /* Split into 2 x 2 segments and reconciled: the same answer */
    EXPECT_EQ(4, TestMergePipeline(&image, 2, 2, ROSMI_CONN_4));

    TestImageDestroy(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests the diagonal pair at the crossing of four segments
 *  \details Under connectivity 8 the pixel at the bottom right of the top
 *           left segment touches the pixel at the top left of the bottom
 *           right segment. That pair crosses two borders at once, so neither
 *           the horizontal nor the vertical sweep sees it. Dropping the
 *           corner handling makes this case report two objects instead of
 *           one, which is exactly what this test pins down.
 */
static void TestMergeFourSegmentCorner(void)
{
    /* 4 x 4 image, split as 2 x 2 segments of 2 x 2, with the two pixels
       meeting only at the centre crossing. */
    static const char *const backslash[] = {"....", ".#..", "..#.", "...."};
    static const char *const slash[]     = {"....", "..#.", ".#..", "...."};
    RosmiImage_t             image       = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, backslash, 4));
    EXPECT_EQ(1, TestMergeWholeImage(&image, ROSMI_CONN_8));
    EXPECT_EQ(1, TestMergePipeline(&image, 2, 2, ROSMI_CONN_8));
    EXPECT_EQ(2, TestMergePipeline(&image, 2, 2, ROSMI_CONN_4));
    TestImageDestroy(&image);

    EXPECT_TRUE(TestImageFromRows(&image, slash, 4));
    EXPECT_EQ(1, TestMergeWholeImage(&image, ROSMI_CONN_8));
    EXPECT_EQ(1, TestMergePipeline(&image, 2, 2, ROSMI_CONN_8));
    TestImageDestroy(&image);

    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that the answer does not depend on how the image is split
 *  \details The strongest property the parallel version has to satisfy: the
 *           number of objects in an image cannot depend on the segmentation
 *           used to count them. If it does, the merge is wrong.
 */
static void TestMergeInvariantOverSegmentation(void)
{
    static const char *const rows[] = {"##..##..##..", ".#...#...#..", ".####....#..", "....#.......",
                                       "#...#..###..", "#...#..#.#..", "#####..###.."};
    RosmiImage_t             image  = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, rows, 7));

    for (int32_t conn_index = 0; conn_index < 2; conn_index++)
    {
        const RosmiConnectivity_e conn      = (conn_index == 0) ? ROSMI_CONN_4 : ROSMI_CONN_8;
        const RosmiLabel_t        reference = TestMergeWholeImage(&image, conn);

        /* 7 rows and 12 columns divide by these segmentations */
        EXPECT_EQ(reference, TestMergePipeline(&image, 1, 1, conn));
        EXPECT_EQ(reference, TestMergePipeline(&image, 1, 2, conn));
        EXPECT_EQ(reference, TestMergePipeline(&image, 7, 1, conn));
        EXPECT_EQ(reference, TestMergePipeline(&image, 7, 12, conn));
        EXPECT_EQ(reference, TestMergePipeline(&image, 7, 4, conn));
        EXPECT_EQ(reference, TestMergePipeline(&image, 1, 6, conn));
    }

    TestImageDestroy(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests the prefix sum that builds the global label space
 */
static void TestMergeOffsets(void)
{
    RosmiBorders_t borders[3] = {0};
    RosmiLabel_t   offsets[4] = {0};

    borders[0].components = 2;
    borders[1].components = 0;
    borders[2].components = 5;

    EXPECT_EQ(ROSMI_RET_OK, RosmiMergeOffsets(borders, 3, offsets));
    EXPECT_EQ(0, offsets[0]);
    EXPECT_EQ(2, offsets[1]);
    EXPECT_EQ(2, offsets[2]); /* an empty segment consumes no label space */
    EXPECT_EQ(7, offsets[3]);

    EXPECT_EQ(ROSMI_INV_PARAM, RosmiMergeOffsets(NULL, 3, offsets));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiMergeOffsets(borders, 3, NULL));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiMergeOffsets(borders, 0, offsets));
}
/*!
 *  \brief  Tests that invalid merge requests are rejected
 */
static void TestMergeInvalid(void)
{
    RosmiBorders_t      borders = {0};
    RosmiLabel_t        offsets[2] = {0};
    RosmiUnionFind_t    uf      = {0};
    RosmiMergeRequest_t request = {1, 1, ROSMI_CONN_4, &borders, offsets};

    TestAllocReset();
    EXPECT_EQ(ROSMI_RET_OK, RosmiUnionFindInitialize(&uf, 4));

    EXPECT_EQ(ROSMI_INV_PARAM, RosmiMergeRun(NULL, &uf, NULL));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiMergeRun(&request, NULL, NULL));

    request.borders = NULL;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiMergeRun(&request, &uf, NULL));
    request.borders = &borders;

    request.offsets = NULL;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiMergeRun(&request, &uf, NULL));
    request.offsets = offsets;

    request.seg_rows = 0;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiMergeRun(&request, &uf, NULL));
    request.seg_rows = 1;

    request.conn = (RosmiConnectivity_e)7;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiMergeRun(&request, &uf, NULL));

    RosmiUnionFindDeinitialize(&uf);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief      Counts the objects treating the image as a single block
 *  \param[in]  image: Image to count
 *  \param[in]  conn: Neighbourhood criterion
 *  \return     Reference object count
 */
static RosmiLabel_t TestMergeWholeImage(const RosmiImage_t *image, RosmiConnectivity_e conn)
{
    RosmiLabelRequest_t request;
    RosmiLabel_t        components = -1;

    request.image    = image;
    request.origin_y = 0;
    request.origin_x = 0;
    request.rows     = image->height;
    request.cols     = image->width;
    request.conn     = conn;

    EXPECT_EQ(ROSMI_RET_OK, RosmiLabelSegment(&request, NULL, &components));
    return components;
}
/*!
 *  \brief      Runs the three phase pipeline over a given segmentation
 *  \param[in]  image: Image to count
 *  \param[in]  seg_rows: Segments down, N
 *  \param[in]  seg_cols: Segments across, M
 *  \param[in]  conn: Neighbourhood criterion
 *  \return     Object count after reconciliation, or -1 on failure
 */
static RosmiLabel_t TestMergePipeline(const RosmiImage_t *image, int32_t seg_rows, int32_t seg_cols,
                                      RosmiConnectivity_e conn)
{
    const int32_t    segments  = seg_rows * seg_cols;
    const int32_t    tile_rows = image->height / seg_rows;
    const int32_t    tile_cols = image->width / seg_cols;
    RosmiBorders_t * borders   = (RosmiBorders_t *)RosmiMalloc((size_t)segments * sizeof(RosmiBorders_t));
    RosmiLabel_t *   offsets   = (RosmiLabel_t *)RosmiMalloc((size_t)(segments + 1) * sizeof(RosmiLabel_t));
    RosmiUnionFind_t uf        = {0};
    RosmiLabel_t     objects   = -1;

    do
    {
        if ((borders == NULL) || (offsets == NULL))
        {
            break;
        }
        for (int32_t s = 0; s < segments; s++)
        {
            if (RosmiBordersInitialize(&borders[s], tile_rows, tile_cols) != ROSMI_RET_OK)
            {
                break;
            }

            RosmiLabelRequest_t request;
            RosmiLabel_t        local = 0;

            request.image    = image;
            request.origin_y = (s / seg_cols) * tile_rows;
            request.origin_x = (s % seg_cols) * tile_cols;
            request.rows     = tile_rows;
            request.cols     = tile_cols;
            request.conn     = conn;

            if (RosmiLabelSegment(&request, &borders[s], &local) != ROSMI_RET_OK)
            {
                break;
            }
        }
        if (RosmiMergeOffsets(borders, segments, offsets) != ROSMI_RET_OK)
        {
            break;
        }
        if (RosmiUnionFindInitialize(&uf, offsets[segments]) != ROSMI_RET_OK)
        {
            break;
        }
        for (RosmiLabel_t i = 0; i < offsets[segments]; i++)
        {
            RosmiLabel_t created = 0;

            (void)RosmiUnionFindMakeSet(&uf, &created);
        }

        const RosmiMergeRequest_t request = {seg_rows, seg_cols, conn, borders, offsets};

        if (RosmiMergeRun(&request, &uf, NULL) != ROSMI_RET_OK)
        {
            break;
        }
        objects = RosmiUnionFindCountRoots(&uf);
    } while (0);

    RosmiUnionFindDeinitialize(&uf);
    if (borders != NULL)
    {
        for (int32_t s = 0; s < segments; s++)
        {
            RosmiBordersDeinitialize(&borders[s]);
        }
    }
    RosmiFree(borders);
    RosmiFree(offsets);
    return objects;
}
