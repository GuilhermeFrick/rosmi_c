/*!
 * \file      TestRosmiLabel.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with RosmiLabel unity test functions
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "uTest.h"
#include "RosmiLabel.h"
#include "TestRosmi.h"
#include "TestRosmiAlloc.h"
#include "TestRosmiFixture.h"

/*! \addtogroup  TestRosmiLabelPrivate ROSMI Label Test Private
 *  \ingroup TestRosmi
 * @{
 */
static void         TestLabelEmptyAndFull(void);
static void         TestLabelSeparateObjects(void);
static void         TestLabelUShapeNeedsUnion(void);
static void         TestLabelConnectivityChangesAnswer(void);
static void         TestLabelBorders(void);
static void         TestLabelInvalid(void);
static void         TestLabelNoMemory(void);
static RosmiLabel_t TestLabelCount(const char *const *rows, int32_t count, RosmiConnectivity_e conn);
/*! @}*/ // End of TestRosmiLabelPrivate

/*!
 * \brief     Function to test all RosmiLabel functions
 */
void TestRosmiLabel(void)
{
    SetUp();

    TestLabelEmptyAndFull();
    TestLabelSeparateObjects();
    TestLabelUShapeNeedsUnion();
    TestLabelConnectivityChangesAnswer();
    TestLabelBorders();
    TestLabelInvalid();
    TestLabelNoMemory();

    TearDown();
}
/*!
 *  \brief  Tests the two degenerate images: no object and a single blob
 */
static void TestLabelEmptyAndFull(void)
{
    static const char *const empty[] = {"....", "....", "...."};
    static const char *const full[]  = {"####", "####", "####"};

    EXPECT_EQ(0, TestLabelCount(empty, 3, ROSMI_CONN_4));
    EXPECT_EQ(1, TestLabelCount(full, 3, ROSMI_CONN_4));
}
/*!
 *  \brief  Tests that objects with a gap between them stay separate
 */
static void TestLabelSeparateObjects(void)
{
    static const char *const rows[] = {"#.#.#", ".....", "#.#.#"};

    EXPECT_EQ(6, TestLabelCount(rows, 3, ROSMI_CONN_4));
}
/*!
 *  \brief  Tests the case that forces an equivalence union
 *  \details The two arms of the U get different labels on the first row and
 *           are only revealed to be the same object on the last one. Without
 *           the union find, this image would be counted as two objects.
 */
static void TestLabelUShapeNeedsUnion(void)
{
    static const char *const u_shape[] = {"#..#", "#..#", "####"};

    EXPECT_EQ(1, TestLabelCount(u_shape, 3, ROSMI_CONN_4));
}
/*!
 *  \brief  Tests that the connectivity criterion changes the answer
 *  \details A pure diagonal is one object under connectivity 8 and two under
 *           connectivity 4. Both answers are correct; what matters is that
 *           the option is honoured.
 */
static void TestLabelConnectivityChangesAnswer(void)
{
    static const char *const diagonal[] = {"#.", ".#"};

    EXPECT_EQ(2, TestLabelCount(diagonal, 2, ROSMI_CONN_4));
    EXPECT_EQ(1, TestLabelCount(diagonal, 2, ROSMI_CONN_8));
}
/*!
 *  \brief  Tests that the four border vectors carry the expected labels
 */
static void TestLabelBorders(void)
{
    static const char *const rows[] = {"#..#", "....", "#..#"};
    RosmiImage_t             image  = {0};
    RosmiBorders_t           borders = {0};
    RosmiLabelRequest_t      request;
    RosmiLabel_t             components = 0;

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, rows, 3));
    EXPECT_EQ(ROSMI_RET_OK, RosmiBordersInitialize(&borders, 3, 4));

    request.image    = &image;
    request.origin_y = 0;
    request.origin_x = 0;
    request.rows     = 3;
    request.cols     = 4;
    request.conn     = ROSMI_CONN_4;

    EXPECT_EQ(ROSMI_RET_OK, RosmiLabelSegment(&request, &borders, &components));
    EXPECT_EQ(4, components);
    EXPECT_EQ(4, borders.components);

    /* Corners hold objects, the middle of every border is background */
    EXPECT_NE(ROSMI_LABEL_BACKGROUND, borders.top[0]);
    EXPECT_NE(ROSMI_LABEL_BACKGROUND, borders.top[3]);
    EXPECT_EQ(ROSMI_LABEL_BACKGROUND, borders.top[1]);
    EXPECT_NE(ROSMI_LABEL_BACKGROUND, borders.bottom[0]);
    EXPECT_EQ(ROSMI_LABEL_BACKGROUND, borders.left[1]);
    EXPECT_NE(ROSMI_LABEL_BACKGROUND, borders.right[2]);

    /* The four corners are four distinct components */
    EXPECT_NE(borders.top[0], borders.top[3]);
    EXPECT_NE(borders.top[0], borders.bottom[0]);

    RosmiBordersDeinitialize(&borders);
    TestImageDestroy(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that invalid requests are rejected
 */
static void TestLabelInvalid(void)
{
    static const char *const rows[] = {"##", "##"};
    RosmiImage_t             image  = {0};
    RosmiBorders_t           borders = {0};
    RosmiLabelRequest_t      request;
    RosmiLabel_t             components = 0;

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, rows, 2));

    request.image    = &image;
    request.origin_y = 0;
    request.origin_x = 0;
    request.rows     = 2;
    request.cols     = 2;
    request.conn     = ROSMI_CONN_4;

    EXPECT_EQ(ROSMI_INV_PARAM, RosmiLabelSegment(NULL, NULL, &components));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiLabelSegment(&request, NULL, NULL));

    request.conn = (RosmiConnectivity_e)5;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiLabelSegment(&request, NULL, &components));
    request.conn = ROSMI_CONN_4;

    request.rows = 99; /* rectangle outside the image */
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiLabelSegment(&request, NULL, &components));
    request.rows = 2;

    request.origin_y = -1;
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiLabelSegment(&request, NULL, &components));
    request.origin_y = 0;

    /* Borders whose dimensions disagree with the request */
    EXPECT_EQ(ROSMI_RET_OK, RosmiBordersInitialize(&borders, 4, 4));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiLabelSegment(&request, &borders, &components));
    RosmiBordersDeinitialize(&borders);

    EXPECT_EQ(ROSMI_INV_PARAM, RosmiBordersInitialize(NULL, 2, 2));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiBordersInitialize(&borders, 0, 2));
    RosmiBordersDeinitialize(NULL);

    TestImageDestroy(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests the out of memory paths of the labeling
 */
static void TestLabelNoMemory(void)
{
    static const char *const rows[] = {"##", "##"};
    RosmiImage_t             image  = {0};
    RosmiBorders_t           borders = {0};
    RosmiLabelRequest_t      request;
    RosmiLabel_t             components = 0;

    TestAllocReset();
    EXPECT_TRUE(TestImageFromRows(&image, rows, 2));

    request.image    = &image;
    request.origin_y = 0;
    request.origin_x = 0;
    request.rows     = 2;
    request.cols     = 2;
    request.conn     = ROSMI_CONN_4;

    /* Borders allocation fails */
    TestAllocFailAfter(0);
    EXPECT_EQ(ROSMI_MALLOC_ERROR, RosmiBordersInitialize(&borders, 2, 2));

    /* Each of the first allocations inside the labeling fails in turn; none
       of them may leak, whichever one gives up. */
    for (int32_t budget = 0; budget < 4; budget++)
    {
        TestAllocFailAfter(budget);

        const RosmiReturn_e ret = RosmiLabelSegment(&request, NULL, &components);

        EXPECT_EQ(ROSMI_MALLOC_ERROR, ret);
        TestAllocUnlimited();
    }

    /* Still works once the allocator recovers */
    EXPECT_EQ(ROSMI_RET_OK, RosmiLabelSegment(&request, NULL, &components));
    EXPECT_EQ(1, components);

    TestImageDestroy(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief      Counts the objects of a text image, asserting along the way
 *  \param[in]  rows: Text rows of the image
 *  \param[in]  count: Number of rows
 *  \param[in]  conn: Neighbourhood criterion
 *  \return     Components found, or -1 when the fixture itself failed
 */
static RosmiLabel_t TestLabelCount(const char *const *rows, int32_t count, RosmiConnectivity_e conn)
{
    RosmiImage_t        image      = {0};
    RosmiLabel_t        components = -1;
    RosmiLabelRequest_t request;

    TestAllocReset();
    if (TestImageFromRows(&image, rows, count))
    {
        request.image    = &image;
        request.origin_y = 0;
        request.origin_x = 0;
        request.rows     = image.height;
        request.cols     = image.width;
        request.conn     = conn;

        EXPECT_EQ(ROSMI_RET_OK, RosmiLabelSegment(&request, NULL, &components));
        TestImageDestroy(&image);
        EXPECT_EQ(0, TestAllocOutstanding());
    }
    return components;
}
