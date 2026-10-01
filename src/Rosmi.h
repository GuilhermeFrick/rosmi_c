/*!
 * \file      Rosmi.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with common types of ROSMI component
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  Rosmi ROSMI
 *  \details   ROSMI Component Guide
 *            ============================
 *
 * ROSMI (Reconhecedor de Objetos por Segmentacao em Multiplas Imagens) counts
 * the objects of a binary image split into a matrix of N x M segments of
 * K x L pixels. An object is a set of contiguous pixels and must be counted
 * once, even when it crosses several segments.
 *
 * ============================
 * How to build this component
 * ============================
 * Sequential only
 * -------------------------
 *     - Build Rosmi.c, RosmiImage.c, RosmiUnionFind.c, RosmiLabel.c and
 *       RosmiMerge.c. Nothing else is required: the weak
 *       \ref RosmiThreadCreate refuses to create threads and \ref RosmiTask
 *       falls back to running every segment in the calling context.
 *
 * POSIX (Linux)
 * -------------------------
 *     - Add RosmiTask.c and RosmiPosixTask.c to the project. The second file
 *       overrides \ref RosmiThreadCreate and \ref RosmiThreadJoin with a
 *       pthreads implementation.
 *
 * FreeRTOS
 * -------------------------
 *     - Add RosmiTask.c and a RosmiFreeRtosTask.c of your own overriding
 *       \ref RosmiThreadCreate with xTaskCreate and \ref RosmiThreadJoin with
 *       the notification of your choice. No other file changes.
 *
 * Custom allocator
 * -------------------------
 *     - Override \ref RosmiMalloc and \ref RosmiFree
 *
 * ============================
 * How to use this component
 * ============================
 *     - Call \ref RosmiImageLoadPbm to read the image
 *     - Call \ref RosmiLabelSegment once over the whole image for the
 *       sequential answer
 *     - Call \ref RosmiTaskLabelSegments, \ref RosmiMergeOffsets and
 *       \ref RosmiMergeRun for the parallel answer
 * @{
 */
#ifndef ROSMI_H
#define ROSMI_H
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C"
{
#endif

    /*!
     *  \brief Return codes of the functions
     */
    typedef enum RosmiReturnDefinition
    {
        ROSMI_RET_OK       = 0,  /**<The function executed successfully*/
        ROSMI_ERROR        = -1, /**<Generic error occurred*/
        ROSMI_INV_PARAM    = -2, /**<Parameter is invalid*/
        ROSMI_MALLOC_ERROR = -3, /**<Insufficient memory for malloc*/
        ROSMI_IO_ERROR     = -4, /**<Error accessing the file*/
        ROSMI_FORMAT_ERROR = -5, /**<File content is malformed*/
        ROSMI_THREAD_ERROR = -6  /**<Error creating or joining a thread*/
    } RosmiReturn_e;

    /*!
     *  \brief Neighbourhood criterion used to decide whether two pixels touch
     *  \details Two pixels linked only by a diagonal are one object under
     *           \ref ROSMI_CONN_8 and two objects under \ref ROSMI_CONN_4, so
     *           the choice changes the answer. The values match the number of
     *           neighbours on purpose, which lets a command line option be
     *           validated against the enumeration directly.
     */
    typedef enum RosmiConnectivityDefinition
    {
        ROSMI_CONN_4 = 4, /**<North, South, East and West only*/
        ROSMI_CONN_8 = 8  /**<Also the four diagonals*/
    } RosmiConnectivity_e;

    /*!
     *  \brief  Label of a connected component
     *  \details A dedicated type, and not a bare int, because labels travel in
     *           two distinct spaces along the algorithm: the local one of a
     *           segment and the global one of the image. Mixing them is the
     *           easiest mistake to make.
     */
    typedef int32_t RosmiLabel_t;

    /*!
     *  \brief  Linear index of a segment inside the N x M matrix
     */
    typedef int32_t RosmiSegment_t;

    /*!
     *  \brief  Label value that marks a background pixel
     *  \details Outside the valid label range, which is always >= 0.
     */
    static const RosmiLabel_t ROSMI_LABEL_BACKGROUND = -1;

    const char *RosmiReturnStr(RosmiReturn_e ret);

    void *RosmiMalloc(size_t wanted_size);
    void  RosmiFree(void *buffer);
    double RosmiGetTimeSeconds(void);
    int32_t RosmiGetCpuCount(void);

#ifdef __cplusplus
}
#endif

#endif // ROSMI_H

/** @}*/ // End of Rosmi
