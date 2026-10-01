/**
 * @file uTest.h
 * @author Guilherme Frick de Oliveira (guilhermeoliveira@deltaglobal.com.br)
 * @brief Header file with functions that reproduce those used in the * GoogleTest API
 * @version 0.1
 * @date 2022-07-14
 *
 * @copyright Copyright (c) 2022
 *
 */

#ifndef _TEST_H_
#define _TEST_H_
#include <float.h>
#include <stdbool.h>
#include <stdint.h>
#include <string.h>
#ifdef __cplusplus
extern "C"
{
#endif

#ifdef __cplusplus
    extern "C"
    {
#endif
        void     TestPrintf(char *format, ...);
        void     TestSetUp(const char *test_case_name);
        void     TestTearDown(void);
        void     TestRunning(const char *func);
        bool     BinaryEqual(int32_t expected, int32_t actual, bool fatal, const char *file, int32_t line);
        bool     BinaryNotEqual(int32_t not_expected, int32_t actual, bool fatal, const char *file, int32_t line);
        bool     FloatNearEqual(float expected, float actual, float tolerance, bool fatal, const char *file, int32_t line);
        bool     BinaryGreater(int32_t greater_than, int32_t actual, bool fatal, const char *file, int32_t line);
        bool     FloatEpsilonCompare(float expected, float actual, float diff, bool fatal, const char *file, int32_t line);
        bool     StringEqual(char *expected, char *actual, bool fatal, const char *file, int32_t line);
        uint32_t TestGetTick(void);
        uint32_t TestGetElapsedTime(uint32_t InitialTime);
        void *   TestMalloc(uint32_t size);
        void     TestFree(void *buff);

// clang-format off

/**
 * @brief   Binary Comparison | Nonfatal Assertion
 *          Verifies (true == actual)
 */
#define EXPECT_TRUE(actual)                                                    \
  TestRunning(__func__);                                                       \
  BinaryEqual(true, (int32_t)actual, false, __FILE__, __LINE__)
/**
 * @brief   Binary Comparison | Nonfatal Assertion
 *          Verifies (false == actual)
 */
#define EXPECT_FALSE(actual)                                                   \
  TestRunning(__func__);                                                       \
  BinaryEqual(false, (int32_t)actual, false, __FILE__, __LINE__)
/**
 * @brief   Binary Comparison | Nonfatal Assertion
 *          Verifies (expected == actual)
 */
#define EXPECT_EQ(expected, actual)                                            \
  TestRunning(__func__);                                                       \
  BinaryEqual((int32_t)expected, (int32_t)actual, false, __FILE__, __LINE__)
/**
 * @brief   Binary Comparison | Nonfatal Assertion
 *          Verifies (expected != actual)
 */
#define EXPECT_NE(expected, actual)                                            \
  TestRunning(__func__);                                                       \
  BinaryNotEqual((int32_t)expected, (int32_t)actual, false, __FILE__, __LINE__)
/**
 * @brief   Binary Comparison | Nonfatal Assertion
 *          Verifies (expected != actual)
 */
#define EXPECT_GT(expected, actual)                                            \
  TestRunning(__func__);                                                       \
  BinaryGreater((int32_t)expected, (int32_t)actual, false, __FILE__, __LINE__)

/**
 * @brief   Binary Comparison | Nonfatal Assertion
 *          Verifies (expected == actual)
 */
#define EXPECT_FLOAT_EQ(expected, actual)                                      \
  TestRunning(__func__);                                                       \
  FloatEpsilonCompare((float)expected, (float)actual, (float)FLT_EPSILON,      \
                      false, __FILE__, __LINE__);
/**
 * @brief   Binary Comparison | Fatal Assertion
 *          Verifies (expected == actual)
 */
#define ASSERT_EQ(expected, actual)                                            \
  TestRunning(__func__);                                                       \
  BinaryEqual((int32_t)expected, (int32_t)actual, true, __FILE__, __LINE__)
/**
 * @brief   Binary Comparison | Fatal Assertion
 *          Verifies (expected != actual)
 */
#define ASSERT_NE(expected, actual)                                            \
  TestRunning(__func__);                                                       \
  BinaryNotEqual((int32_t)expected, (int32_t)actual, true, __FILE__, __LINE__) 

#define ASSERT_NEAR(expected, actual, tolerance) \
    FloatNearEqual((expected), (actual), (tolerance), true, __FILE__, __LINE__)
/**
 * @brief   String Comparison | Nonfatal Assertion
 *          Verifies that the two C strings have the same content
 */
#define EXPECT_STREQ(expected, actual)                                         \
  TestRunning(__func__);                                                       \
  StringEqual(expected, actual, false, __FILE__, __LINE__)
/**
 * @brief   String Comparison | Fatal Assertion
 *          Verifies that the two C strings have the same content
 */
#define ASSERT_STREQ(expected, actual)                                         \
  TestRunning(__func__);                                                       \
  StringEqual(expected, actual, true, __FILE__, __LINE__)
    
#ifdef __cplusplus
}
#endif

/**
 * @brief     Set up a new test
 */
#define SetUp() TestSetUp(__func__)
/**
 * @brief   Tear down current test
 */
#define TearDown() TestTearDown()

//#define TEST(test_case_name, test_name)  void test_case_name(void){SetUp();}
// void test_name(void)

#ifdef __cplusplus
}
#endif
#endif //_TEST_H_
