/*!
 * \file      TestRosmiMain.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Entry point of the ROSMI test suite
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "uTest.h"
#include "Rosmi.h"
#include "TestRosmi.h"

/*!
 * \brief     Runs every ROSMI test group
 */
void TestRosmi(void)
{
    TestRosmiUnionFind();
    TestRosmiImage();
    TestRosmiPng();
    TestRosmiLabel();
    TestRosmiMerge();
    TestRosmiTask();
    TestRosmiCli();
    TestRosmiReport();
}
/*!
 *  \brief   Entry point of the test binary
 *  \return  0 when every assertion passed, 1 otherwise
 */
int main(void)
{
    TestPrintf("ROSMI test suite\n");
    TestRosmi();
    return 0;
}
