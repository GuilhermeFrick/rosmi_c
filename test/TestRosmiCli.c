/*!
 * \file      TestRosmiCli.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with RosmiCli unity test functions
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "uTest.h"
#include "RosmiCli.h"
#include "TestRosmi.h"

/*! \addtogroup  TestRosmiCliPrivate ROSMI Cli Test Private
 *  \ingroup TestRosmi
 * @{
 */
static void TestCliDefaults(void);
static void TestCliAllOptions(void);
static void TestCliRejects(void);
static void TestCliUsage(void);
/*! @}*/ // End of TestRosmiCliPrivate

/*!
 * \brief     Function to test all RosmiCli functions
 */
void TestRosmiCli(void)
{
    SetUp();

    TestCliDefaults();
    TestCliAllOptions();
    TestCliRejects();
    TestCliUsage();

    TearDown();
}
/*!
 *  \brief  Tests the minimal command line and the defaults it implies
 */
static void TestCliDefaults(void)
{
    char *     argv[] = {(char *)"rosmi", (char *)"img.pbm", (char *)"--N", (char *)"2", (char *)"--M",
                         (char *)"3"};
    RosmiCli_t options = {0};

    EXPECT_EQ(ROSMI_RET_OK, RosmiCliParse(&options, 6, argv));
    EXPECT_STREQ((char *)"img.pbm", (char *)options.image);
    EXPECT_EQ(2, options.seg_rows);
    EXPECT_EQ(3, options.seg_cols);
    EXPECT_EQ(ROSMI_CONN_4, options.conn); /* default */
    EXPECT_EQ(1, options.reps);            /* default */
    EXPECT_EQ(0, options.threads);         /* means every processor */
    EXPECT_EQ(0, (options.csv != NULL));
    EXPECT_EQ(0, (options.trace != NULL));
}
/*!
 *  \brief  Tests that every option is read, in an arbitrary order
 */
static void TestCliAllOptions(void)
{
    char *argv[] = {(char *)"rosmi",   (char *)"--threads", (char *)"8",      (char *)"--conn",
                    (char *)"8",       (char *)"img.pbm",   (char *)"--csv",  (char *)"out.csv",
                    (char *)"--reps",  (char *)"5",         (char *)"--N",    (char *)"7",
                    (char *)"--trace", (char *)"pre",       (char *)"--M",    (char *)"7"};
    RosmiCli_t options = {0};

    EXPECT_EQ(ROSMI_RET_OK, RosmiCliParse(&options, 16, argv));
    EXPECT_STREQ((char *)"img.pbm", (char *)options.image);
    EXPECT_EQ(7, options.seg_rows);
    EXPECT_EQ(7, options.seg_cols);
    EXPECT_EQ(ROSMI_CONN_8, options.conn);
    EXPECT_EQ(8, options.threads);
    EXPECT_EQ(5, options.reps);
    EXPECT_STREQ((char *)"out.csv", (char *)options.csv);
    EXPECT_STREQ((char *)"pre", (char *)options.trace);
}
/*!
 *  \brief  Tests that bad command lines are refused
 *  \details Refusing beats defaulting here: silently guessing a segmentation
 *           would produce a plausible but meaningless answer.
 */
static void TestCliRejects(void)
{
    RosmiCli_t options = {0};
    char *     no_image[]  = {(char *)"rosmi", (char *)"--N", (char *)"2", (char *)"--M", (char *)"3"};
    char *     no_n[]      = {(char *)"rosmi", (char *)"img.pbm", (char *)"--M", (char *)"3"};
    char *     zero_m[]    = {(char *)"rosmi", (char *)"img.pbm", (char *)"--N", (char *)"2", (char *)"--M",
                              (char *)"0"};
    char *     bad_conn[]  = {(char *)"rosmi", (char *)"img.pbm", (char *)"--N",    (char *)"2",
                              (char *)"--M",   (char *)"3",       (char *)"--conn", (char *)"5"};
    char *     bad_reps[]  = {(char *)"rosmi", (char *)"img.pbm", (char *)"--N",    (char *)"2",
                              (char *)"--M",   (char *)"3",       (char *)"--reps", (char *)"0"};
    char *     unknown[]   = {(char *)"rosmi", (char *)"img.pbm", (char *)"--N",  (char *)"2",
                              (char *)"--M",   (char *)"3",       (char *)"--xyz"};
    char *     two_images[] = {(char *)"rosmi", (char *)"a.pbm", (char *)"b.pbm", (char *)"--N",
                               (char *)"2",     (char *)"--M",   (char *)"3"};
    char *     dangling[]  = {(char *)"rosmi", (char *)"img.pbm", (char *)"--N", (char *)"2", (char *)"--M"};

    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(NULL, 5, no_image));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(&options, 5, NULL));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(&options, 5, no_image));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(&options, 4, no_n));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(&options, 6, zero_m));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(&options, 8, bad_conn));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(&options, 8, bad_reps));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(&options, 7, unknown));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(&options, 7, two_images));
    /* An option at the very end, with no value after it, is not consumed */
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiCliParse(&options, 5, dangling));
}
/*!
 *  \brief  Tests that the usage text can be printed without crashing
 */
static void TestCliUsage(void)
{
    RosmiCliUsage("rosmi_test");
    EXPECT_TRUE(true);
}
