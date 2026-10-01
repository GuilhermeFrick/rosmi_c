/**
 * @file uTest.c
 * @author Guilherme Frick de Oliveira (guilhermeoliveira@deltaglobal.com.br)
 * @brief Source file with functions that reproduce those used in the GoogleTest API
 * @version 0.1
 * @date 2022-07-14
 *
 * @copyright Copyright (c) 2022
 *
 */

#include "uTest.h"
#include <stdarg.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#define ENGLISH    1 ///< English language
#define PORTUGUESE 2 ///< English language

#define LANGUAGE ENGLISH ///< Language selected

#if (LANGUAGE == ENGLISH)
#define RUNNING_TEST  "Running test"                          ///< Test string definition
#define SET_UP_TEST   "Global test environment set-up"        ///< Test string definition
#define NOT_SET_UP    "Global test environment not set-up"    ///< Test string definition
#define TEAR_DOWN     "Global test environment tear-down"     ///< Test string definition
#define NOT_TEAR_DOWN "Global test environment not tear-down" ///< Test string definition
#define TEST_CASE     "Test Case"                             ///< Test string definition
#define TESTS         "tests"                                 ///< Test string definition
#define RAN           "ran"                                   ///< Test string definition
#define RESULT        "Result"                                ///< Test string definition
#define EXPECTED      "Expected"                              ///< Test string definition
#define NOT_EXPECTED  "Not Expected"                          ///< Test string definition
#define GREATER_THAN  "Greater than"                          ///< Test string definition
#define ACTUAL        "Actual"                                ///< Test string definition
#define FILE          "File"                                  ///< Test string definition
#define LINE          "Line"                                  ///< Test string definition
#define RUN           "[ RUN      ]"                          ///< Test string definition
#define FAIL          "[     FAIL ]"                          ///< Test string definition
#define ERROR         "[    ERROR ]"                          ///< Test string definition
#define FAILED        "[  FAILED  ]"                          ///< Test string definition
#define PASSED        "[  PASSED  ]"                          ///< Test string definition
#elif (LANGUAGE == ENGLISH)
#define RUNNING_TEST  "Iniciando Teste"                     ///< Test string definition
#define SET_UP_TEST   "Ambiente de testes configurado"      ///< Test string definition
#define NOT_SET_UP    "Ambiente de testes nao configurado." ///< Test string definition
#define TEAR_DOWN     "Ambiente de testes finalizado"       ///< Test string definition
#define NOT_TEAR_DOWN "Ambiente de testes nao finalizado"   ///< Test string definition
#define TEST_CASE     "Caso de teste"                       ///< Test string definition
#define TESTS         "testes"                              ///< Test string definition
#define RAN           "executados"                          ///< Test string definition
#define RESULT        "Resultado"                           ///< Test string definition
#define EXPECTED      "Esperado"                            ///< Test string definition
#define NOT_EXPECTED  "Nao Esperado"                        ///< Test string definition
#define GREATER_THAN  "Maior que"                           ///< Test string definition
#define ACTUAL        "Atual"                               ///< Test string definition
#define FILE          "Arquivo"                             ///< Test string definition
#define LINE          "Linha"                               ///< Test string definition
#define RUN           "[ RODA     ]"                        ///< Test string definition
#define FAIL          "[    FALHA ]"                        ///< Test string definition
#define ERROR         "[    ERRO  ]"                        ///< Test string definition
#define FAILED        "[  FALHOU  ]"                        ///< Test string definition
#define PASSED        "[  PASSOU  ]"                        ///< Test string definition
#else
#error "LANGUAGE not defined"
#endif

static uint8_t TestDebugData[128]; ///< Buffer to debug test data

#ifndef __weak
#define __weak __attribute__((weak)) ///< Definition of weak attribute
#endif

__weak bool  TestWrite(uint8_t *data, uint32_t size);
__weak void *TestMalloc(uint32_t size);
__weak void  TestFree(void *buff);
__weak void  TestFatalError(void);

/**
 *  @brief Struct with test control
 */
typedef struct
{
    uint8_t  TestCaseName[64];  ///< Name of Test Case, function that performs SetUp
    uint32_t test_count;        ///< Number of tests performed
    uint32_t error_count;       ///< Number of failures occurred
    uint32_t TestCaseTimestamp; ///< Time stamp of the start of the test case execution
    uint32_t TestTimestamp;     ///< Time stamp of the start of the test execution
    bool     SetUp_control;     ///< Functions SetUp and TearDown execution control
} TestControl_t;

/// Global control structure
static TestControl_t TestControl = {
    .test_count        = 0,
    .error_count       = 0,
    .TestCaseTimestamp = 0,
    .TestTimestamp     = 0,
    .SetUp_control     = false,
};
/**
 *  @brief      Printf on @ref TestWrite defined interface
 *  @param[in]  format: like default printf
 */
void TestPrintf(char *format, ...)
{
    va_list  argptr;
    uint32_t buffer_size = 0;

    va_start(argptr, format);

    buffer_size = vsnprintf((char *)TestDebugData, sizeof(TestDebugData), format, argptr);
    TestWrite(TestDebugData, buffer_size);

    va_end(argptr);
}
/**
 *  @brief      Set Up test configuration (start test case)
 *  @param[in]  test_case_name: string with function (TestCase) name
 */
void TestSetUp(const char *test_case_name)
{
    if (TestControl.SetUp_control == false)
    {
        TestPrintf("\r\n[==========] " RUNNING_TEST ".\r\n");
        TestPrintf("[----------] " SET_UP_TEST ".\r\n");

        snprintf((char *)TestControl.TestCaseName, sizeof(TestControl.TestCaseName), "%s", test_case_name);
        TestControl.TestCaseName[sizeof(TestControl.TestCaseName) - 1] = '\0';
        TestPrintf("[----------] " TEST_CASE ": %s\r\n", TestControl.TestCaseName);

        TestControl.test_count        = 0;
        TestControl.error_count       = 0;
        TestControl.SetUp_control     = true;
        TestControl.TestCaseTimestamp = TestGetTick();
        TestControl.TestTimestamp     = TestGetTick();
    }
    else
    {
        TestPrintf(ERROR " " NOT_TEAR_DOWN ".\r\n");
    }
}
/**
 *  @brief      Tear Down test configuration (ends test case)
 */
void TestTearDown(void)
{
    if (TestControl.SetUp_control == true)
    {
        TestPrintf("[----------] " TEAR_DOWN ".\r\n");
        TestPrintf("[==========] %d " TESTS " " RAN " (%d ms total).\r\n", TestControl.test_count, TestGetElapsedTime(TestControl.TestCaseTimestamp));

        if (TestControl.error_count)
        {
            TestPrintf(FAILED " " TEST_CASE ": %s : %d / %d " TESTS ".\r\n", TestControl.TestCaseName, TestControl.error_count,
                       TestControl.test_count);
        }
        else
        {
            TestPrintf(PASSED " " TEST_CASE ": %s : %d " TESTS ".\r\n", TestControl.TestCaseName, TestControl.test_count);
        }

        TestControl.SetUp_control = false;
    }
    else
    {
        TestPrintf(ERROR " " NOT_SET_UP ".\r\n");
    }
}
/**
 *  @brief      Function to execute Number Comparison
 *  @param[in]  func: string with function (Test) name
 *  @warning    Stronger function must be declared externally
 */
void TestRunning(const char *func)
{
    TestControl.test_count++;
    TestControl.TestTimestamp = TestGetTick();
    TestPrintf(RUN " [%d] %s\r\n", TestControl.test_count, func);
}
/**
 *  @brief      Function to execute Number Comparison
 *  @param[in]  expected: expected number
 *  @param[in]  actual: actual number
 *  @param[in]  fatal: Boolean indicating whether a test failure will report @ref TestFatalError
 *  @param[in]  file: string with file name
 *  @param[in]  line: line number
 *  @return     Boolean indicating the success of the operation
 *  @warning    Stronger function must be declared externally
 */
bool BinaryEqual(int32_t expected, int32_t actual, bool fatal, const char *file, int32_t line)
{
    bool ret;

    if (expected == actual)
    {
        TestPrintf("[       OK ] [%d] " RESULT ": %d (%d ms)\r\n", TestControl.test_count, actual, TestGetElapsedTime(TestControl.TestTimestamp));
        ret = true;
    }
    else
    {
        TestControl.error_count++;
        TestPrintf(FAIL " [%d] (%d ms)\r\n", TestControl.test_count, TestGetElapsedTime(TestControl.TestTimestamp));
        TestPrintf("  " FILE ": %s " LINE ": %d\r\n", file, line);
        TestPrintf("  " ACTUAL ": %d\r\n", actual);
        TestPrintf("  " EXPECTED ": %d\r\n", expected);
        ret = false;
        if (fatal)
        {
            TestPrintf("[    FATAL ] [%d]\r\n", TestControl.test_count);
            TestFatalError();
        }
    }

    return ret;
}
/**
 *  @brief      Function to execute Number Comparison
 *  @param[in]  not_expected: expected unequal number
 *  @param[in]  actual: actual number
 *  @param[in]  fatal: Boolean indicating whether a test failure will report @ref TestFatalError
 *  @param[in]  file: string with file name
 *  @param[in]  line: line number
 *  @return     Boolean indicating the success of the operation
 *  @warning    Stronger function must be declared externally
 */
bool BinaryNotEqual(int32_t not_expected, int32_t actual, bool fatal, const char *file, int32_t line)
{
    bool ret;

    if (not_expected != actual)
    {
        TestPrintf("[       OK ] [%d] " RESULT ": %d (%d ms)\r\n", TestControl.test_count, actual, TestGetElapsedTime(TestControl.TestTimestamp));
        ret = true;
    }
    else
    {
        TestControl.error_count++;
        TestPrintf(FAIL " [%d] (%d ms)\r\n", TestControl.test_count, TestGetElapsedTime(TestControl.TestTimestamp));
        TestPrintf("  " FILE ": %s " LINE ": %d\r\n", file, line);
        TestPrintf("  " ACTUAL ": %d\r\n", actual);
        TestPrintf("  " NOT_EXPECTED ": %d\r\n", not_expected);
        ret = false;
        if (fatal)
        {
            TestPrintf("[    FATAL ] [%d]\r\n", TestControl.test_count);
            TestFatalError();
        }
    }

    return ret;
}

bool FloatNearEqual(float expected, float actual, float tolerance, bool fatal, const char *file, int32_t line)
{
    bool ret;

    if (fabs(expected - actual) <= tolerance)
    {
        TestPrintf("[       OK ] [%d] RESULT: %f vs %f (%d ms)\r\n", TestControl.test_count, actual, expected,
                   TestGetElapsedTime(TestControl.TestTimestamp));
        ret = true;
    }
    else
    {
        TestControl.error_count++;
        TestPrintf(FAIL " [%d] (%d ms)\r\n", TestControl.test_count, TestGetElapsedTime(TestControl.TestTimestamp));
        TestPrintf("  " FILE ": %s " LINE ": %d\r\n", file, line);
        TestPrintf("  " ACTUAL ": %f\r\n", actual);
        TestPrintf("  " EXPECTED ": %f\r\n", expected);
        ret = false;
        if (fatal)
        {
            TestPrintf("[    FATAL ] [%d]\r\n", TestControl.test_count);
            TestFatalError();
        }
    }

    return ret;
}

/**
 *  @brief      Function to execute Number Comparison
 *  @param[in]  greater_than: expected limit value
 *  @param[in]  actual: actual number
 *  @param[in]  fatal: Boolean indicating whether a test failure will report @ref TestFatalError
 *  @param[in]  file: string with file name
 *  @param[in]  line: line number
 *  @return     Boolean indicating the success of the operation
 *  @warning    Stronger function must be declared externally
 */
bool BinaryGreater(int32_t greater_than, int32_t actual, bool fatal, const char *file, int32_t line)
{
    bool ret;

    if (actual > greater_than)
    {
        TestPrintf("[       OK ] [%d] " RESULT ": %d (%d ms)\r\n", TestControl.test_count, actual, TestGetElapsedTime(TestControl.TestTimestamp));
        ret = true;
    }
    else
    {
        TestControl.error_count++;
        TestPrintf(FAIL " [%d] (%d ms)\r\n", TestControl.test_count, TestGetElapsedTime(TestControl.TestTimestamp));
        TestPrintf("  " FILE ": %s " LINE ": %d\r\n", file, line);
        TestPrintf("  " ACTUAL ": %d\r\n", actual);
        TestPrintf("  " GREATER_THAN ": %d\r\n", greater_than);
        ret = false;
        if (fatal)
        {
            TestPrintf("[    FATAL ] [%d]\r\n", TestControl.test_count);
            TestFatalError();
        }
    }

    return ret;
}
/**
 *  @brief      Function to execute String Comparison
 *  @param[in]  expected: pointer to expected string
 *  @param[in]  actual: pointer to actual string
 *  @param[in]  fatal: Boolean indicating whether a test failure will report @ref TestFatalError
 *  @param[in]  file: string with file name
 *  @param[in]  line: line number
 *  @return     Boolean indicating the success of the operation
 *  @warning    Stronger function must be declared externally
 */
bool StringEqual(char *expected, char *actual, bool fatal, const char *file, int32_t line)
{
    bool ret;

    if (!strcmp(expected, actual))
    {
        TestPrintf("[       OK ] [%d] " RESULT ": %s (%d ms)\r\n", TestControl.test_count, expected, TestGetElapsedTime(TestControl.TestTimestamp));
        ret = true;
    }
    else
    {
        TestControl.error_count++;
        TestPrintf(FAIL " [%d] (%d ms)\r\n", TestControl.test_count, TestGetElapsedTime(TestControl.TestTimestamp));
        TestPrintf("  " FILE ": %s " LINE ": %d\r\n", file, line);
        TestPrintf("  " ACTUAL ": %s\r\n", actual);
        TestPrintf("  " EXPECTED ": %s\r\n", expected);
        ret = false;
        if (fatal)
        {
            TestPrintf("[    FATAL ] [%d]\r\n", TestControl.test_count);
            TestFatalError();
        }
    }

    return ret;
}
/**
 * @brief       Compares two float values considering a tolerance provided by diff
 * @param[in]   expected: The target value
 * @param[in]   actual: The actual value
 * @param[in]   diff: The tolerance in the difference between the two values
 * @param[in]   fatal: Boolean indicating if the execution results in a FATAL condition
 * @param[in]   file: File of the comparison
 * @param[in]   line: Line of the comparison
 * @return      Boolean indicating success of the operation
 */
bool FloatEpsilonCompare(float expected, float actual, float diff, bool fatal, const char *file, int32_t line)
{
    bool ret;

    if ((fabs(expected - actual)) <= diff)
    {
        TestPrintf("[       OK ] [%d] " RESULT ": %f (%d ms)\r\n", TestControl.test_count, actual, TestGetElapsedTime(TestControl.TestTimestamp));
        ret = true;
    }
    else
    {
        TestControl.error_count++;
        TestPrintf(FAIL " [%d] (%d ms)\r\n", TestControl.test_count, TestGetElapsedTime(TestControl.TestTimestamp));
        TestPrintf("  " FILE ": %s " LINE ": %d\r\n", file, line);
        TestPrintf("  " ACTUAL ": %f\r\n", actual);
        TestPrintf("  " EXPECTED ": %f\r\n", expected);
        ret = false;
        if (fatal)
        {
            TestPrintf("[    FATAL ] [%d]\r\n", TestControl.test_count);
            TestFatalError();
        }
        ret = false;
    }

    return ret;
}

/**
 *  @brief      Function that calculates the elapsed time from an initial time
 *  @param[in]  InitialTime: Initial time for calculation in ms
 *  @return     Elapsed time from initial time in milliseconds
 *  @note       This function corrects the error caused by overflow
 */
uint32_t TestGetElapsedTime(uint32_t InitialTime)
{
    uint32_t actualTime;
    actualTime = TestGetTick();

    if (InitialTime <= actualTime)
        return (actualTime - InitialTime);
    else
        return ((UINT32_MAX - InitialTime) + actualTime);
}
/**
 *  @brief      Function that writes the test information
 *  @param[in]  data: pointer with the data to be written
 *  @param[in]  size: number of bytes to be written
 *  @return     Boolean indicating the success of the operation
 *  @warning    Stronger function must be declared externally
 */
__weak bool TestWrite(uint8_t *data, uint32_t size)
{
    return false;
}
/**
 *  @brief      Returns the current value of the RTOS tick timer in ms
 *  @return     Tick value
 *  @warning    Stronger function must be declared externally
 */
__weak uint32_t TestGetTick(void)
{
    return 0;
}
/**
 *  @brief      Function to free memory
 *  @param[in]  size: Size of the memory block, in bytes
 *  @return     On success, a pointer to the memory block allocated by the function. \n
 *              If the function failed to allocate the requested block of memory, a null pointer is returned.
 *  @warning    Stronger function can be declared externally
 */
__weak void *TestMalloc(uint32_t size)
{
    return malloc(size);
}
/**
 *  @brief      Function to free memory
 *  @param[in]  buff: Pointer to a memory block previously allocated with TestMalloc
 *  @warning    Stronger function can be declared externally
 */
__weak void TestFree(void *buff)
{
    free(buff);
}
/**
 *  @brief      Callback function for a fatal error occurrence in the test
 *  @warning    Stronger function can be declared externally
 */
__weak void TestFatalError(void)
{
}
