/*!
 * \file      TestRosmiReport.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with RosmiReport unity test functions
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "uTest.h"
#include <stdio.h>
#include <string.h>
#include "RosmiReport.h"
#include "TestRosmi.h"

/*! \addtogroup  TestRosmiReportPrivate ROSMI Report Test Private
 *  \ingroup TestRosmi
 * @{
 */

/*!
 *  \brief Files written by these tests, removed at the end
 */
static const char TEST_REPORT_CSV[]   = "test_report_tmp.csv";
static const char TEST_REPORT_PREFIX[] = "test_report_tmp";

static void  TestReportWritesHeaderOnce(void);
static void  TestReportTraces(void);
static void  TestReportInvalid(void);
static void  TestReportReturnStrings(void);
static int32_t TestReportCountLines(const char *path);
static bool  TestReportFirstLineIs(const char *path, const char *expected);
/*! @}*/ // End of TestRosmiReportPrivate

/*!
 * \brief     Function to test all RosmiReport functions
 */
void TestRosmiReport(void)
{
    SetUp();

    TestReportWritesHeaderOnce();
    TestReportTraces();
    TestReportInvalid();
    TestReportReturnStrings();

    (void)remove(TEST_REPORT_CSV);
    (void)remove("test_report_tmp_tiles.csv");
    (void)remove("test_report_tmp_edges.csv");
    TearDown();
}
/*!
 *  \brief  Tests that the header is written once and rows accumulate
 *  \details The header decision cannot use ftell right after opening in
 *           append mode, so it deserves an explicit test: a second row must
 *           not bring a second header.
 */
static void TestReportWritesHeaderOnce(void)
{
    const RosmiReportRow_t row = {"seq", "img.pbm", 1536, 3072, 2, 3, 768, 1024, 4, 1, 13, 16, 0.5};

    (void)remove(TEST_REPORT_CSV);

    EXPECT_EQ(ROSMI_RET_OK, RosmiReportAppendCsv(TEST_REPORT_CSV, &row));
    EXPECT_EQ(2, TestReportCountLines(TEST_REPORT_CSV)); /* header + row */
    EXPECT_TRUE(TestReportFirstLineIs(TEST_REPORT_CSV, "versao,imagem"));

    EXPECT_EQ(ROSMI_RET_OK, RosmiReportAppendCsv(TEST_REPORT_CSV, &row));
    EXPECT_EQ(3, TestReportCountLines(TEST_REPORT_CSV)); /* only one more row */
}
/*!
 *  \brief  Tests the per tile and per edge traces
 */
static void TestReportTraces(void)
{
    const RosmiLabel_t components[6] = {1, 2, 3, 4, 5, 6};
    const double       durations[6]  = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6};

    EXPECT_EQ(ROSMI_RET_OK,
              RosmiReportWriteTiles(TEST_REPORT_PREFIX, 2, 3, 768, 1024, components, durations));
    /* header plus one row per segment */
    EXPECT_EQ(7, TestReportCountLines("test_report_tmp_tiles.csv"));

    EXPECT_EQ(ROSMI_RET_OK, RosmiReportWriteEdges(TEST_REPORT_PREFIX, 2, 3, 768, 1024));
    /* header, plus N*(M-1) horizontal and (N-1)*M vertical edges = 4 + 3 */
    EXPECT_EQ(8, TestReportCountLines("test_report_tmp_edges.csv"));
}
/*!
 *  \brief  Tests that bad arguments and unwritable paths are refused
 */
static void TestReportInvalid(void)
{
    const RosmiReportRow_t row           = {"seq", "i", 1, 1, 1, 1, 1, 1, 4, 1, 0, 0, 0.0};
    const RosmiLabel_t     components[1] = {0};
    const double           durations[1]  = {0.0};

    EXPECT_EQ(ROSMI_INV_PARAM, RosmiReportAppendCsv(NULL, &row));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiReportAppendCsv(TEST_REPORT_CSV, NULL));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiReportWriteTiles(NULL, 1, 1, 1, 1, components, durations));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiReportWriteTiles(TEST_REPORT_PREFIX, 1, 1, 1, 1, NULL, durations));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiReportWriteTiles(TEST_REPORT_PREFIX, 1, 1, 1, 1, components, NULL));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiReportWriteEdges(NULL, 1, 1, 1, 1));

    /* A directory that does not exist cannot be opened for writing */
    EXPECT_EQ(ROSMI_IO_ERROR, RosmiReportAppendCsv("dir_inexistente/x.csv", &row));
    EXPECT_EQ(ROSMI_IO_ERROR,
              RosmiReportWriteTiles("dir_inexistente/x", 1, 1, 1, 1, components, durations));
    EXPECT_EQ(ROSMI_IO_ERROR, RosmiReportWriteEdges("dir_inexistente/x", 1, 1, 1, 1));
}
/*!
 *  \brief  Tests that every return code has a description
 */
static void TestReportReturnStrings(void)
{
    EXPECT_STREQ((char *)"ok", (char *)RosmiReturnStr(ROSMI_RET_OK));
    EXPECT_STREQ((char *)"invalid parameter", (char *)RosmiReturnStr(ROSMI_INV_PARAM));
    EXPECT_STREQ((char *)"out of memory", (char *)RosmiReturnStr(ROSMI_MALLOC_ERROR));
    EXPECT_STREQ((char *)"io error", (char *)RosmiReturnStr(ROSMI_IO_ERROR));
    EXPECT_STREQ((char *)"malformed file", (char *)RosmiReturnStr(ROSMI_FORMAT_ERROR));
    EXPECT_STREQ((char *)"thread error", (char *)RosmiReturnStr(ROSMI_THREAD_ERROR));
    EXPECT_STREQ((char *)"generic error", (char *)RosmiReturnStr(ROSMI_ERROR));
    /* An out of range code still yields a usable string, never NULL */
    EXPECT_STREQ((char *)"unknown", (char *)RosmiReturnStr((RosmiReturn_e)-99));
}
/*!
 *  \brief      Counts the lines of a text file
 *  \param[in]  path: File to read
 *  \return     Line count, or -1 when the file cannot be opened
 */
static int32_t TestReportCountLines(const char *path)
{
    int32_t lines = -1;
    FILE *  file  = fopen(path, "r");

    if (file != NULL)
    {
        int c = 0;

        lines = 0;
        while ((c = fgetc(file)) != EOF)
        {
            if (c == '\n')
            {
                lines++;
            }
        }
        (void)fclose(file);
    }
    return lines;
}
/*!
 *  \brief      Tells whether the first line starts with the expected text
 *  \param[in]  path: File to read
 *  \param[in]  expected: Prefix to look for
 *  \return     true when the first line starts with expected
 */
static bool TestReportFirstLineIs(const char *path, const char *expected)
{
    bool  ret  = false;
    FILE *file = fopen(path, "r");

    if (file != NULL)
    {
        char line[256];

        if (fgets(line, (int)sizeof(line), file) != NULL)
        {
            ret = (strncmp(line, expected, strlen(expected)) == 0);
        }
        (void)fclose(file);
    }
    return ret;
}
