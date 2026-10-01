/*!
 * \file      TestRosmiAlloc.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Wrapper for Rosmi with a controllable test allocator
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "TestRosmiAlloc.h"
#include <stdatomic.h>
#include <stdlib.h>
#include "Rosmi.h"

/*! \addtogroup  TestRosmiAllocPrivate ROSMI Test Allocator Private
 *  \ingroup TestRosmiAlloc
 * @{
 */

/*!
 *  \brief Value of the budget meaning "never fail"
 */
static const int32_t TEST_ALLOC_UNLIMITED = -1;

/*
 * Both counters are atomic because the parallel tests call the allocator from
 * several threads at once. With plain int32_t the read-modify-write of the
 * outstanding counter races and updates are lost, which showed up as phantom
 * leaks that scaled with the thread count. The product code has no such race:
 * it is the bookkeeping of this test double that needed the guarantee.
 */
static _Atomic int32_t test_alloc_budget      = -1; /**<Allocations left before failing*/
static _Atomic int32_t test_alloc_outstanding = 0;  /**<Blocks handed out and not freed*/

/** @}*/ // End of TestRosmiAllocPrivate

/*!
 *  \brief      Makes the allocation after the given number of successes fail
 *  \param[in]  successes: How many allocations still succeed. Zero makes the
 *              very next one fail.
 */
void TestAllocFailAfter(int32_t successes)
{
    atomic_store(&test_alloc_budget, successes);
}
/*!
 *  \brief      Restores the allocator to never failing
 */
void TestAllocUnlimited(void)
{
    test_alloc_budget = TEST_ALLOC_UNLIMITED;
}
/*!
 *  \brief      Blocks handed out and not yet returned
 *  \return     Outstanding block count; zero after a clean run
 */
int32_t TestAllocOutstanding(void)
{
    return atomic_load(&test_alloc_outstanding);
}
/*!
 *  \brief      Clears the budget and the outstanding counter
 */
void TestAllocReset(void)
{
    atomic_store(&test_alloc_budget, TEST_ALLOC_UNLIMITED);
    atomic_store(&test_alloc_outstanding, 0);
}
/*!
 *  \overload void *RosmiMalloc(size_t wanted_size)
 */
void *RosmiMalloc(size_t wanted_size)
{
    void *buffer = NULL;

    do
    {
        int32_t budget = atomic_load(&test_alloc_budget);

        if (budget == 0)
        {
            atomic_store(&test_alloc_budget, TEST_ALLOC_UNLIMITED); /* one shot */
            break;
        }
        if (budget > 0)
        {
            (void)atomic_fetch_sub(&test_alloc_budget, 1);
        }
        if (wanted_size == 0u)
        {
            break;
        }
        buffer = malloc(wanted_size);
        if (buffer != NULL)
        {
            (void)atomic_fetch_add(&test_alloc_outstanding, 1);
        }
    } while (0);

    return buffer;
}
/*!
 *  \overload void RosmiFree(void *buffer)
 */
void RosmiFree(void *buffer)
{
    if (buffer != NULL)
    {
        (void)atomic_fetch_sub(&test_alloc_outstanding, 1);
        free(buffer);
    }
}
