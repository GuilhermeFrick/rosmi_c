/*!
 * \file      RosmiLabel.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with connected component labeling of one segment
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiLabel ROSMI Label
 *  \ingroup Rosmi
 *  \details Connected Component Labeling, the recognition core.
 *
 *           It is **the same** in both versions of the application: the
 *           sequential one calls it once with the rectangle equal to the
 *           whole image, the parallel one calls it once per segment. There
 *           are not two algorithms, there is one called two ways.
 *
 *           Memory cost
 *           ============================
 *           The scan keeps only **two rows** of labels, not the whole labeled
 *           image: the cost is O(L) and not O(K*L). For a 768x1024 segment
 *           that is the difference between 1024 and 786432 labels. It is not
 *           a cosmetic saving, it is what makes the algorithm implementable
 *           on a tile with small local memory, which is the target here.
 * @{
 */
#ifndef ROSMI_LABEL_H
#define ROSMI_LABEL_H
#include "Rosmi.h"
#include "RosmiImage.h"

#ifdef __cplusplus
extern "C"
{
#endif

    /*!
     *  \brief Labels of the four borders of a segment, already compacted
     *  \details This is all a segment has to publish so that its neighbours
     *           can discover which components continue across the borders.
     *           The interior of the segment never leaves it, which is why
     *           inter tile communication is cheap.
     *
     *           Labels are in [0, components), with
     *           \ref ROSMI_LABEL_BACKGROUND where there is background. The
     *           four vectors are views over a single allocation, so a segment
     *           costs one malloc instead of four.
     */
    typedef struct RosmiBordersDefinition
    {
        int32_t       rows;       /**<Segment height, K*/
        int32_t       cols;       /**<Segment width, L*/
        RosmiLabel_t  components; /**<Local components found*/
        RosmiLabel_t *top;        /**<First row, cols elements*/
        RosmiLabel_t *bottom;     /**<Last row, cols elements*/
        RosmiLabel_t *left;       /**<First column, rows elements*/
        RosmiLabel_t *right;      /**<Last column, rows elements*/
        RosmiLabel_t *storage;    /**<Single block backing the four views*/
    } RosmiBorders_t;

    /*!
     *  \brief Description of the rectangle to label
     */
    typedef struct RosmiLabelRequestDefinition
    {
        const RosmiImage_t *image;    /**<Source image*/
        int32_t             origin_y; /**<First row of the rectangle*/
        int32_t             origin_x; /**<First column of the rectangle*/
        int32_t             rows;     /**<Rectangle height, K*/
        int32_t             cols;     /**<Rectangle width, L*/
        RosmiConnectivity_e conn;     /**<Neighbourhood criterion*/
    } RosmiLabelRequest_t;

    RosmiReturn_e RosmiBordersInitialize(RosmiBorders_t *borders, int32_t rows, int32_t cols);
    void          RosmiBordersDeinitialize(RosmiBorders_t *borders);
    RosmiReturn_e RosmiLabelSegment(const RosmiLabelRequest_t *request, RosmiBorders_t *borders,
                                    RosmiLabel_t *components);

#ifdef __cplusplus
}
#endif

#endif // ROSMI_LABEL_H

/** @}*/ // End of RosmiLabel
