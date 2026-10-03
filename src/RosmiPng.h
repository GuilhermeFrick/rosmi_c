/*!
 * \file      RosmiPng.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with PNG reader and image loader by file extension
 * \version   1.0
 * \date      2026-10-03
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiPng ROSMI Png
 *  \ingroup Rosmi
 *  \details Lets both programs take the image of the assignment as it is
 *           delivered, a PNG, with no conversion step before them.
 *           Kept apart from \ref RosmiImage on purpose: it is the only module
 *           that depends on an external library (libpng), so the functional
 *           core still builds with nothing but a C compiler.
 *           A PNG is reduced to gray and binarized: a pixel darker than
 *           \ref ROSMI_PNG_THRESHOLD is an object, the rest is background.
 *           Transparent pixels are composed over white, so they count as
 *           background. This keeps the convention of \ref RosmiImage, where
 *           objects are black over a white background.
 * @{
 */
#ifndef ROSMI_PNG_H
#define ROSMI_PNG_H
#include "Rosmi.h"
#include "RosmiImage.h"

#ifdef __cplusplus
extern "C"
{
#endif

    /*!
     *  \brief Gray level that splits object from background
     *  \details Gray values below it, out of 0..255, are object pixels.
     */
    typedef enum RosmiPngThresholdDefinition
    {
        ROSMI_PNG_THRESHOLD = 128 /**<First gray value taken as background*/
    } RosmiPngThreshold_e;

    RosmiReturn_e RosmiImageLoadPng(RosmiImage_t *image, const char *path);
    RosmiReturn_e RosmiImageLoad(RosmiImage_t *image, const char *path);

#ifdef __cplusplus
}
#endif

#endif // ROSMI_PNG_H

/** @}*/ // End of RosmiPng
