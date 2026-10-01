/*!
 * \file      TestRosmiFixture.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with helpers to build images inside the test suite
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "TestRosmiFixture.h"
#include <string.h>
#include "Rosmi.h"

/*!
 *  \brief      Builds a bit packed image from an array of text rows
 *  \details    Every row must have the same length. A '#' becomes an object
 *              pixel and any other character becomes background.
 *  \param[out] image: Receives the image; release with \ref TestImageDestroy
 *  \param[in]  rows: Array of strings, one per pixel row
 *  \param[in]  count: Number of rows
 *  \return     true on success, false on a bad argument or allocation failure
 */
bool TestImageFromRows(RosmiImage_t *image, const char *const *rows, int32_t count)
{
    bool ret = false;

    do
    {
        if ((image == NULL) || (rows == NULL) || (count <= 0))
        {
            break;
        }

        const int32_t width = (int32_t)strlen(rows[0]);

        if (width <= 0)
        {
            break;
        }

        const int32_t stride = (width + 7) / 8;
        uint8_t *     data   = (uint8_t *)RosmiMalloc((size_t)stride * (size_t)count);

        if (data == NULL)
        {
            break;
        }
        memset(data, 0, (size_t)stride * (size_t)count);

        for (int32_t y = 0; y < count; y++)
        {
            for (int32_t x = 0; x < width; x++)
            {
                if (rows[y][x] == '#')
                {
                    data[((size_t)y * (size_t)stride) + (size_t)(x >> 3)] |= (uint8_t)(1u << (7 - (x & 7)));
                }
            }
        }

        image->width  = width;
        image->height = count;
        image->stride = stride;
        image->data   = data;
        ret           = true;
    } while (0);

    return ret;
}
/*!
 *  \brief      Releases an image built by \ref TestImageFromRows
 *  \param[in]  image: Image to release
 */
void TestImageDestroy(RosmiImage_t *image)
{
    RosmiImageRelease(image);
}
