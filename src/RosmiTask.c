/*!
 * \file      RosmiTask.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with the task layer of ROSMI
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "RosmiTask.h"

/** \weakgroup  RosmiTaskWeak ROSMI Task Weak
 *  \ingroup RosmiTask
 *  \details The whole platform dependency of the parallel version sits in
 *           these two functions. Build RosmiPosixTask.c to get pthreads, or
 *           write the equivalent for your RTOS; nothing else changes.
 * @{
 */

#ifndef __weak
#define __weak __attribute__((weak)) /**<weak attribute definition*/
#endif

__weak bool RosmiThreadCreate(RosmiThreadEntry_f entry, void *arg, RosmiThread_t *thread);
__weak bool RosmiThreadJoin(RosmiThread_t thread);

/** @}*/ // End of RosmiTaskWeak

/*! \addtogroup  RosmiTaskPrivate ROSMI Task Private
 *  \ingroup RosmiTask
 * @{
 */

/*!
 *  \brief Slice of the work handed to one worker
 *  \details A worker walks the segments s with (s % stride) == first. The
 *           fields are read only for the worker except through the output
 *           vectors of the configuration, where it writes only at its own
 *           indexes.
 */
typedef struct RosmiTaskWorkerDefinition
{
    const RosmiTaskConfig_t *config; /**<Shared work description*/
    int32_t                  first;  /**<Index of the first segment of the slice*/
    int32_t                  stride; /**<Distance between segments of the slice*/
} RosmiTaskWorker_t;

static void          RosmiTaskWorker(void *arg);
static void          RosmiTaskLabelOne(const RosmiTaskConfig_t *config, int32_t segment);
static RosmiReturn_e RosmiTaskValidate(const RosmiTaskConfig_t *config, int32_t threads);
static RosmiReturn_e RosmiTaskCollect(const RosmiTaskConfig_t *config);

/** @}*/ // End of RosmiTaskPrivate

/*!
 *  \brief      Labels every segment, spreading them over the given threads
 *  \details    Blocks until every segment has been processed. With threads
 *              equal to 1 no thread is created and the work runs in the
 *              caller, which gives an honest baseline for the cost of
 *              parallelism itself.
 *
 *              If a thread cannot be created, the remaining slices are run in
 *              the calling context rather than failing: a partial degradation
 *              to sequential still yields the right answer, which matters
 *              more here than reporting the shortage.
 *
 *              A failure inside a segment is recorded in results and
 *              propagated by the return, but the other segments are still
 *              processed so the diagnosis shows the whole picture instead of
 *              the first symptom.
 *  \param[in]  config: Work to perform
 *  \param[in]  threads: Thread count, >= 1. Values above the segment count are
 *              reduced to it, since there is no more independent work.
 *  \return     \ref ROSMI_RET_OK when every segment was labeled \n
 *              \ref ROSMI_INV_PARAM on an invalid configuration \n
 *              \ref ROSMI_MALLOC_ERROR when the worker descriptors do not fit \n
 *              the first failing segment code otherwise
 */
RosmiReturn_e RosmiTaskLabelSegments(const RosmiTaskConfig_t *config, int32_t threads)
{
    RosmiReturn_e      ret     = RosmiTaskValidate(config, threads);
    RosmiTaskWorker_t *workers = NULL;
    RosmiThread_t *    handles = NULL;
    int32_t            started = 0;

    do
    {
        if (ret != ROSMI_RET_OK)
        {
            break;
        }

        const int32_t segments = config->seg_rows * config->seg_cols;
        int32_t       wanted   = (threads > segments) ? segments : threads;

        workers = (RosmiTaskWorker_t *)RosmiMalloc((size_t)wanted * sizeof(RosmiTaskWorker_t));
        handles = (RosmiThread_t *)RosmiMalloc((size_t)wanted * sizeof(RosmiThread_t));
        if ((workers == NULL) || (handles == NULL))
        {
            ret = ROSMI_MALLOC_ERROR;
            break;
        }

        for (int32_t i = 0; i < wanted; i++)
        {
            workers[i].config = config;
            workers[i].first  = i;
            workers[i].stride = wanted;
        }

        /* Slice 0 stays with the caller, so a single thread run creates no
           thread at all and the caller never sits idle waiting. */
        for (int32_t i = 1; i < wanted; i++)
        {
            if (!RosmiThreadCreate(RosmiTaskWorker, &workers[i], &handles[started]))
            {
                /* No threading available, or the system refused: this slice
                   and the ones after it run here. */
                for (int32_t j = i; j < wanted; j++)
                {
                    RosmiTaskWorker(&workers[j]);
                }
                break;
            }
            started++;
        }

        RosmiTaskWorker(&workers[0]);

        for (int32_t i = 0; i < started; i++)
        {
            if (!RosmiThreadJoin(handles[i]))
            {
                ret = ROSMI_THREAD_ERROR;
            }
        }
        if (ret == ROSMI_RET_OK)
        {
            ret = RosmiTaskCollect(config);
        }
    } while (0);

    RosmiFree(workers);
    RosmiFree(handles);
    return ret;
}
/*!
 *  \brief      Body of a worker: walks its interleaved slice of segments
 *  \param[in]  arg: Pointer to the RosmiTaskWorker_t of this slice
 */
static void RosmiTaskWorker(void *arg)
{
    const RosmiTaskWorker_t *worker   = (const RosmiTaskWorker_t *)arg;
    const RosmiTaskConfig_t *config   = worker->config;
    const int32_t            segments = config->seg_rows * config->seg_cols;

    for (int32_t s = worker->first; s < segments; s += worker->stride)
    {
        RosmiTaskLabelOne(config, s);
    }
}
/*!
 *  \brief      Labels one segment and records its time and result
 *  \param[in]  config: Shared work description
 *  \param[in]  segment: Linear index of the segment
 */
static void RosmiTaskLabelOne(const RosmiTaskConfig_t *config, int32_t segment)
{
    const int32_t       row   = segment / config->seg_cols;
    const int32_t       col   = segment % config->seg_cols;
    const double        start = RosmiGetTimeSeconds();
    RosmiLabelRequest_t request;
    RosmiLabel_t        components = 0;

    request.image    = config->image;
    request.origin_y = row * config->tile_rows;
    request.origin_x = col * config->tile_cols;
    request.rows     = config->tile_rows;
    request.cols     = config->tile_cols;
    request.conn     = config->conn;

    const RosmiReturn_e ret = RosmiLabelSegment(&request, &config->borders[segment], &components);

    if (config->durations != NULL)
    {
        config->durations[segment] = RosmiGetTimeSeconds() - start;
    }
    if (config->results != NULL)
    {
        config->results[segment] = ret;
    }
}
/*!
 *  \brief      Validates the arguments of \ref RosmiTaskLabelSegments
 *  \param[in]  config: Configuration to validate
 *  \param[in]  threads: Requested thread count
 *  \return     \ref ROSMI_RET_OK or \ref ROSMI_INV_PARAM
 */
static RosmiReturn_e RosmiTaskValidate(const RosmiTaskConfig_t *config, int32_t threads)
{
    RosmiReturn_e ret = ROSMI_INV_PARAM;

    do
    {
        if ((config == NULL) || (config->image == NULL) || (config->borders == NULL))
        {
            break;
        }
        if ((config->seg_rows <= 0) || (config->seg_cols <= 0))
        {
            break;
        }
        if ((config->tile_rows <= 0) || (config->tile_cols <= 0))
        {
            break;
        }
        if (threads < 1)
        {
            break;
        }
        ret = ROSMI_RET_OK;
    } while (0);

    return ret;
}
/*!
 *  \brief      Reduces the per segment results to a single return code
 *  \param[in]  config: Configuration holding the results vector
 *  \return     \ref ROSMI_RET_OK when every segment succeeded, otherwise the
 *              code of the first failing one
 */
static RosmiReturn_e RosmiTaskCollect(const RosmiTaskConfig_t *config)
{
    RosmiReturn_e ret = ROSMI_RET_OK;

    if (config->results != NULL)
    {
        const int32_t segments = config->seg_rows * config->seg_cols;

        for (int32_t s = 0; (s < segments) && (ret == ROSMI_RET_OK); s++)
        {
            ret = config->results[s];
        }
    }
    return ret;
}
/*!
 *  \brief      Creates and starts a thread
 *  \details    Default implementation: refuses, so that a target without
 *              threading still works through the sequential fallback of
 *              \ref RosmiTaskLabelSegments. Override it in a port file.
 *  \param[in]  entry: Function to run
 *  \param[in]  arg: Argument handed to entry
 *  \param[out] thread: Receives the handle of the created thread
 *  \return     true when the thread is running, false otherwise
 */
__weak bool RosmiThreadCreate(RosmiThreadEntry_f entry, void *arg, RosmiThread_t *thread)
{
    (void)entry;
    (void)arg;
    (void)thread;
    return false;
}
/*!
 *  \brief      Waits for a thread to finish and releases its handle
 *  \details    Default implementation: nothing to wait for, since the default
 *              \ref RosmiThreadCreate never starts anything.
 *  \param[in]  thread: Handle returned by \ref RosmiThreadCreate
 *  \return     true when the thread has finished
 */
__weak bool RosmiThreadJoin(RosmiThread_t thread)
{
    (void)thread;
    return true;
}
