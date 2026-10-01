/*!
 * \file      RosmiMerge.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with reconciliation of components across segments
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiMerge ROSMI Merge
 *  \ingroup Rosmi
 *  \details This is the step that satisfies the central requirement of the
 *           assignment: an object crossing several segments must be counted
 *           **once**. Without it, summing the local counts counts the same
 *           object as many times as the segments it touches, which in the
 *           cases of this work is a 38% error.
 *
 *           The module only consumes border vectors and never touches pixels.
 *           That is why inter tile communication is cheap: what travels is a
 *           row of labels, not a slice of image.
 * @{
 */
#ifndef ROSMI_MERGE_H
#define ROSMI_MERGE_H
#include <stdint.h>
#include "Rosmi.h"
#include "RosmiLabel.h"
#include "RosmiUnionFind.h"

#ifdef __cplusplus
extern "C"
{
#endif

    /*!
     *  \brief Description of the set of segments to reconcile
     */
    typedef struct RosmiMergeRequestDefinition
    {
        int32_t               seg_rows; /**<Segments down, N*/
        int32_t               seg_cols; /**<Segments across, M*/
        RosmiConnectivity_e   conn;     /**<Neighbourhood criterion*/
        const RosmiBorders_t *borders;  /**<N*M borders, in linear order (r * M + c)*/
        const RosmiLabel_t *  offsets;  /**<N*M+1 offsets taking each segment
                                            local space to the global space*/
    } RosmiMergeRequest_t;

    /*!
     *  \brief Accounting of the reconciliation step
     *  \details Feeds the communication characterisation used in the NoC
     *           model: bytes is the volume that would cross the network in a
     *           distributed implementation, where each segment sends its
     *           border to the neighbour.
     */
    typedef struct RosmiMergeStatsDefinition
    {
        int64_t bytes;  /**<Border bytes exchanged between neighbours*/
        int64_t unions; /**<Equivalence pairs applied*/
        int64_t edges;  /**<Neighbouring segment pairs compared*/
    } RosmiMergeStats_t;

    RosmiReturn_e RosmiMergeOffsets(const RosmiBorders_t *borders, int32_t count, RosmiLabel_t *offsets);
    RosmiReturn_e RosmiMergeRun(const RosmiMergeRequest_t *request, RosmiUnionFind_t *uf,
                                RosmiMergeStats_t *stats);

#ifdef __cplusplus
}
#endif

#endif // ROSMI_MERGE_H

/** @}*/ // End of RosmiMerge
