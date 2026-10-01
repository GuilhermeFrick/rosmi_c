/*!
 * \file      RosmiImage.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with binary PBM (P4) reader
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "RosmiImage.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/*! \addtogroup  RosmiImagePrivate ROSMI Image Private
 *  \ingroup RosmiImage
 * @{
 */

/*!
 *  \brief Limits of the PBM header parser
 *  \details Declared as an enumeration, and not as static const, because the
 *           token size must be a compile time constant to dimension an
 *           automatic array without resorting to a variable length array.
 */
typedef enum RosmiPbmLimitDefinition
{
    ROSMI_PBM_TOKEN_MAX = 32,     /**<Longest header token, terminator included*/
    ROSMI_PBM_DIM_MAX   = 1000000 /**<Largest accepted pixel count per dimension*/
} RosmiPbmLimit_e;

static RosmiReturn_e RosmiPbmToken(FILE *file, char *buffer, size_t size);
static RosmiReturn_e RosmiPbmDimension(const char *token, int32_t *value);
static RosmiReturn_e RosmiPbmHeader(FILE *file, int32_t *width, int32_t *height);

/** @}*/ // End of RosmiImagePrivate

/*!
 *  \brief      Loads a binary PBM (P4) file from disk
 *  \param[out] image: Receives the image; untouched when the call fails
 *  \param[in]  path: File path
 *  \return     \ref ROSMI_RET_OK on success, release with
 *              \ref RosmiImageRelease \n
 *              \ref ROSMI_INV_PARAM when image or path is NULL \n
 *              \ref ROSMI_IO_ERROR when the file cannot be opened \n
 *              \ref ROSMI_FORMAT_ERROR when it is not P4, the dimensions are
 *              invalid or the pixel data is truncated \n
 *              \ref ROSMI_MALLOC_ERROR when there is no memory for the content
 */
RosmiReturn_e RosmiImageLoadPbm(RosmiImage_t *image, const char *path)
{
    RosmiReturn_e ret    = ROSMI_INV_PARAM;
    FILE *        file   = NULL;
    uint8_t *     data   = NULL;
    int32_t       width  = 0;
    int32_t       height = 0;

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
        ret = RosmiPbmHeader(file, &width, &height);
        if (ret != ROSMI_RET_OK)
        {
            break;
        }

        const int32_t stride = (width + 7) / 8;
        const size_t  bytes  = (size_t)stride * (size_t)height;

        data = (uint8_t *)RosmiMalloc(bytes);
        if (data == NULL)
        {
            ret = ROSMI_MALLOC_ERROR;
            break;
        }
        if (fread(data, 1u, bytes, file) != bytes)
        {
            ret = ROSMI_FORMAT_ERROR;
            break;
        }

        image->width  = width;
        image->height = height;
        image->stride = stride;
        image->data   = data;
        data          = NULL; /* ownership handed to the caller */
        ret           = ROSMI_RET_OK;
    } while (0);

    RosmiFree(data);
    if (file != NULL)
    {
        (void)fclose(file);
    }
    return ret;
}
/*!
 *  \brief      Releases the image memory and leaves it empty
 *  \param[in]  image: Image to release. NULL is accepted and calling twice is
 *              safe.
 */
void RosmiImageRelease(RosmiImage_t *image)
{
    if (image != NULL)
    {
        RosmiFree(image->data);
        image->data   = NULL;
        image->width  = 0;
        image->height = 0;
        image->stride = 0;
    }
}
/*!
 *  \brief      Reads one header token, skipping blanks and comments
 *  \details    A comment starts at '#' and runs to the end of the line. The
 *              blank that ends the token is consumed, which is exactly the
 *              single separator the format requires before the binary data,
 *              so returning from the height token leaves the stream on the
 *              first pixel byte.
 *  \param[in]  file: File positioned on the header
 *  \param[out] buffer: Receives the token, terminated with '\0'
 *  \param[in]  size: Capacity of buffer
 *  \return     \ref ROSMI_RET_OK or \ref ROSMI_FORMAT_ERROR on premature end
 *              of file or oversized token
 */
static RosmiReturn_e RosmiPbmToken(FILE *file, char *buffer, size_t size)
{
    RosmiReturn_e ret  = ROSMI_FORMAT_ERROR;
    size_t        used = 0u;
    int           c    = 0;

    while (ret == ROSMI_FORMAT_ERROR)
    {
        c = fgetc(file);
        if (c == EOF)
        {
            break;
        }
        if (c == '#')
        {
            while ((c != '\n') && (c != EOF))
            {
                c = fgetc(file);
            }
            continue;
        }
        if ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r'))
        {
            if (used > 0u)
            {
                ret = ROSMI_RET_OK;
            }
            continue;
        }
        if ((used + 1u) >= size)
        {
            break;
        }
        buffer[used] = (char)c;
        used++;
    }

    if (ret == ROSMI_RET_OK)
    {
        buffer[used] = '\0';
    }
    return ret;
}
/*!
 *  \brief      Converts a header token into a valid dimension
 *  \param[in]  token: Token text
 *  \param[out] value: Receives the dimension
 *  \return     \ref ROSMI_RET_OK when the value is in
 *              [1, \ref ROSMI_PBM_DIM_MAX], \ref ROSMI_FORMAT_ERROR otherwise
 */
static RosmiReturn_e RosmiPbmDimension(const char *token, int32_t *value)
{
    RosmiReturn_e ret    = ROSMI_FORMAT_ERROR;
    char *        end    = NULL;
    long          parsed = strtol(token, &end, 10);

    if ((end != token) && (*end == '\0') && (parsed > 0L) && (parsed <= (long)ROSMI_PBM_DIM_MAX))
    {
        *value = (int32_t)parsed;
        ret    = ROSMI_RET_OK;
    }
    return ret;
}
/*!
 *  \brief      Reads the header and validates signature and dimensions
 *  \param[in]  file: Freshly opened file
 *  \param[out] width: Receives the width
 *  \param[out] height: Receives the height
 *  \return     \ref ROSMI_RET_OK or \ref ROSMI_FORMAT_ERROR
 */
static RosmiReturn_e RosmiPbmHeader(FILE *file, int32_t *width, int32_t *height)
{
    RosmiReturn_e ret = ROSMI_FORMAT_ERROR;
    char          token[ROSMI_PBM_TOKEN_MAX];

    do
    {
        if (RosmiPbmToken(file, token, (size_t)ROSMI_PBM_TOKEN_MAX) != ROSMI_RET_OK)
        {
            break;
        }
        if (strcmp(token, "P4") != 0)
        {
            break;
        }
        if (RosmiPbmToken(file, token, (size_t)ROSMI_PBM_TOKEN_MAX) != ROSMI_RET_OK)
        {
            break;
        }
        if (RosmiPbmDimension(token, width) != ROSMI_RET_OK)
        {
            break;
        }
        if (RosmiPbmToken(file, token, (size_t)ROSMI_PBM_TOKEN_MAX) != ROSMI_RET_OK)
        {
            break;
        }
        if (RosmiPbmDimension(token, height) != ROSMI_RET_OK)
        {
            break;
        }
        ret = ROSMI_RET_OK;
    } while (0);

    return ret;
}
