/*!
 * \file      RosmiReport.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with CSV result and trace writing
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiReport ROSMI Report
 *  \ingroup Rosmi
 *  \details Writes the measurement artefacts consumed downstream: a results
 *           row per run, and the per tile and per edge traces that feed the
 *           NoC characterisation in CAFES.
 * @{
 */
#ifndef ROSMI_REPORT_H
#define ROSMI_REPORT_H
#include "Rosmi.h"

#ifdef __cplusplus
extern "C"
{
#endif

    /*!
     *  \brief One row of the consolidated results file
     */
    typedef struct RosmiReportRowDefinition
    {
        const char *version;      /**<"seq" or "par"*/
        const char *image;        /**<Image path*/
        int32_t     height;       /**<Image height in pixels*/
        int32_t     width;        /**<Image width in pixels*/
        int32_t     seg_rows;     /**<Segments down, N*/
        int32_t     seg_cols;     /**<Segments across, M*/
        int32_t     tile_rows;    /**<Segment height, K*/
        int32_t     tile_cols;    /**<Segment width, L*/
        int32_t     conn;         /**<Connectivity*/
        int32_t     threads;      /**<Threads used*/
        int32_t     objects;      /**<Objects counted*/
        int32_t     naive_sum;    /**<Sum of the local counts*/
        double      seconds;      /**<Wall time of the run*/
    } RosmiReportRow_t;

    RosmiReturn_e RosmiReportAppendCsv(const char *path, const RosmiReportRow_t *row);
    RosmiReturn_e RosmiReportWriteTiles(const char *prefix, int32_t seg_rows, int32_t seg_cols,
                                        int32_t tile_rows, int32_t tile_cols,
                                        const RosmiLabel_t *components, const double *durations);
    RosmiReturn_e RosmiReportWriteEdges(const char *prefix, int32_t seg_rows, int32_t seg_cols,
                                        int32_t tile_rows, int32_t tile_cols);

#ifdef __cplusplus
}
#endif

#endif // ROSMI_REPORT_H

/** @}*/ // End of RosmiReport
