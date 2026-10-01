/*!
 * \file      RosmiTask.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with the task layer that spreads segments over threads
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiTask ROSMI Task
 *  \ingroup Rosmi
 *  \details This is the only module of ROSMI that knows threads exist. The
 *           functional modules, \ref RosmiLabel and \ref RosmiMerge, are
 *           sequential and hold no reference to concurrency, which keeps them
 *           testable in isolation and reusable by the sequential program
 *           unchanged.
 *
 *           Platform seam
 *           ============================
 *           Only two primitives are weak, \ref RosmiThreadCreate and
 *           \ref RosmiThreadJoin, and they are as thin as the platform
 *           actually makes them: a create and a join. Everything else in this
 *           module, including how the work is split and how the results are
 *           collected, is portable and stays strong.
 *
 *           The default create refuses to start a thread and returns false.
 *           That is not a failure mode: \ref RosmiTaskLabelSegments falls back
 *           to running every segment in the calling context, so a target with
 *           no threading still produces the right answer with no extra file.
 *           Adding RosmiPosixTask.c turns the same code multithreaded on
 *           Linux; a RosmiFreeRtosTask.c wrapping xTaskCreate would do the
 *           same on FreeRTOS.
 *
 *           Static split, no lock
 *           ============================
 *           Tasks do not go through a mutex protected queue. Each thread t of
 *           a total P processes the segments whose index satisfies
 *           (s % P) == t, decided before any thread starts.
 *
 *           The choice rests on a measured property: the cost of a tile is
 *           dominated by the scan of K*L pixels, identical for every segment
 *           whatever it contains. The measured correlation between the time
 *           of a tile and the object density of its segment is +0.08, that
 *           is, none. With the load already balanced by construction, a
 *           dynamic queue would only add contention.
 *
 *           The split is interleaved rather than in contiguous blocks, to
 *           dilute any spatial gradient the image may have. Since each thread
 *           writes only to its own positions of the output vectors, there is
 *           no sharing and no critical section is needed.
 * @{
 */
#ifndef ROSMI_TASK_H
#define ROSMI_TASK_H
#include "Rosmi.h"
#include "RosmiImage.h"
#include "RosmiLabel.h"

#ifdef __cplusplus
extern "C"
{
#endif

    /*!
     *  \brief Opaque handle of a thread created by the port
     *  \details A void pointer so that any platform can box whatever it needs
     *           behind it: a pthread_t on POSIX, a TaskHandle_t on FreeRTOS.
     */
    typedef void *RosmiThread_t;

    /*!
     *  \brief Body of a thread
     *  \param[in] arg: Argument passed through untouched by
     *             \ref RosmiThreadCreate
     */
    typedef void (*RosmiThreadEntry_f)(void *arg);

    /*!
     *  \brief Work to spread: label every segment of an image
     *  \details The three output vectors have seg_rows*seg_cols positions and
     *           are indexed by (row * seg_cols + col).
     */
    typedef struct RosmiTaskConfigDefinition
    {
        const RosmiImage_t *image;     /**<Complete image*/
        int32_t             seg_rows;  /**<Segments down, N*/
        int32_t             seg_cols;  /**<Segments across, M*/
        int32_t             tile_rows; /**<Height of a segment, K*/
        int32_t             tile_cols; /**<Width of a segment, L*/
        RosmiConnectivity_e conn;      /**<Neighbourhood criterion*/
        RosmiBorders_t *    borders;   /**<Output: borders of each segment,
                                           already initialized as K x L*/
        double *            durations; /**<Output: seconds per segment, or NULL*/
        RosmiReturn_e *     results;   /**<Output: return code per segment, or NULL*/
    } RosmiTaskConfig_t;

    bool RosmiThreadCreate(RosmiThreadEntry_f entry, void *arg, RosmiThread_t *thread);
    bool RosmiThreadJoin(RosmiThread_t thread);

    RosmiReturn_e RosmiTaskLabelSegments(const RosmiTaskConfig_t *config, int32_t threads);

#ifdef __cplusplus
}
#endif

#endif // ROSMI_TASK_H

/** @}*/ // End of RosmiTask
