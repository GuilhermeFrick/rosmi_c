/*!
 * \file      TestRosmiAlloc.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with the allocator used by the ROSMI test suite
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  TestRosmiAlloc ROSMI Test Allocator
 *  \ingroup TestRosmi
 *  \details This file provides the strong \ref RosmiMalloc and
 *           \ref RosmiFree that override the weak ones of Rosmi.c for the
 *           whole test binary. It exists for two reasons.
 *
 *           Fault injection
 *           ============================
 *           \ref TestAllocFailAfter makes the n-th following allocation fail.
 *           The out of memory paths are the hardest to reach by ordinary use
 *           and therefore the ones that usually stay uncovered in gcov; with
 *           a controllable allocator they become deterministic test cases.
 *
 *           Leak detection
 *           ============================
 *           The allocator counts every block handed out and every block
 *           returned, so \ref TestAllocOutstanding turns "did this function
 *           release everything on the error path" into an assertion instead
 *           of a hope.
 * @{
 */
#ifndef TEST_ROSMI_ALLOC_H
#define TEST_ROSMI_ALLOC_H
#include <stdint.h>

void    TestAllocFailAfter(int32_t successes);
void    TestAllocUnlimited(void);
int32_t TestAllocOutstanding(void);
void    TestAllocReset(void);

#endif // TEST_ROSMI_ALLOC_H

/** @}*/ // End of TestRosmiAlloc
