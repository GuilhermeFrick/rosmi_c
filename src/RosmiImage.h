/*!
 * \file      RosmiImage.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with bit packed binary image and PBM reader
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiImage ROSMI Image
 *  \ingroup Rosmi
 *  \details Project convention, the same as the assignment: bit 1 is an
 *           object pixel and bit 0 is background. That is also the standard
 *           P4 semantics, where bit 1 is black, so objects show up black over
 *           a white background in any viewer.
 *
 *           Pixels are packed 8 per byte. For the 7x7 segmentation of
 *           768x1024 tiles that turns 38 MB into 4.8 MB, which matters on an
 *           embedded target.
 * @{
 */
#ifndef ROSMI_IMAGE_H
#define ROSMI_IMAGE_H
#include <stdint.h>
#include "Rosmi.h"

#ifdef __cplusplus
extern "C"
{
#endif

    /*!
     *  \brief Binary image held in memory
     *  \details Read only for users; only \ref RosmiImageLoadPbm and
     *           \ref RosmiImageRelease change the fields.
     */
    typedef struct RosmiImageDefinition
    {
        int32_t  width;  /**<Width in pixels*/
        int32_t  height; /**<Height in pixels*/
        int32_t  stride; /**<Bytes per row, equal to ceil(width/8)*/
        uint8_t *data;   /**<height * stride bytes, most significant bit first*/
    } RosmiImage_t;

    /*!
     *  \brief      Value of one pixel
     *  \param[in]  image: Loaded image
     *  \param[in]  y: Row, in [0, image->height)
     *  \param[in]  x: Column, in [0, image->width)
     *  \return     1 for an object pixel, 0 for background
     *  \warning    Indexes are not validated. This runs once per pixel in the
     *              innermost loop of the algorithm and a test per call would
     *              dominate the cost. Every loop that calls it derives its
     *              bounds from the image dimensions.
     */
    static inline int32_t RosmiImageAt(const RosmiImage_t *image, int32_t y, int32_t x)
    {
        /* x >> 3 is x/8, the byte holding the pixel; x & 7 is x%8, the bit
           inside that byte counted from the most significant one. */
        const uint8_t byte = image->data[((size_t)y * (size_t)image->stride) + (size_t)(x >> 3)];

        return (int32_t)((byte >> (7 - (x & 7))) & 1u);
    }

    RosmiReturn_e RosmiImageLoadPbm(RosmiImage_t *image, const char *path);
    void          RosmiImageRelease(RosmiImage_t *image);

#ifdef __cplusplus
}
#endif

#endif // ROSMI_IMAGE_H

/** @}*/ // End of RosmiImage
