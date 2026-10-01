/*!
 * \file      RosmiUnionFind.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with disjoint set structure
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiUnionFind ROSMI Union Find
 *  \ingroup Rosmi
 *  \details Union by size with path halving.
 *
 *           This is the central structure of **both** levels of the
 *           algorithm: inside a segment it merges provisional labels of the
 *           raster scan; between segments it merges local components that
 *           touch across the borders. That reuse is why the parallel version
 *           needs no new algorithm.
 *
 *           The number of objects is, in the end, the number of distinct
 *           roots.
 * @{
 */
#ifndef ROSMI_UNION_FIND_H
#define ROSMI_UNION_FIND_H
#include "Rosmi.h"

#ifdef __cplusplus
extern "C"
{
#endif

    /*!
     *  \brief Collection of disjoint sets over labels
     *  \details Capacity grows on demand because the number of provisional
     *           labels of a segment depends on the image content, not on its
     *           size, and is not known beforehand.
     */
    typedef struct RosmiUnionFindDefinition
    {
        RosmiLabel_t *parent;   /**<Parent of each element; a root points to itself*/
        RosmiLabel_t *weight;   /**<Tree size, used for union by size*/
        RosmiLabel_t  count;    /**<Elements created*/
        RosmiLabel_t  capacity; /**<Elements that fit without reallocating*/
    } RosmiUnionFind_t;

    RosmiReturn_e RosmiUnionFindInitialize(RosmiUnionFind_t *uf, RosmiLabel_t capacity);
    void          RosmiUnionFindDeinitialize(RosmiUnionFind_t *uf);
    RosmiReturn_e RosmiUnionFindMakeSet(RosmiUnionFind_t *uf, RosmiLabel_t *label);
    RosmiLabel_t  RosmiUnionFindFind(RosmiUnionFind_t *uf, RosmiLabel_t label);
    void          RosmiUnionFindUnite(RosmiUnionFind_t *uf, RosmiLabel_t a, RosmiLabel_t b);
    RosmiLabel_t  RosmiUnionFindCountRoots(RosmiUnionFind_t *uf);

#ifdef __cplusplus
}
#endif

#endif // ROSMI_UNION_FIND_H

/** @}*/ // End of RosmiUnionFind
