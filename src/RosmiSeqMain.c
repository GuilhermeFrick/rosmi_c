/*!
 * \file      RosmiSeqMain.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Sequential ROSMI program
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiSeqMain ROSMI Sequential Program
 *  \ingroup Rosmi
 *  \details Treats the whole image as a single block, so the N x M
 *           segmentation does not influence the answer. It is used here only
 *           to report the "naive sum", the number a program that merely added
 *           the local counts would produce. The gap between that number and
 *           the correct count is exactly the effect of the distributed
 *           objects.
 * @{
 */
#include <stdio.h>
#include "Rosmi.h"
#include "RosmiCli.h"
#include "RosmiImage.h"
#include "RosmiLabel.h"
#include "RosmiReport.h"

/*! \addtogroup  RosmiSeqMainPrivate ROSMI Sequential Program Private
 *  \ingroup RosmiSeqMain
 * @{
 */

static RosmiReturn_e RosmiSeqCountWhole(const RosmiImage_t *image, RosmiConnectivity_e conn, int32_t reps,
                                        RosmiLabel_t *objects, double *best);
static RosmiReturn_e RosmiSeqCountNaive(const RosmiImage_t *image, const RosmiCli_t *options, int32_t tile_rows,
                                        int32_t tile_cols, RosmiLabel_t *naive);

/** @}*/ // End of RosmiSeqMainPrivate

/*!
 *  \brief      Entry point of the sequential program
 *  \param[in]  argc: Argument count
 *  \param[in]  argv: Argument vector
 *  \return     0 on success, 1 on runtime failure, 2 on bad usage
 */
int main(int argc, char **argv)
{
    RosmiCli_t    options = {0};
    RosmiImage_t  image   = {0};
    RosmiReturn_e ret     = RosmiCliParse(&options, argc, argv);
    int           status  = 2;

    do
    {
        if (ret != ROSMI_RET_OK)
        {
            RosmiCliUsage(argv[0]);
            break;
        }
        status = 1;

        ret = RosmiImageLoadPbm(&image, options.image);
        if (ret != ROSMI_RET_OK)
        {
            (void)fprintf(stderr, "erro ao abrir '%s': %s\n", options.image, RosmiReturnStr(ret));
            break;
        }
        if (((image.height % options.seg_rows) != 0) || ((image.width % options.seg_cols) != 0))
        {
            (void)fprintf(stderr, "erro: imagem %dx%d nao divide em %dx%d segmentos\n", image.height, image.width,
                          options.seg_rows, options.seg_cols);
            break;
        }

        const int32_t tile_rows = image.height / options.seg_rows;
        const int32_t tile_cols = image.width / options.seg_cols;
        RosmiLabel_t  objects   = 0;
        RosmiLabel_t  naive     = 0;
        double        best      = 0.0;

        ret = RosmiSeqCountWhole(&image, options.conn, options.reps, &objects, &best);
        if (ret != ROSMI_RET_OK)
        {
            (void)fprintf(stderr, "erro na rotulacao: %s\n", RosmiReturnStr(ret));
            break;
        }
        ret = RosmiSeqCountNaive(&image, &options, tile_rows, tile_cols, &naive);
        if (ret != ROSMI_RET_OK)
        {
            (void)fprintf(stderr, "erro na contagem por segmento: %s\n", RosmiReturnStr(ret));
            break;
        }

        (void)printf("ROSMI sequencial (C)\n");
        (void)printf("  imagem            : %s (%d x %d px)\n", options.image, image.height, image.width);
        (void)printf("  segmentacao       : N=%d x M=%d de K=%d x L=%d\n", options.seg_rows, options.seg_cols,
                     tile_rows, tile_cols);
        (void)printf("  conectividade     : %d\n", (int)options.conn);
        (void)printf("  OBJETOS           : %d\n", objects);
        (void)printf("  soma ingenua      : %d (conta %d vez(es) a mais os distribuidos)\n", naive,
                     naive - objects);
        (void)printf("  tempo (imagem)    : %.6f s%s\n", best, (options.reps > 1) ? "  [melhor das repeticoes]" : "");

        if (options.csv != NULL)
        {
            const RosmiReportRow_t row = {"seq",           options.image,     image.height, image.width,
                                          options.seg_rows, options.seg_cols, tile_rows,    tile_cols,
                                          (int32_t)options.conn, 1,           objects,      naive,
                                          best};

            (void)RosmiReportAppendCsv(options.csv, &row);
        }
        status = 0;
    } while (0);

    RosmiImageRelease(&image);
    return status;
}
/*!
 *  \brief      Counts the connected components of the whole image
 *  \param[in]  image: Loaded image
 *  \param[in]  conn: Neighbourhood criterion
 *  \param[in]  reps: Repetitions; the shortest run is reported
 *  \param[out] objects: Receives the object count
 *  \param[out] best: Receives the shortest elapsed time, in seconds
 *  \return     \ref ROSMI_RET_OK or the labeling error
 */
static RosmiReturn_e RosmiSeqCountWhole(const RosmiImage_t *image, RosmiConnectivity_e conn, int32_t reps,
                                        RosmiLabel_t *objects, double *best)
{
    RosmiLabelRequest_t request;
    RosmiReturn_e       ret = ROSMI_RET_OK;

    request.image    = image;
    request.origin_y = 0;
    request.origin_x = 0;
    request.rows     = image->height;
    request.cols     = image->width;
    request.conn     = conn;

    *best = 0.0;
    for (int32_t r = 0; (r < reps) && (ret == ROSMI_RET_OK); r++)
    {
        const double start = RosmiGetTimeSeconds();

        ret = RosmiLabelSegment(&request, NULL, objects);

        const double elapsed = RosmiGetTimeSeconds() - start;

        if ((r == 0) || (elapsed < *best))
        {
            *best = elapsed;
        }
    }
    return ret;
}
/*!
 *  \brief      Sums the component counts of each segment taken alone
 *  \details    This is the wrong answer on purpose: it is reported so the
 *              cost of ignoring the distributed objects is visible.
 *  \param[in]  image: Loaded image
 *  \param[in]  options: Parsed options, source of the segmentation
 *  \param[in]  tile_rows: Segment height, K
 *  \param[in]  tile_cols: Segment width, L
 *  \param[out] naive: Receives the sum of the local counts
 *  \return     \ref ROSMI_RET_OK or the labeling error
 */
static RosmiReturn_e RosmiSeqCountNaive(const RosmiImage_t *image, const RosmiCli_t *options, int32_t tile_rows,
                                        int32_t tile_cols, RosmiLabel_t *naive)
{
    RosmiLabelRequest_t request;
    RosmiReturn_e       ret = ROSMI_RET_OK;

    request.image = image;
    request.rows  = tile_rows;
    request.cols  = tile_cols;
    request.conn  = options->conn;
    *naive        = 0;

    for (int32_t row = 0; (row < options->seg_rows) && (ret == ROSMI_RET_OK); row++)
    {
        for (int32_t col = 0; (col < options->seg_cols) && (ret == ROSMI_RET_OK); col++)
        {
            RosmiLabel_t local = 0;

            request.origin_y = row * tile_rows;
            request.origin_x = col * tile_cols;

            ret = RosmiLabelSegment(&request, NULL, &local);
            *naive += local;
        }
    }
    return ret;
}

/** @}*/ // End of RosmiSeqMain
