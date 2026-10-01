/*!
 * \file      TestRosmiUnionFind.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with RosmiUnionFind unity test functions
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "uTest.h"
#include "RosmiUnionFind.h"
#include "TestRosmi.h"
#include "TestRosmiAlloc.h"

/*! \addtogroup  TestRosmiUnionFindPrivate ROSMI Union Find Test Private
 *  \ingroup TestRosmi
 * @{
 */
static void TestUnionFindMakeAndCount(void);
static void TestUnionFindUnite(void);
static void TestUnionFindTransitive(void);
static void TestUnionFindGrow(void);
static void TestUnionFindInvalid(void);
static void TestUnionFindNoMemory(void);
/*! @}*/ // End of TestRosmiUnionFindPrivate

/*!
 * \brief     Function to test all RosmiUnionFind functions
 */
void TestRosmiUnionFind(void)
{
    SetUp();

    TestUnionFindMakeAndCount();
    TestUnionFindUnite();
    TestUnionFindTransitive();
    TestUnionFindGrow();
    TestUnionFindInvalid();
    TestUnionFindNoMemory();

    TearDown();
}
/*!
 *  \brief  Tests that each new set counts as one root
 */
static void TestUnionFindMakeAndCount(void)
{
    RosmiUnionFind_t uf    = {0};
    RosmiLabel_t     label = 0;

    TestAllocReset();
    EXPECT_EQ(ROSMI_RET_OK, RosmiUnionFindInitialize(&uf, 4));
    for (int32_t i = 0; i < 5; i++)
    {
        EXPECT_EQ(ROSMI_RET_OK, RosmiUnionFindMakeSet(&uf, &label));
        EXPECT_EQ(i, label);
    }
    EXPECT_EQ(5, RosmiUnionFindCountRoots(&uf));

    RosmiUnionFindDeinitialize(&uf);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that uniting two sets reduces the root count by one
 */
static void TestUnionFindUnite(void)
{
    RosmiUnionFind_t uf    = {0};
    RosmiLabel_t     label = 0;

    TestAllocReset();
    EXPECT_EQ(ROSMI_RET_OK, RosmiUnionFindInitialize(&uf, 8));
    for (int32_t i = 0; i < 4; i++)
    {
        (void)RosmiUnionFindMakeSet(&uf, &label);
    }

    RosmiUnionFindUnite(&uf, 0, 1);
    EXPECT_EQ(3, RosmiUnionFindCountRoots(&uf));

    /* Uniting the same pair again must be a no operation */
    RosmiUnionFindUnite(&uf, 1, 0);
    EXPECT_EQ(3, RosmiUnionFindCountRoots(&uf));
    EXPECT_EQ(RosmiUnionFindFind(&uf, 0), RosmiUnionFindFind(&uf, 1));

    RosmiUnionFindDeinitialize(&uf);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that unions chain transitively into a single set
 */
static void TestUnionFindTransitive(void)
{
    RosmiUnionFind_t uf    = {0};
    RosmiLabel_t     label = 0;

    TestAllocReset();
    EXPECT_EQ(ROSMI_RET_OK, RosmiUnionFindInitialize(&uf, 8));
    for (int32_t i = 0; i < 6; i++)
    {
        (void)RosmiUnionFindMakeSet(&uf, &label);
    }

    RosmiUnionFindUnite(&uf, 0, 1);
    RosmiUnionFindUnite(&uf, 2, 3);
    RosmiUnionFindUnite(&uf, 1, 2); /* joins the two pairs */
    EXPECT_EQ(3, RosmiUnionFindCountRoots(&uf));
    EXPECT_EQ(RosmiUnionFindFind(&uf, 0), RosmiUnionFindFind(&uf, 3));

    RosmiUnionFindDeinitialize(&uf);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that the capacity grows past the initial allocation
 */
static void TestUnionFindGrow(void)
{
    RosmiUnionFind_t uf    = {0};
    RosmiLabel_t     label = 0;

    TestAllocReset();
    EXPECT_EQ(ROSMI_RET_OK, RosmiUnionFindInitialize(&uf, 1));

    /* The minimum capacity is 64, so 300 sets force at least three growths */
    for (int32_t i = 0; i < 300; i++)
    {
        EXPECT_EQ(ROSMI_RET_OK, RosmiUnionFindMakeSet(&uf, &label));
    }
    EXPECT_EQ(300, RosmiUnionFindCountRoots(&uf));
    EXPECT_EQ(299, label);

    RosmiUnionFindDeinitialize(&uf);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that invalid arguments are rejected instead of crashing
 */
static void TestUnionFindInvalid(void)
{
    RosmiUnionFind_t uf    = {0};
    RosmiLabel_t     label = 0;

    TestAllocReset();
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiUnionFindInitialize(NULL, 4));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiUnionFindInitialize(&uf, -1));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiUnionFindMakeSet(NULL, &label));

    EXPECT_EQ(ROSMI_RET_OK, RosmiUnionFindInitialize(&uf, 4));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiUnionFindMakeSet(&uf, NULL));

    /* Out of range labels resolve to background and unions ignore them */
    EXPECT_EQ(ROSMI_LABEL_BACKGROUND, RosmiUnionFindFind(&uf, 0));
    EXPECT_EQ(ROSMI_LABEL_BACKGROUND, RosmiUnionFindFind(&uf, -5));
    RosmiUnionFindUnite(&uf, 0, 1);
    EXPECT_EQ(0, RosmiUnionFindCountRoots(&uf));

    RosmiUnionFindDeinitialize(&uf);
    RosmiUnionFindDeinitialize(&uf); /* releasing twice must be safe */
    RosmiUnionFindDeinitialize(NULL);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests the out of memory paths using the injecting allocator
 *  \details Reaching these lines is only possible because the test binary
 *           overrides the weak RosmiMalloc with a controllable strong one.
 */
static void TestUnionFindNoMemory(void)
{
    RosmiUnionFind_t uf    = {0};
    RosmiLabel_t     label = 0;

    /* First allocation of the initialize fails */
    TestAllocReset();
    TestAllocFailAfter(0);
    EXPECT_EQ(ROSMI_MALLOC_ERROR, RosmiUnionFindInitialize(&uf, 4));
    EXPECT_EQ(0, TestAllocOutstanding());

    /* Second allocation fails: the first one must still be released */
    TestAllocReset();
    TestAllocFailAfter(1);
    EXPECT_EQ(ROSMI_MALLOC_ERROR, RosmiUnionFindInitialize(&uf, 4));
    EXPECT_EQ(0, TestAllocOutstanding());

    /* Growth fails while making a set beyond the capacity */
    TestAllocReset();
    EXPECT_EQ(ROSMI_RET_OK, RosmiUnionFindInitialize(&uf, 64));
    for (int32_t i = 0; i < 64; i++)
    {
        (void)RosmiUnionFindMakeSet(&uf, &label);
    }
    TestAllocFailAfter(0);
    EXPECT_EQ(ROSMI_MALLOC_ERROR, RosmiUnionFindMakeSet(&uf, &label));
    /* The structure stays usable with the previous capacity */
    EXPECT_EQ(64, RosmiUnionFindCountRoots(&uf));

    RosmiUnionFindDeinitialize(&uf);
    TestAllocUnlimited();
    EXPECT_EQ(0, TestAllocOutstanding());
}
