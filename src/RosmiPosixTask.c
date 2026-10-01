/*!
 * \file      RosmiPosixTask.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Wrapper for RosmiTask with POSIX threads
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiPosixTask ROSMI Posix Task
 *  \ingroup RosmiTask
 *  \details   RosmiPosixTask Wrapper Guide
 *   =========================================
 *
 *   =========================================
 *   Making this wrapper functional
 *   =========================================
 *  - Use a toolchain with a POSIX threads implementation \n
 *  - Build this source file in the project to override the RosmiTask weak
 *    functions \n
 *  - Link with -pthread \n
 *
 *   =========================================
 *   Porting to another platform
 *   =========================================
 *  Copy this file and replace only the two calls. On FreeRTOS,
 *  \ref RosmiThreadCreate becomes xTaskCreate and \ref RosmiThreadJoin becomes
 *  the notification or semaphore that signals the task has finished. Nothing
 *  in RosmiTask.c, RosmiLabel.c or RosmiMerge.c changes: the seam is only
 *  these two primitives, which is the smallest surface the platform actually
 *  imposes.
 * @{
 */
#include <pthread.h>
#include "RosmiTask.h"

/*! \addtogroup  RosmiPosixTaskPrivate ROSMI Posix Task Private
 *  \ingroup RosmiPosixTask
 * @{
 */

/*!
 *  \brief Descriptor kept behind the opaque \ref RosmiThread_t
 *  \details pthread_t is not guaranteed to fit in a pointer, so it is boxed
 *           here instead of being cast. The box also carries the entry point,
 *           which lets the trampoline bridge the two calling conventions.
 */
typedef struct RosmiPosixThreadDefinition
{
    pthread_t          handle; /**<Thread identifier of the system*/
    RosmiThreadEntry_f entry;  /**<Body to run*/
    void *             arg;    /**<Argument handed to the body*/
} RosmiPosixThread_t;

static void *RosmiPosixTrampoline(void *raw);

/** @}*/ // End of RosmiPosixTaskPrivate

/*!
 *  \overload bool RosmiThreadCreate(RosmiThreadEntry_f entry, void *arg, RosmiThread_t *thread)
 */
bool RosmiThreadCreate(RosmiThreadEntry_f entry, void *arg, RosmiThread_t *thread)
{
    bool                ret = false;
    RosmiPosixThread_t *box = NULL;

    do
    {
        if ((entry == NULL) || (thread == NULL))
        {
            break;
        }
        box = (RosmiPosixThread_t *)RosmiMalloc(sizeof(RosmiPosixThread_t));
        if (box == NULL)
        {
            break;
        }

        box->entry = entry;
        box->arg   = arg;

        if (pthread_create(&box->handle, NULL, RosmiPosixTrampoline, box) != 0)
        {
            break;
        }

        *thread = (RosmiThread_t)box;
        box     = NULL; /* ownership handed to the caller */
        ret     = true;
    } while (0);

    RosmiFree(box);
    return ret;
}
/*!
 *  \overload bool RosmiThreadJoin(RosmiThread_t thread)
 */
bool RosmiThreadJoin(RosmiThread_t thread)
{
    bool                ret = false;
    RosmiPosixThread_t *box = (RosmiPosixThread_t *)thread;

    if (box != NULL)
    {
        ret = (pthread_join(box->handle, NULL) == 0);
        RosmiFree(box);
    }
    return ret;
}
/*!
 *  \brief      Bridges the pthreads convention to \ref RosmiThreadEntry_f
 *  \param[in]  raw: Thread descriptor, as a void pointer
 *  \return     Always NULL; ROSMI uses no thread return value
 */
static void *RosmiPosixTrampoline(void *raw)
{
    RosmiPosixThread_t *box = (RosmiPosixThread_t *)raw;

    box->entry(box->arg);
    return NULL;
}

/** @}*/ // End of RosmiPosixTask
