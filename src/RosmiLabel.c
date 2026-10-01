/*!
 * \file      RosmiLabel.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with connected component labeling
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "RosmiLabel.h"
#include <string.h>
#include "RosmiUnionFind.h"

/*! \addtogroup  RosmiLabelPrivate ROSMI Label Private
 *  \ingroup RosmiLabel
 * @{
 */

/*!
 *  \brief Scan state, grouped so the helper signatures stay short
 */
typedef struct RosmiScanDefinition
{
    const RosmiLabelRequest_t *request;  /**<Rectangle being processed*/
    RosmiUnionFind_t *         uf;       /**<Equivalences between labels*/
    RosmiLabel_t *             previous; /**<Labels of the previous row*/
    RosmiLabel_t *             current;  /**<Labels of the current row*/
} RosmiScan_t;

static inline RosmiLabel_t RosmiLabelAbsorb(RosmiUnionFind_t *uf, RosmiLabel_t current, RosmiLabel_t neighbour);
static inline RosmiLabel_t RosmiLabelInherit(RosmiScan_t *scan, int32_t y, int32_t x);
static RosmiReturn_e       RosmiLabelScanRow(RosmiScan_t *scan, int32_t y);
static void                RosmiLabelCapture(RosmiBorders_t *borders, const RosmiScan_t *scan, int32_t y);
static RosmiReturn_e       RosmiLabelCompact(RosmiUnionFind_t *uf, RosmiLabel_t **map, RosmiLabel_t *components);
static void                RosmiLabelRemap(RosmiLabel_t *vector, int32_t length, const RosmiLabel_t *map);
static RosmiReturn_e       RosmiLabelValidate(const RosmiLabelRequest_t *request, const RosmiBorders_t *borders);

/** @}*/ // End of RosmiLabelPrivate

/*!
 *  \brief      Allocates the border vectors of a segment
 *  \param[out] borders: Structure to prepare
 *  \param[in]  rows: Segment height, > 0
 *  \param[in]  cols: Segment width, > 0
 *  \return     \ref ROSMI_RET_OK, release with
 *              \ref RosmiBordersDeinitialize \n
 *              \ref ROSMI_INV_PARAM on NULL pointer or non positive dimension \n
 *              \ref ROSMI_MALLOC_ERROR when there is no memory
 */
RosmiReturn_e RosmiBordersInitialize(RosmiBorders_t *borders, int32_t rows, int32_t cols)
{
    RosmiReturn_e ret = ROSMI_INV_PARAM;

    do
    {
        if ((borders == NULL) || (rows <= 0) || (cols <= 0))
        {
            break;
        }

        const size_t  entries = (size_t)((2 * cols) + (2 * rows));
        RosmiLabel_t *storage = (RosmiLabel_t *)RosmiMalloc(entries * sizeof(RosmiLabel_t));

        if (storage == NULL)
        {
            ret = ROSMI_MALLOC_ERROR;
            break;
        }

        borders->rows       = rows;
        borders->cols       = cols;
        borders->components = 0;
        borders->storage    = storage;
        borders->top        = storage;
        borders->bottom     = storage + cols;
        borders->left       = storage + (2 * cols);
        borders->right      = storage + (2 * cols) + rows;
        ret                 = ROSMI_RET_OK;
    } while (0);

    return ret;
}
/*!
 *  \brief      Releases the border vectors
 *  \param[in]  borders: Structure to release. NULL is accepted and calling
 *              twice is safe.
 */
void RosmiBordersDeinitialize(RosmiBorders_t *borders)
{
    if (borders != NULL)
    {
        RosmiFree(borders->storage);
        borders->storage    = NULL;
        borders->top        = NULL;
        borders->bottom     = NULL;
        borders->left       = NULL;
        borders->right      = NULL;
        borders->components = 0;
        borders->rows       = 0;
        borders->cols       = 0;
    }
}
/*!
 *  \brief      Labels the rectangle and counts its connected components
 *  \details    Scans in raster order. Each object pixel inherits the label of
 *              the first already visited neighbour it finds and merges the
 *              remaining labeled neighbours in the union find, which is how
 *              two labels created separately turn out to be the same object
 *              further down the scan.
 *  \param[in]  request: Rectangle and connectivity
 *  \param[out] borders: Borders to fill, already initialized with the same
 *              dimensions as request, or NULL when only the count is wanted
 *  \param[out] components: Receives the number of local components
 *  \return     \ref ROSMI_RET_OK \n
 *              \ref ROSMI_INV_PARAM on NULL argument, non positive dimension,
 *              rectangle outside the image, invalid connectivity or borders
 *              with mismatched dimensions \n
 *              \ref ROSMI_MALLOC_ERROR when the temporaries cannot be allocated
 */
RosmiReturn_e RosmiLabelSegment(const RosmiLabelRequest_t *request, RosmiBorders_t *borders,
                                RosmiLabel_t *components)
{
    RosmiReturn_e    ret      = RosmiLabelValidate(request, borders);
    RosmiUnionFind_t uf       = {0};
    RosmiLabel_t *   lines    = NULL;
    RosmiLabel_t *   map      = NULL;
    bool             uf_ready = false;

    do
    {
        if (ret != ROSMI_RET_OK)
        {
            break;
        }
        if (components == NULL)
        {
            ret = ROSMI_INV_PARAM;
            break;
        }
        ret = RosmiUnionFindInitialize(&uf, 0);
        if (ret != ROSMI_RET_OK)
        {
            break;
        }
        uf_ready = true;

        /* Two contiguous rows: the previous one and the current one. */
        lines = (RosmiLabel_t *)RosmiMalloc((size_t)(2 * request->cols) * sizeof(RosmiLabel_t));
        if (lines == NULL)
        {
            ret = ROSMI_MALLOC_ERROR;
            break;
        }

        RosmiScan_t scan;

        scan.request  = request;
        scan.uf       = &uf;
        scan.previous = lines;
        scan.current  = lines + request->cols;

        for (int32_t x = 0; x < request->cols; x++)
        {
            scan.previous[x] = ROSMI_LABEL_BACKGROUND;
        }

        for (int32_t y = 0; y < request->rows; y++)
        {
            ret = RosmiLabelScanRow(&scan, y);
            if (ret != ROSMI_RET_OK)
            {
                break;
            }
            RosmiLabelCapture(borders, &scan, y);

            /* The current row becomes the previous one; the old buffer is
               fully rewritten on the next row, so it needs no clearing. */
            RosmiLabel_t *swap = scan.previous;

            scan.previous = scan.current;
            scan.current  = swap;
        }
        if (ret != ROSMI_RET_OK)
        {
            break;
        }
        ret = RosmiLabelCompact(&uf, &map, components);
        if (ret != ROSMI_RET_OK)
        {
            break;
        }
        if (borders != NULL)
        {
            borders->components = *components;
            RosmiLabelRemap(borders->top, borders->cols, map);
            RosmiLabelRemap(borders->bottom, borders->cols, map);
            RosmiLabelRemap(borders->left, borders->rows, map);
            RosmiLabelRemap(borders->right, borders->rows, map);
        }
    } while (0);

    RosmiFree(map);
    RosmiFree(lines);
    if (uf_ready)
    {
        RosmiUnionFindDeinitialize(&uf);
    }
    return ret;
}
/*!
 *  \brief      Absorbs a neighbour label into the label being formed
 *  \details    Concentrates in one place the four neighbourhood cases, which
 *              would otherwise become four nearly identical nested if blocks
 *              inside the innermost loop. The rule is: a background neighbour
 *              changes nothing; with no label yet, adopt the neighbour's; with
 *              a different label already, the two components are in fact the
 *              same and get united.
 *  \param[in,out] uf: Equivalences
 *  \param[in]  current: Label formed so far, or \ref ROSMI_LABEL_BACKGROUND
 *  \param[in]  neighbour: Neighbour label, or \ref ROSMI_LABEL_BACKGROUND
 *  \return     The label to adopt for the pixel
 */
static inline RosmiLabel_t RosmiLabelAbsorb(RosmiUnionFind_t *uf, RosmiLabel_t current, RosmiLabel_t neighbour)
{
    RosmiLabel_t result = current;

    if (neighbour != ROSMI_LABEL_BACKGROUND)
    {
        if (current == ROSMI_LABEL_BACKGROUND)
        {
            result = neighbour;
        }
        else
        {
            RosmiUnionFindUnite(uf, current, neighbour);
        }
    }
    return result;
}
/*!
 *  \brief      Derives the label of an object pixel from its neighbours
 *  \param[in,out] scan: Scan state
 *  \param[in]  y: Row inside the rectangle
 *  \param[in]  x: Column inside the rectangle
 *  \return     Inherited label, or \ref ROSMI_LABEL_BACKGROUND when no already
 *              visited neighbour is labeled, in which case the caller creates
 *              a new one
 */
static inline RosmiLabel_t RosmiLabelInherit(RosmiScan_t *scan, int32_t y, int32_t x)
{
    const int32_t cols     = scan->request->cols;
    const bool    diagonal = (scan->request->conn == ROSMI_CONN_8);
    RosmiLabel_t  label    = ROSMI_LABEL_BACKGROUND;

    if (x > 0) /* West */
    {
        label = RosmiLabelAbsorb(scan->uf, label, scan->current[x - 1]);
    }
    if (y > 0) /* North */
    {
        label = RosmiLabelAbsorb(scan->uf, label, scan->previous[x]);
    }
    if (diagonal && (y > 0) && (x > 0)) /* North West */
    {
        label = RosmiLabelAbsorb(scan->uf, label, scan->previous[x - 1]);
    }
    if (diagonal && (y > 0) && ((x + 1) < cols)) /* North East */
    {
        label = RosmiLabelAbsorb(scan->uf, label, scan->previous[x + 1]);
    }
    return label;
}
/*!
 *  \brief      Scans one row of the rectangle, filling scan->current
 *  \param[in,out] scan: Scan state
 *  \param[in]  y: Row inside the rectangle
 *  \return     \ref ROSMI_RET_OK, or \ref ROSMI_MALLOC_ERROR when a new label
 *              cannot be created
 */
static RosmiReturn_e RosmiLabelScanRow(RosmiScan_t *scan, int32_t y)
{
    const RosmiLabelRequest_t *request = scan->request;
    const int32_t              image_y = request->origin_y + y;
    RosmiReturn_e              ret     = ROSMI_RET_OK;

    for (int32_t x = 0; (x < request->cols) && (ret == ROSMI_RET_OK); x++)
    {
        if (RosmiImageAt(request->image, image_y, request->origin_x + x) == 0)
        {
            scan->current[x] = ROSMI_LABEL_BACKGROUND;
            continue;
        }

        RosmiLabel_t label = RosmiLabelInherit(scan, y, x);

        if (label == ROSMI_LABEL_BACKGROUND)
        {
            ret = RosmiUnionFindMakeSet(scan->uf, &label);
        }
        scan->current[x] = label;
    }
    return ret;
}
/*!
 *  \brief      Copies the borders of the current row into the output
 *  \param[out] borders: Borders, or NULL when they are not wanted
 *  \param[in]  scan: Scan state with the row already filled
 *  \param[in]  y: Row inside the rectangle
 */
static void RosmiLabelCapture(RosmiBorders_t *borders, const RosmiScan_t *scan, int32_t y)
{
    if (borders != NULL)
    {
        const int32_t cols  = scan->request->cols;
        const size_t  bytes = (size_t)cols * sizeof(RosmiLabel_t);

        if (y == 0)
        {
            memcpy(borders->top, scan->current, bytes);
        }
        if (y == (scan->request->rows - 1))
        {
            memcpy(borders->bottom, scan->current, bytes);
        }
        borders->left[y]  = scan->current[0];
        borders->right[y] = scan->current[cols - 1];
    }
}
/*!
 *  \brief      Builds the map taking provisional labels to [0, components)
 *  \details    Each union find root gets a sequential index and the others
 *              inherit the index of their root. Without this compaction the
 *              labels of a segment would have holes and the shift to the
 *              global space could not be a prefix sum.
 *  \param[in,out] uf: Equivalences already consolidated
 *  \param[out] map: Receives the allocated map, of uf->count entries
 *  \param[out] components: Receives the component count
 *  \return     \ref ROSMI_RET_OK, release the map with \ref RosmiFree \n
 *              \ref ROSMI_MALLOC_ERROR when there is no memory
 */
static RosmiReturn_e RosmiLabelCompact(RosmiUnionFind_t *uf, RosmiLabel_t **map, RosmiLabel_t *components)
{
    RosmiReturn_e      ret     = ROSMI_MALLOC_ERROR;
    const RosmiLabel_t total   = uf->count;
    const size_t       entries = (total > 0) ? (size_t)total : 1u;
    RosmiLabel_t *     table   = NULL;

    do
    {
        table = (RosmiLabel_t *)RosmiMalloc(entries * sizeof(RosmiLabel_t));
        if (table == NULL)
        {
            break;
        }

        RosmiLabel_t found = 0;

        for (RosmiLabel_t i = 0; i < total; i++)
        {
            table[i] = (RosmiUnionFindFind(uf, i) == i) ? found++ : ROSMI_LABEL_BACKGROUND;
        }
        for (RosmiLabel_t i = 0; i < total; i++)
        {
            if (table[i] == ROSMI_LABEL_BACKGROUND)
            {
                table[i] = table[RosmiUnionFindFind(uf, i)];
            }
        }

        *map        = table;
        *components = found;
        table       = NULL; /* ownership handed to the caller */
        ret         = ROSMI_RET_OK;
    } while (0);

    RosmiFree(table);
    return ret;
}
/*!
 *  \brief      Translates a border vector into the compact space
 *  \param[in,out] vector: Vector to translate; background stays untouched
 *  \param[in]  length: Elements of vector
 *  \param[in]  map: Map produced by RosmiLabelCompact
 */
static void RosmiLabelRemap(RosmiLabel_t *vector, int32_t length, const RosmiLabel_t *map)
{
    for (int32_t i = 0; i < length; i++)
    {
        if (vector[i] != ROSMI_LABEL_BACKGROUND)
        {
            vector[i] = map[vector[i]];
        }
    }
}
/*!
 *  \brief      Validates the arguments of \ref RosmiLabelSegment
 *  \param[in]  request: Request to validate
 *  \param[in]  borders: Associated borders, possibly NULL
 *  \return     \ref ROSMI_RET_OK or \ref ROSMI_INV_PARAM
 */
static RosmiReturn_e RosmiLabelValidate(const RosmiLabelRequest_t *request, const RosmiBorders_t *borders)
{
    RosmiReturn_e ret = ROSMI_INV_PARAM;

    do
    {
        if ((request == NULL) || (request->image == NULL) || (request->image->data == NULL))
        {
            break;
        }
        if ((request->rows <= 0) || (request->cols <= 0) || (request->origin_y < 0) || (request->origin_x < 0))
        {
            break;
        }
        if (((request->origin_y + request->rows) > request->image->height)
            || ((request->origin_x + request->cols) > request->image->width))
        {
            break;
        }
        if ((request->conn != ROSMI_CONN_4) && (request->conn != ROSMI_CONN_8))
        {
            break;
        }
        if ((borders != NULL) && ((borders->rows != request->rows) || (borders->cols != request->cols)))
        {
            break;
        }
        ret = ROSMI_RET_OK;
    } while (0);

    return ret;
}
