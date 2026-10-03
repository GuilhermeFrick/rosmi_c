/*!
 * \file      RosmiPng.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with PNG reader and image loader by file extension
 * \version   1.0
 * \date      2026-10-03
 * \copyright Copyright (c) 2026
 */
#include "RosmiPng.h"
#include <ctype.h>
#include <png.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

/*! \addtogroup  RosmiPngPrivate ROSMI Png Private
 *  \ingroup RosmiPng
 * @{
 */

/*!
 *  \brief Limits of the PNG reader
 */
typedef enum RosmiPngLimitDefinition
{
    ROSMI_PNG_DIM_MAX = 1000000 /**<Largest accepted pixel count per dimension, as for PBM*/
} RosmiPngLimit_e;

static bool RosmiPngIsPngPath(const char *path);
static void RosmiPngPack(const uint8_t *gray, int32_t width, int32_t height, uint8_t *data);

/** @}*/ // End of RosmiPngPrivate

/*!
 *  \brief      Loads a PNG file from disk and binarizes it
 *  \details    Any PNG flavour is accepted (gray, RGB, palette, with or
 *              without alpha, 8 or 16 bits): libpng converts it to 8 bit gray
 *              first, then each pixel is compared against
 *              \ref ROSMI_PNG_THRESHOLD.
 *  \param[out] image: Receives the image; untouched when the call fails
 *  \param[in]  path: File path
 *  \return     \ref ROSMI_RET_OK on success, release with
 *              \ref RosmiImageRelease \n
 *              \ref ROSMI_INV_PARAM when image or path is NULL \n
 *              \ref ROSMI_IO_ERROR when the file cannot be opened \n
 *              \ref ROSMI_FORMAT_ERROR when it is not a valid PNG or the
 *              dimensions are out of range \n
 *              \ref ROSMI_MALLOC_ERROR when there is no memory for the content
 */
RosmiReturn_e RosmiImageLoadPng(RosmiImage_t *image, const char *path)
{
    static const png_color white = {255u, 255u, 255u};
    RosmiReturn_e          ret   = ROSMI_INV_PARAM;
    FILE *                 file  = NULL;
    uint8_t *              gray  = NULL;
    uint8_t *              data  = NULL;
    png_image              png;

    (void)memset(&png, 0, sizeof(png));
    png.version = PNG_IMAGE_VERSION;

    do
    {
        if ((image == NULL) || (path == NULL))
        {
            break;
        }
        file = fopen(path, "rb");
        if (file == NULL)
        {
            ret = ROSMI_IO_ERROR;
            break;
        }
        ret = ROSMI_FORMAT_ERROR;
        if (png_image_begin_read_from_stdio(&png, file) == 0)
        {
            break;
        }
        if ((png.width == 0u) || (png.height == 0u) || (png.width > (png_uint_32)ROSMI_PNG_DIM_MAX) ||
            (png.height > (png_uint_32)ROSMI_PNG_DIM_MAX))
        {
            break;
        }
        png.format = PNG_FORMAT_GRAY;

        const int32_t width  = (int32_t)png.width;
        const int32_t height = (int32_t)png.height;
        const int32_t stride = (width + 7) / 8;
        const size_t  bytes  = (size_t)stride * (size_t)height;

        gray = (uint8_t *)RosmiMalloc((size_t)width * (size_t)height);
        data = (uint8_t *)RosmiMalloc(bytes);
        if ((gray == NULL) || (data == NULL))
        {
            ret = ROSMI_MALLOC_ERROR;
            break;
        }
        if (png_image_finish_read(&png, &white, gray, 0, NULL) == 0)
        {
            break;
        }
        RosmiPngPack(gray, width, height, data);

        image->width  = width;
        image->height = height;
        image->stride = stride;
        image->data   = data;
        data          = NULL; /* ownership handed to the caller */
        ret           = ROSMI_RET_OK;
    } while (0);

    png_image_free(&png); /* no-op when libpng already released it */
    RosmiFree(gray);
    RosmiFree(data);
    if (file != NULL)
    {
        (void)fclose(file);
    }
    return ret;
}
/*!
 *  \brief      Loads an image choosing the reader by the file extension
 *  \details    A path ending in ".png", in any letter case, goes to
 *              \ref RosmiImageLoadPng; anything else to
 *              \ref RosmiImageLoadPbm.
 *  \param[out] image: Receives the image; untouched when the call fails
 *  \param[in]  path: File path
 *  \return     The return of the chosen reader, or \ref ROSMI_INV_PARAM
 *              when path is NULL
 */
RosmiReturn_e RosmiImageLoad(RosmiImage_t *image, const char *path)
{
    RosmiReturn_e ret = ROSMI_INV_PARAM;

    if (path != NULL)
    {
        ret = RosmiPngIsPngPath(path) ? RosmiImageLoadPng(image, path) : RosmiImageLoadPbm(image, path);
    }
    return ret;
}
/*!
 *  \brief      Tells whether a path ends in ".png", ignoring letter case
 *  \param[in]  path: File path
 *  \return     true for a PNG path
 */
static bool RosmiPngIsPngPath(const char *path)
{
    static const char ext[]  = ".png";
    const size_t      length = strlen(path);
    const size_t      size   = sizeof(ext) - 1u;
    bool              ret    = (length >= size);

    for (size_t i = 0u; ret && (i < size); i++)
    {
        ret = (tolower((unsigned char)path[length - size + i]) == (int)ext[i]);
    }
    return ret;
}
/*!
 *  \brief      Packs an 8 bit gray buffer into the bit layout of RosmiImage
 *  \param[in]  gray: width * height gray values, row by row
 *  \param[in]  width: Width in pixels
 *  \param[in]  height: Height in pixels
 *  \param[out] data: height * ceil(width/8) bytes, most significant bit first
 */
static void RosmiPngPack(const uint8_t *gray, int32_t width, int32_t height, uint8_t *data)
{
    const int32_t stride = (width + 7) / 8;

    (void)memset(data, 0, (size_t)stride * (size_t)height);
    for (int32_t y = 0; y < height; y++)
    {
        const uint8_t *in  = &gray[(size_t)y * (size_t)width];
        uint8_t *      out = &data[(size_t)y * (size_t)stride];

        for (int32_t x = 0; x < width; x++)
        {
            if (in[x] < (uint8_t)ROSMI_PNG_THRESHOLD)
            {
                out[x >> 3] = (uint8_t)(out[x >> 3] | (0x80u >> (x & 7)));
            }
        }
    }
}
