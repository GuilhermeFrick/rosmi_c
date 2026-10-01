/*!
 * \file      RosmiUnionFind.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with disjoint set structure
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "RosmiUnionFind.h"
#include <string.h>

/*! \addtogroup  RosmiUnionFindPrivate ROSMI Union Find Private
 *  \ingroup RosmiUnionFind
 * @{
 */

/*!
 *  \brief Minimum capacity, to avoid a burst of early reallocations
 */
static const RosmiLabel_t ROSMI_UF_CAPACITY_MIN = 64;

static inline bool  RosmiUnionFindIsValid(const RosmiUnionFind_t *uf, RosmiLabel_t label);
static RosmiReturn_e RosmiUnionFindGrow(RosmiUnionFind_t *uf);

/** @}*/ // End of RosmiUnionFindPrivate

/*!
 *  \brief      Prepares the structure for use
 *  \param[out] uf: Structure to initialize
 *  \param[in]  capacity: Desired initial capacity; small values are raised to
 *              an internal minimum
 *  \return     \ref ROSMI_RET_OK, release with
 *              \ref RosmiUnionFindDeinitialize \n
 *              \ref ROSMI_INV_PARAM when uf is NULL or capacity is negative \n
 *              \ref ROSMI_MALLOC_ERROR when there is no memory
 */
RosmiReturn_e RosmiUnionFindInitialize(RosmiUnionFind_t *uf, RosmiLabel_t capacity)
{
    RosmiReturn_e ret    = ROSMI_INV_PARAM;
    RosmiLabel_t *parent = NULL;
    RosmiLabel_t *weight = NULL;

    do
    {
        if ((uf == NULL) || (capacity < 0))
        {
            break;
        }

        const RosmiLabel_t wanted = (capacity < ROSMI_UF_CAPACITY_MIN) ? ROSMI_UF_CAPACITY_MIN : capacity;
        const size_t       bytes  = (size_t)wanted * sizeof(RosmiLabel_t);

        parent = (RosmiLabel_t *)RosmiMalloc(bytes);
        weight = (RosmiLabel_t *)RosmiMalloc(bytes);
        if ((parent == NULL) || (weight == NULL))
        {
            ret = ROSMI_MALLOC_ERROR;
            break;
        }

        uf->parent   = parent;
        uf->weight   = weight;
        uf->count    = 0;
        uf->capacity = wanted;
        parent       = NULL; /* ownership handed to the structure */
        weight       = NULL;
        ret          = ROSMI_RET_OK;
    } while (0);

    RosmiFree(parent);
    RosmiFree(weight);
    return ret;
}
/*!
 *  \brief      Releases the structure and leaves it empty
 *  \param[in]  uf: Structure to release. NULL is accepted and calling twice is
 *              safe.
 */
void RosmiUnionFindDeinitialize(RosmiUnionFind_t *uf)
{
    if (uf != NULL)
    {
        RosmiFree(uf->parent);
        RosmiFree(uf->weight);
        uf->parent   = NULL;
        uf->weight   = NULL;
        uf->count    = 0;
        uf->capacity = 0;
    }
}
/*!
 *  \brief      Creates a singleton set
 *  \param[in,out] uf: Initialized structure
 *  \param[out] label: Receives the created label
 *  \return     \ref ROSMI_RET_OK \n
 *              \ref ROSMI_INV_PARAM on a NULL argument \n
 *              \ref ROSMI_MALLOC_ERROR when the capacity cannot grow
 */
RosmiReturn_e RosmiUnionFindMakeSet(RosmiUnionFind_t *uf, RosmiLabel_t *label)
{
    RosmiReturn_e ret = ROSMI_INV_PARAM;

    do
    {
        if ((uf == NULL) || (uf->parent == NULL) || (label == NULL))
        {
            break;
        }
        if (uf->count == uf->capacity)
        {
            ret = RosmiUnionFindGrow(uf);
            if (ret != ROSMI_RET_OK)
            {
                break;
            }
        }

        uf->parent[uf->count] = uf->count;
        uf->weight[uf->count] = 1;
        *label                = uf->count;
        uf->count++;
        ret = ROSMI_RET_OK;
    } while (0);

    return ret;
}
/*!
 *  \brief      Root of the set containing the label
 *  \details    Applies path halving: every element visited starts pointing to
 *              its grandparent, which halves the chain at no extra cost.
 *  \param[in,out] uf: Initialized structure
 *  \param[in]  label: Valid label, in [0, uf->count)
 *  \return     The root, or \ref ROSMI_LABEL_BACKGROUND when the label is
 *              invalid
 */
RosmiLabel_t RosmiUnionFindFind(RosmiUnionFind_t *uf, RosmiLabel_t label)
{
    RosmiLabel_t node = ROSMI_LABEL_BACKGROUND;

    if (RosmiUnionFindIsValid(uf, label))
    {
        node = label;
        while (uf->parent[node] != node)
        {
            uf->parent[node] = uf->parent[uf->parent[node]]; /* path halving */
            node             = uf->parent[node];
        }
    }
    return node;
}
/*!
 *  \brief      Merges the sets of two labels
 *  \details    The smaller tree points to the larger one, which prevents long
 *              chains. Uniting labels already in the same set does nothing,
 *              and invalid labels are ignored.
 *  \param[in,out] uf: Initialized structure
 *  \param[in]  a: First label
 *  \param[in]  b: Second label
 */
void RosmiUnionFindUnite(RosmiUnionFind_t *uf, RosmiLabel_t a, RosmiLabel_t b)
{
    const RosmiLabel_t root_a = RosmiUnionFindFind(uf, a);
    const RosmiLabel_t root_b = RosmiUnionFindFind(uf, b);

    do
    {
        if ((root_a == ROSMI_LABEL_BACKGROUND) || (root_b == ROSMI_LABEL_BACKGROUND) || (root_a == root_b))
        {
            break;
        }

        RosmiLabel_t keep = root_a;
        RosmiLabel_t drop = root_b;

        if (uf->weight[keep] < uf->weight[drop])
        {
            keep = root_b;
            drop = root_a;
        }
        uf->parent[drop] = keep;
        uf->weight[keep] += uf->weight[drop];
    } while (0);
}
/*!
 *  \brief      Number of distinct sets
 *  \param[in,out] uf: Initialized structure
 *  \return     Root count, which is the answer of the counting problem
 */
RosmiLabel_t RosmiUnionFindCountRoots(RosmiUnionFind_t *uf)
{
    RosmiLabel_t roots = 0;

    if ((uf != NULL) && (uf->parent != NULL))
    {
        for (RosmiLabel_t i = 0; i < uf->count; i++)
        {
            if (RosmiUnionFindFind(uf, i) == i)
            {
                roots++;
            }
        }
    }
    return roots;
}
/*!
 *  \brief      Tells whether a label is inside the created range
 *  \param[in]  uf: Initialized structure
 *  \param[in]  label: Label to test
 *  \return     true when the label can be used
 */
static inline bool RosmiUnionFindIsValid(const RosmiUnionFind_t *uf, RosmiLabel_t label)
{
    return ((uf != NULL) && (uf->parent != NULL) && (label >= 0) && (label < uf->count));
}
/*!
 *  \brief      Doubles the capacity of the structure
 *  \param[in,out] uf: Initialized structure
 *  \return     \ref ROSMI_RET_OK, or \ref ROSMI_MALLOC_ERROR keeping the
 *              structure usable with the previous capacity
 */
static RosmiReturn_e RosmiUnionFindGrow(RosmiUnionFind_t *uf)
{
    RosmiReturn_e      ret    = ROSMI_MALLOC_ERROR;
    const RosmiLabel_t wanted = uf->capacity * 2;
    RosmiLabel_t *     parent = NULL;
    RosmiLabel_t *     weight = NULL;

    do
    {
        if (wanted <= uf->capacity)
        {
            break; /* int32_t overflow */
        }

        const size_t bytes = (size_t)wanted * sizeof(RosmiLabel_t);

        parent = (RosmiLabel_t *)RosmiMalloc(bytes);
        weight = (RosmiLabel_t *)RosmiMalloc(bytes);
        if ((parent == NULL) || (weight == NULL))
        {
            break;
        }

        const size_t used = (size_t)uf->count * sizeof(RosmiLabel_t);

        memcpy(parent, uf->parent, used);
        memcpy(weight, uf->weight, used);
        RosmiFree(uf->parent);
        RosmiFree(uf->weight);
        uf->parent   = parent;
        uf->weight   = weight;
        uf->capacity = wanted;
        parent       = NULL; /* ownership handed to the structure */
        weight       = NULL;
        ret          = ROSMI_RET_OK;
    } while (0);

    RosmiFree(parent);
    RosmiFree(weight);
    return ret;
}
