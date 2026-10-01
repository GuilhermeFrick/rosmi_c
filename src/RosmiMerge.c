/*!
 * \file      RosmiMerge.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with reconciliation of components across segments
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "RosmiMerge.h"

/*! \addtogroup  RosmiMergePrivate ROSMI Merge Private
 *  \ingroup RosmiMerge
 * @{
 */

/*!
 *  \brief Context shared by the border sweeps
 */
typedef struct RosmiMergeContextDefinition
{
    const RosmiMergeRequest_t *request; /**<Request being processed*/
    RosmiUnionFind_t *         uf;      /**<Global equivalences*/
    RosmiMergeStats_t          stats;   /**<Accumulated accounting*/
} RosmiMergeContext_t;

static inline RosmiSegment_t RosmiMergeIndex(const RosmiMergeRequest_t *request, int32_t row, int32_t col);
static inline void RosmiMergePair(RosmiMergeContext_t *ctx, RosmiSegment_t seg_a, RosmiLabel_t label_a,
                                  RosmiSegment_t seg_b, RosmiLabel_t label_b);
static void        RosmiMergeHorizontal(RosmiMergeContext_t *ctx);
static void        RosmiMergeVertical(RosmiMergeContext_t *ctx);
static void        RosmiMergeCorners(RosmiMergeContext_t *ctx);

/** @}*/ // End of RosmiMergePrivate

/*!
 *  \brief      Computes the offsets taking local labels to the global space
 *  \details    Prefix sum over the component count of each segment. Segment s
 *              comes to occupy [offsets[s], offsets[s+1]), so labels of
 *              different segments stop colliding.
 *  \param[in]  borders: count borders already labeled
 *  \param[in]  count: Number of segments, N*M
 *  \param[out] offsets: count+1 positions to fill
 *  \return     \ref ROSMI_RET_OK or \ref ROSMI_INV_PARAM
 */
RosmiReturn_e RosmiMergeOffsets(const RosmiBorders_t *borders, int32_t count, RosmiLabel_t *offsets)
{
    RosmiReturn_e ret = ROSMI_INV_PARAM;

    do
    {
        if ((borders == NULL) || (offsets == NULL) || (count <= 0))
        {
            break;
        }

        offsets[0] = 0;
        for (int32_t i = 0; i < count; i++)
        {
            offsets[i + 1] = offsets[i] + borders[i].components;
        }
        ret = ROSMI_RET_OK;
    } while (0);

    return ret;
}
/*!
 *  \brief      Unites, in the global space, components touching across borders
 *  \details    Compares the right border of each segment with the left border
 *              of the neighbour to the east, and the bottom with the top of
 *              the neighbour to the south. Under \ref ROSMI_CONN_8 it also
 *              handles the diagonal pairs at the crossings of four segments.
 *  \param[in]  request: Segments, borders and offsets
 *  \param[in,out] uf: Union find already populated with one element per local
 *              component, in the global space
 *  \param[out] stats: Accounting of the step, or NULL
 *  \return     \ref ROSMI_RET_OK or \ref ROSMI_INV_PARAM
 */
RosmiReturn_e RosmiMergeRun(const RosmiMergeRequest_t *request, RosmiUnionFind_t *uf, RosmiMergeStats_t *stats)
{
    RosmiReturn_e       ret = ROSMI_INV_PARAM;
    RosmiMergeContext_t ctx = {0};

    do
    {
        if ((request == NULL) || (uf == NULL) || (request->borders == NULL) || (request->offsets == NULL))
        {
            break;
        }
        if ((request->seg_rows <= 0) || (request->seg_cols <= 0))
        {
            break;
        }
        if ((request->conn != ROSMI_CONN_4) && (request->conn != ROSMI_CONN_8))
        {
            break;
        }

        ctx.request = request;
        ctx.uf      = uf;

        RosmiMergeHorizontal(&ctx);
        RosmiMergeVertical(&ctx);
        RosmiMergeCorners(&ctx);

        if (stats != NULL)
        {
            *stats = ctx.stats;
        }
        ret = ROSMI_RET_OK;
    } while (0);

    return ret;
}
/*!
 *  \brief      Linear index of segment (row, col)
 *  \param[in]  request: Request, source of the column count
 *  \param[in]  row: Segment row
 *  \param[in]  col: Segment column
 *  \return     Index in [0, seg_rows*seg_cols)
 */
static inline RosmiSegment_t RosmiMergeIndex(const RosmiMergeRequest_t *request, int32_t row, int32_t col)
{
    return (RosmiSegment_t)((row * request->seg_cols) + col);
}
/*!
 *  \brief      Unites two local labels, each one in its own segment
 *  \details    Converts both to the global space by adding the offset of the
 *              respective segment. Pairs where either side is background are
 *              ignored, which is the common case since most of a border has
 *              no object on it.
 *  \param[in,out] ctx: Context, whose accounting is updated
 *  \param[in]  seg_a: Segment of the first label
 *  \param[in]  label_a: Local label in the first segment
 *  \param[in]  seg_b: Segment of the second label
 *  \param[in]  label_b: Local label in the second segment
 */
static inline void RosmiMergePair(RosmiMergeContext_t *ctx, RosmiSegment_t seg_a, RosmiLabel_t label_a,
                                  RosmiSegment_t seg_b, RosmiLabel_t label_b)
{
    if ((label_a != ROSMI_LABEL_BACKGROUND) && (label_b != ROSMI_LABEL_BACKGROUND))
    {
        const RosmiLabel_t *offsets = ctx->request->offsets;

        RosmiUnionFindUnite(ctx->uf, offsets[seg_a] + label_a, offsets[seg_b] + label_b);
        ctx->stats.unions++;
    }
}
/*!
 *  \brief      Reconciles each segment with its neighbour to the east
 *  \param[in,out] ctx: Reconciliation context
 */
static void RosmiMergeHorizontal(RosmiMergeContext_t *ctx)
{
    const RosmiMergeRequest_t *request  = ctx->request;
    const bool                 diagonal = (request->conn == ROSMI_CONN_8);

    for (int32_t row = 0; row < request->seg_rows; row++)
    {
        for (int32_t col = 0; (col + 1) < request->seg_cols; col++)
        {
            const RosmiSegment_t a     = RosmiMergeIndex(request, row, col);
            const RosmiSegment_t b     = RosmiMergeIndex(request, row, col + 1);
            const RosmiLabel_t * right = request->borders[a].right;
            const RosmiLabel_t * left  = request->borders[b].left;
            const int32_t        rows  = request->borders[a].rows;

            ctx->stats.bytes += (int64_t)rows * (int64_t)sizeof(RosmiLabel_t);
            ctx->stats.edges++;

            for (int32_t y = 0; y < rows; y++)
            {
                RosmiMergePair(ctx, a, right[y], b, left[y]);
                if (diagonal)
                {
                    if (y > 0)
                    {
                        RosmiMergePair(ctx, a, right[y], b, left[y - 1]);
                    }
                    if ((y + 1) < rows)
                    {
                        RosmiMergePair(ctx, a, right[y], b, left[y + 1]);
                    }
                }
            }
        }
    }
}
/*!
 *  \brief      Reconciles each segment with its neighbour to the south
 *  \param[in,out] ctx: Reconciliation context
 */
static void RosmiMergeVertical(RosmiMergeContext_t *ctx)
{
    const RosmiMergeRequest_t *request  = ctx->request;
    const bool                 diagonal = (request->conn == ROSMI_CONN_8);

    for (int32_t row = 0; (row + 1) < request->seg_rows; row++)
    {
        for (int32_t col = 0; col < request->seg_cols; col++)
        {
            const RosmiSegment_t a      = RosmiMergeIndex(request, row, col);
            const RosmiSegment_t b      = RosmiMergeIndex(request, row + 1, col);
            const RosmiLabel_t * bottom = request->borders[a].bottom;
            const RosmiLabel_t * top    = request->borders[b].top;
            const int32_t        cols   = request->borders[a].cols;

            ctx->stats.bytes += (int64_t)cols * (int64_t)sizeof(RosmiLabel_t);
            ctx->stats.edges++;

            for (int32_t x = 0; x < cols; x++)
            {
                RosmiMergePair(ctx, a, bottom[x], b, top[x]);
                if (diagonal)
                {
                    if (x > 0)
                    {
                        RosmiMergePair(ctx, a, bottom[x], b, top[x - 1]);
                    }
                    if ((x + 1) < cols)
                    {
                        RosmiMergePair(ctx, a, bottom[x], b, top[x + 1]);
                    }
                }
            }
        }
    }
}
/*!
 *  \brief      Handles the diagonal pairs at crossings of four segments
 *  \details    Under connectivity 8 there is a case neither the horizontal
 *              nor the vertical sweep reaches. Where four segments meet, the
 *              corner pixel of one segment is a diagonal neighbour of the
 *              corner pixel of the segment **diagonally opposite** to it, and
 *              that pair crosses two borders at once rather than one. It is
 *              the natural blind spot of any merge implementation, and
 *              ignoring it produces wrong counts.
 *  \param[in,out] ctx: Reconciliation context
 */
static void RosmiMergeCorners(RosmiMergeContext_t *ctx)
{
    const RosmiMergeRequest_t *request = ctx->request;

    do
    {
        if (request->conn != ROSMI_CONN_8)
        {
            break;
        }

        for (int32_t row = 0; (row + 1) < request->seg_rows; row++)
        {
            for (int32_t col = 0; (col + 1) < request->seg_cols; col++)
            {
                const RosmiSegment_t nw   = RosmiMergeIndex(request, row, col);
                const RosmiSegment_t ne   = RosmiMergeIndex(request, row, col + 1);
                const RosmiSegment_t sw   = RosmiMergeIndex(request, row + 1, col);
                const RosmiSegment_t se   = RosmiMergeIndex(request, row + 1, col + 1);
                const int32_t        cols = request->borders[nw].cols;

                /* Diagonal "\": bottom right corner of NW with top left of SE */
                RosmiMergePair(ctx, nw, request->borders[nw].bottom[cols - 1], se, request->borders[se].top[0]);
                /* Diagonal "/": bottom left corner of NE with top right of SW */
                RosmiMergePair(ctx, ne, request->borders[ne].bottom[0], sw, request->borders[sw].top[cols - 1]);

                ctx->stats.bytes += 2 * (int64_t)sizeof(RosmiLabel_t);
            }
        }
    } while (0);
}
