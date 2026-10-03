/*!
 * \file      Rosmi.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with common services of ROSMI component
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
/* -std=c11 hides POSIX; clock_gettime and sysconf need it exposed */
#define _POSIX_C_SOURCE 200809L

#include "Rosmi.h"
#include <stdlib.h>
#include <time.h>

#if defined(_WIN32)
#include <windows.h>
#else
#include <unistd.h>
#endif

/** \weakgroup  RosmiWeak ROSMI Weak
 *  \ingroup Rosmi
 *  \details These are the only points where ROSMI touches the platform. Each
 *           one has a minimal default here and is meant to be overridden by a
 *           strong definition in a port file, exactly as RosmiPosixTask.c does
 *           for the threading primitives.
 * @{
 */

#ifndef __weak
#define __weak __attribute__((weak)) /**<weak attribute definition*/
#endif

__weak void *  RosmiMalloc(size_t wanted_size);
__weak void    RosmiFree(void *buffer);
__weak double  RosmiGetTimeSeconds(void);
__weak int32_t RosmiGetCpuCount(void);

/** @}*/ // End of RosmiWeak

/*!
 *  \brief      Textual description of a return code
 *  \param[in]  ret: Code to describe
 *  \return     Constant string, never NULL, valid for the whole execution
 */
const char *RosmiReturnStr(RosmiReturn_e ret)
{
    const char *str = "unknown";

    switch (ret)
    {
        case ROSMI_RET_OK:
            str = "ok";
            break;
        case ROSMI_ERROR:
            str = "generic error";
            break;
        case ROSMI_INV_PARAM:
            str = "invalid parameter";
            break;
        case ROSMI_MALLOC_ERROR:
            str = "out of memory";
            break;
        case ROSMI_IO_ERROR:
            str = "io error";
            break;
        case ROSMI_FORMAT_ERROR:
            str = "malformed file";
            break;
        case ROSMI_THREAD_ERROR:
            str = "thread error";
            break;
        default:
            break;
    }
    return str;
}
/*!
 *  \brief      Allocates a block of size bytes of memory
 *  \param[in]  wanted_size: Size of the memory block, in bytes
 *  \return     On success, a pointer to the memory block allocated. \n
 *              If the allocation failed, a null pointer is returned.
 */
__weak void *RosmiMalloc(size_t wanted_size)
{
    void *buffer = NULL;

    /* malloc(0) is implementation defined; normalising to NULL keeps every
       caller down to a single pointer test. */
    if (wanted_size > 0u)
    {
        buffer = malloc(wanted_size);
    }
    return buffer;
}
/*!
 *  \brief      Free memory allocated on the buffer pointer
 *  \param[in]  buffer: pointer to a memory block previously allocated
 */
__weak void RosmiFree(void *buffer)
{
    free(buffer);
}
/*!
 *  \brief   Current reading of a monotonic clock, in seconds
 *  \details Measures intervals, not wall clock time. The origin is arbitrary;
 *           only the difference between two readings has meaning.
 *  \return  Seconds since an unspecified origin
 */
__weak double RosmiGetTimeSeconds(void)
{
    double seconds = 0.0;

#if defined(_WIN32)
    LARGE_INTEGER frequency;
    LARGE_INTEGER counter;

    if ((QueryPerformanceFrequency(&frequency) != 0) && (QueryPerformanceCounter(&counter) != 0))
    {
        seconds = (double)counter.QuadPart / (double)frequency.QuadPart;
    }
#else
    struct timespec now;

    if (clock_gettime(CLOCK_MONOTONIC, &now) == 0)
    {
        seconds = (double)now.tv_sec + ((double)now.tv_nsec * 1e-9);
    }
#endif
    return seconds;
}
/*!
 *  \brief   Number of processors available to run threads
 *  \return  Processor count, always >= 1
 */
__weak int32_t RosmiGetCpuCount(void)
{
    int32_t count = 1;

#if defined(_WIN32)
    SYSTEM_INFO info;

    GetSystemInfo(&info);
    if (info.dwNumberOfProcessors > 0u)
    {
        count = (int32_t)info.dwNumberOfProcessors;
    }
#else
    long online = sysconf(_SC_NPROCESSORS_ONLN);

    if (online > 0L)
    {
        count = (int32_t)online;
    }
#endif
    return count;
}
