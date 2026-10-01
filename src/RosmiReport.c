/*!
 * \file      RosmiReport.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with CSV result and trace writing
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "RosmiReport.h"
#include <stdio.h>

/*! \addtogroup  RosmiReportPrivate ROSMI Report Private
 *  \ingroup RosmiReport
 * @{
 */

/*!
 *  \brief Header line of the consolidated results file
 */
static const char ROSMI_CSV_HEADER[] = "versao,imagem,H,W,N,M,K,L,conn,threads,objetos,soma_ingenua,tempo_s";

/*!
 *  \brief Longest trace file path this module composes
 */
typedef enum RosmiReportLimitDefinition
{
    ROSMI_REPORT_PATH_MAX = 512 /**<Trace file path buffer size*/
} RosmiReportLimit_e;

static bool RosmiReportNeedsHeader(const char *path);

/** @}*/ // End of RosmiReportPrivate

/*!
 *  \brief      Appends one result row, writing the header when needed
 *  \param[in]  path: CSV file path
 *  \param[in]  row: Row to append
 *  \return     \ref ROSMI_RET_OK, \ref ROSMI_INV_PARAM or \ref ROSMI_IO_ERROR
 */
RosmiReturn_e RosmiReportAppendCsv(const char *path, const RosmiReportRow_t *row)
{
    RosmiReturn_e ret  = ROSMI_INV_PARAM;
    FILE *        file = NULL;

    do
    {
        if ((path == NULL) || (row == NULL))
        {
            break;
        }

        const bool header = RosmiReportNeedsHeader(path);

        file = fopen(path, "a");
        if (file == NULL)
        {
            ret = ROSMI_IO_ERROR;
            break;
        }
        if (header)
        {
            (void)fprintf(file, "%s\n", ROSMI_CSV_HEADER);
        }
        (void)fprintf(file, "%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%.6f\n", row->version, row->image, row->height,
                      row->width, row->seg_rows, row->seg_cols, row->tile_rows, row->tile_cols, row->conn,
                      row->threads, row->objects, row->naive_sum, row->seconds);
        ret = ROSMI_RET_OK;
    } while (0);

    if (file != NULL)
    {
        (void)fclose(file);
    }
    return ret;
}
/*!
 *  \brief      Writes the per tile trace, the computation load of each tile
 *  \param[in]  prefix: Path prefix; "_tiles.csv" is appended
 *  \param[in]  seg_rows: Segments down, N
 *  \param[in]  seg_cols: Segments across, M
 *  \param[in]  tile_rows: Segment height, K
 *  \param[in]  tile_cols: Segment width, L
 *  \param[in]  components: Local component count per segment
 *  \param[in]  durations: Seconds per segment
 *  \return     \ref ROSMI_RET_OK, \ref ROSMI_INV_PARAM or \ref ROSMI_IO_ERROR
 */
RosmiReturn_e RosmiReportWriteTiles(const char *prefix, int32_t seg_rows, int32_t seg_cols, int32_t tile_rows,
                                    int32_t tile_cols, const RosmiLabel_t *components, const double *durations)
{
    RosmiReturn_e ret  = ROSMI_INV_PARAM;
    FILE *        file = NULL;
    char          path[ROSMI_REPORT_PATH_MAX];

    do
    {
        if ((prefix == NULL) || (components == NULL) || (durations == NULL))
        {
            break;
        }
        if (snprintf(path, sizeof(path), "%s_tiles.csv", prefix) >= (int)sizeof(path))
        {
            break;
        }
        file = fopen(path, "w");
        if (file == NULL)
        {
            ret = ROSMI_IO_ERROR;
            break;
        }

        (void)fprintf(file, "segmento,r,c,pixels,componentes_locais,tempo_s\n");
        for (int32_t row = 0; row < seg_rows; row++)
        {
            for (int32_t col = 0; col < seg_cols; col++)
            {
                const int32_t s = (row * seg_cols) + col;

                (void)fprintf(file, "%d,%d,%d,%d,%d,%.9f\n", s, row, col, tile_rows * tile_cols, components[s],
                              durations[s]);
            }
        }
        ret = ROSMI_RET_OK;
    } while (0);

    if (file != NULL)
    {
        (void)fclose(file);
    }
    return ret;
}
/*!
 *  \brief      Writes the per edge trace, the communication volume per link
 *  \param[in]  prefix: Path prefix; "_edges.csv" is appended
 *  \param[in]  seg_rows: Segments down, N
 *  \param[in]  seg_cols: Segments across, M
 *  \param[in]  tile_rows: Segment height, K
 *  \param[in]  tile_cols: Segment width, L
 *  \return     \ref ROSMI_RET_OK, \ref ROSMI_INV_PARAM or \ref ROSMI_IO_ERROR
 */
RosmiReturn_e RosmiReportWriteEdges(const char *prefix, int32_t seg_rows, int32_t seg_cols, int32_t tile_rows,
                                    int32_t tile_cols)
{
    RosmiReturn_e ret  = ROSMI_INV_PARAM;
    FILE *        file = NULL;
    char          path[ROSMI_REPORT_PATH_MAX];

    do
    {
        if (prefix == NULL)
        {
            break;
        }
        if (snprintf(path, sizeof(path), "%s_edges.csv", prefix) >= (int)sizeof(path))
        {
            break;
        }
        file = fopen(path, "w");
        if (file == NULL)
        {
            ret = ROSMI_IO_ERROR;
            break;
        }

        const int32_t label_bytes = (int32_t)sizeof(RosmiLabel_t);

        (void)fprintf(file, "origem,destino,r_o,c_o,r_d,c_d,tipo,bytes\n");
        for (int32_t row = 0; row < seg_rows; row++)
        {
            for (int32_t col = 0; (col + 1) < seg_cols; col++)
            {
                (void)fprintf(file, "%d,%d,%d,%d,%d,%d,horizontal,%d\n", (row * seg_cols) + col,
                              (row * seg_cols) + col + 1, row, col, row, col + 1, tile_rows * label_bytes);
            }
        }
        for (int32_t row = 0; (row + 1) < seg_rows; row++)
        {
            for (int32_t col = 0; col < seg_cols; col++)
            {
                (void)fprintf(file, "%d,%d,%d,%d,%d,%d,vertical,%d\n", (row * seg_cols) + col,
                              ((row + 1) * seg_cols) + col, row, col, row + 1, col, tile_cols * label_bytes);
            }
        }
        ret = ROSMI_RET_OK;
    } while (0);

    if (file != NULL)
    {
        (void)fclose(file);
    }
    return ret;
}
/*!
 *  \brief      Tells whether the CSV still needs its header line
 *  \details    It cannot be decided with ftell right after fopen in append
 *              mode: the initial stream position is unspecified by the
 *              standard, and on glibc it reads zero even for files that
 *              already have content.
 *  \param[in]  path: CSV file path
 *  \return     true when the file does not exist or is empty
 */
static bool RosmiReportNeedsHeader(const char *path)
{
    bool  needs = true;
    FILE *file  = fopen(path, "rb");

    if (file != NULL)
    {
        needs = (fgetc(file) == EOF);
        (void)fclose(file);
    }
    return needs;
}
