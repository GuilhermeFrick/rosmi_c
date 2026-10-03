/*!
 * \file      RosmiParMain.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Parallel ROSMI program
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiParMain ROSMI Parallel Program
 *  \ingroup Rosmi
 *  \details Label locally, unite on the borders.
 *
 *           Phase 1, parallel and communication free: every segment is
 *           labeled independently by the same core the sequential version
 *           uses. No segment needs to see the whole image.
 *
 *           Phase 2, neighbour communication: a prefix sum moves the local
 *           labels into a global space, then each pair of neighbouring
 *           segments compares the shared border and records an equivalence
 *           wherever two object pixels are contiguous across it.
 *
 *           Phase 3, reduction: the equivalences are applied to a global
 *           union find. The object count is the number of distinct roots, so
 *           an object crossing several segments is counted once.
 * @{
 */
#include <stdio.h>
#include "Rosmi.h"
#include "RosmiCli.h"
#include "RosmiImage.h"
#include "RosmiLabel.h"
#include "RosmiMerge.h"
#include "RosmiPng.h"
#include "RosmiReport.h"
#include "RosmiTask.h"
#include "RosmiUnionFind.h"

/*! \addtogroup  RosmiParMainPrivate ROSMI Parallel Program Private
 *  \ingroup RosmiParMain
 * @{
 */

/*!
 *  \brief Working set of one parallel run, allocated once and reused
 */
typedef struct RosmiParWorkDefinition
{
    RosmiBorders_t *borders;    /**<Borders of every segment*/
    double *        durations;  /**<Seconds spent on every segment*/
    RosmiReturn_e * results;    /**<Return code of every segment*/
    RosmiLabel_t *  offsets;    /**<Global offset of every segment, plus one*/
    RosmiLabel_t *  components; /**<Local component count of every segment*/
    int32_t         segments;   /**<Number of segments, N*M*/
    int32_t         ready;      /**<Borders successfully initialized*/
} RosmiParWork_t;

/*!
 *  \brief Measurements of one complete run of the pipeline
 */
typedef struct RosmiParRunDefinition
{
    RosmiLabel_t      objects;     /**<Objects counted*/
    RosmiLabel_t      local_sum;   /**<Sum of the local counts*/
    double            phase_label; /**<Seconds of phase 1*/
    double            phase_merge; /**<Seconds of phase 2*/
    double            phase_count; /**<Seconds of phase 3*/
    double            total;       /**<Seconds of the whole pipeline*/
    RosmiMergeStats_t stats;       /**<Communication accounting*/
} RosmiParRun_t;

static RosmiReturn_e RosmiParWorkInitialize(RosmiParWork_t *work, const RosmiCli_t *options, int32_t tile_rows,
                                            int32_t tile_cols);
static void          RosmiParWorkDeinitialize(RosmiParWork_t *work);
static RosmiReturn_e RosmiParExecute(const RosmiImage_t *image, const RosmiCli_t *options, int32_t tile_rows,
                                     int32_t tile_cols, int32_t threads, RosmiParWork_t *work,
                                     RosmiParRun_t *run);
static void          RosmiParPrint(const RosmiCli_t *options, const RosmiImage_t *image, int32_t tile_rows,
                                   int32_t tile_cols, int32_t threads, const RosmiParWork_t *work,
                                   const RosmiParRun_t *run);

/** @}*/ // End of RosmiParMainPrivate

/*!
 *  \brief      Entry point of the parallel program
 *  \param[in]  argc: Argument count
 *  \param[in]  argv: Argument vector
 *  \return     0 on success, 1 on runtime failure, 2 on bad usage
 */
int main(int argc, char **argv)
{
    RosmiCli_t     options = {0};
    RosmiImage_t   image   = {0};
    RosmiParWork_t work    = {0};
    RosmiReturn_e  ret     = RosmiCliParse(&options, argc, argv);
    int            status  = 2;

    do
    {
        if (ret != ROSMI_RET_OK)
        {
            RosmiCliUsage(argv[0]);
            break;
        }
        status = 1;

        ret = RosmiImageLoad(&image, options.image);
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
        const int32_t threads   = (options.threads > 0) ? options.threads : RosmiGetCpuCount();

        ret = RosmiParWorkInitialize(&work, &options, tile_rows, tile_cols);
        if (ret != ROSMI_RET_OK)
        {
            (void)fprintf(stderr, "erro ao alocar as estruturas: %s\n", RosmiReturnStr(ret));
            break;
        }

        RosmiParRun_t best = {0};

        for (int32_t r = 0; (r < options.reps) && (ret == ROSMI_RET_OK); r++)
        {
            RosmiParRun_t run = {0};

            ret = RosmiParExecute(&image, &options, tile_rows, tile_cols, threads, &work, &run);
            if ((ret == ROSMI_RET_OK) && ((r == 0) || (run.total < best.total)))
            {
                best = run;
            }
        }
        if (ret != ROSMI_RET_OK)
        {
            (void)fprintf(stderr, "erro na execucao: %s\n", RosmiReturnStr(ret));
            break;
        }

        RosmiParPrint(&options, &image, tile_rows, tile_cols, threads, &work, &best);

        if (options.csv != NULL)
        {
            const RosmiReportRow_t row = {"par",           options.image,     image.height,  image.width,
                                          options.seg_rows, options.seg_cols, tile_rows,     tile_cols,
                                          (int32_t)options.conn, threads,     best.objects,  best.local_sum,
                                          best.total};

            (void)RosmiReportAppendCsv(options.csv, &row);
        }
        if (options.trace != NULL)
        {
            (void)RosmiReportWriteTiles(options.trace, options.seg_rows, options.seg_cols, tile_rows, tile_cols,
                                        work.components, work.durations);
            (void)RosmiReportWriteEdges(options.trace, options.seg_rows, options.seg_cols, tile_rows, tile_cols);
        }
        status = 0;
    } while (0);

    RosmiParWorkDeinitialize(&work);
    RosmiImageRelease(&image);
    return status;
}
/*!
 *  \brief      Allocates the working set of a parallel run
 *  \param[out] work: Structure to prepare
 *  \param[in]  options: Parsed options, source of the segmentation
 *  \param[in]  tile_rows: Segment height, K
 *  \param[in]  tile_cols: Segment width, L
 *  \return     \ref ROSMI_RET_OK or \ref ROSMI_MALLOC_ERROR
 */
static RosmiReturn_e RosmiParWorkInitialize(RosmiParWork_t *work, const RosmiCli_t *options, int32_t tile_rows,
                                            int32_t tile_cols)
{
    RosmiReturn_e ret      = ROSMI_MALLOC_ERROR;
    const int32_t segments = options->seg_rows * options->seg_cols;

    do
    {
        work->segments   = segments;
        work->ready      = 0;
        work->borders    = (RosmiBorders_t *)RosmiMalloc((size_t)segments * sizeof(RosmiBorders_t));
        work->durations  = (double *)RosmiMalloc((size_t)segments * sizeof(double));
        work->results    = (RosmiReturn_e *)RosmiMalloc((size_t)segments * sizeof(RosmiReturn_e));
        work->components = (RosmiLabel_t *)RosmiMalloc((size_t)segments * sizeof(RosmiLabel_t));
        work->offsets    = (RosmiLabel_t *)RosmiMalloc((size_t)(segments + 1) * sizeof(RosmiLabel_t));

        if ((work->borders == NULL) || (work->durations == NULL) || (work->results == NULL)
            || (work->components == NULL) || (work->offsets == NULL))
        {
            break;
        }

        bool ok = true;

        for (int32_t s = 0; (s < segments) && ok; s++)
        {
            ok = (RosmiBordersInitialize(&work->borders[s], tile_rows, tile_cols) == ROSMI_RET_OK);
            if (ok)
            {
                work->ready++;
            }
        }
        if (!ok)
        {
            break;
        }
        ret = ROSMI_RET_OK;
    } while (0);

    return ret;
}
/*!
 *  \brief      Releases the working set
 *  \param[in]  work: Structure to release; partially built sets are handled
 */
static void RosmiParWorkDeinitialize(RosmiParWork_t *work)
{
    if (work->borders != NULL)
    {
        for (int32_t s = 0; s < work->ready; s++)
        {
            RosmiBordersDeinitialize(&work->borders[s]);
        }
    }
    RosmiFree(work->borders);
    RosmiFree(work->durations);
    RosmiFree(work->results);
    RosmiFree(work->components);
    RosmiFree(work->offsets);
    work->borders    = NULL;
    work->durations  = NULL;
    work->results    = NULL;
    work->components = NULL;
    work->offsets    = NULL;
    work->ready      = 0;
}
/*!
 *  \brief      Runs the three phases once and collects the measurements
 *  \param[in]  image: Loaded image
 *  \param[in]  options: Parsed options
 *  \param[in]  tile_rows: Segment height, K
 *  \param[in]  tile_cols: Segment width, L
 *  \param[in]  threads: Threads to use
 *  \param[in,out] work: Working set, reused across repetitions
 *  \param[out] run: Receives the measurements
 *  \return     \ref ROSMI_RET_OK or the first failing step
 */
static RosmiReturn_e RosmiParExecute(const RosmiImage_t *image, const RosmiCli_t *options, int32_t tile_rows,
                                     int32_t tile_cols, int32_t threads, RosmiParWork_t *work,
                                     RosmiParRun_t *run)
{
    RosmiReturn_e     ret   = ROSMI_RET_OK;
    RosmiUnionFind_t  uf    = {0};
    RosmiTaskConfig_t config;
    const double      start = RosmiGetTimeSeconds();

    config.image     = image;
    config.seg_rows  = options->seg_rows;
    config.seg_cols  = options->seg_cols;
    config.tile_rows = tile_rows;
    config.tile_cols = tile_cols;
    config.conn      = options->conn;
    config.borders   = work->borders;
    config.durations = work->durations;
    config.results   = work->results;

    do
    {
        /* Phase 1: local labeling, spread over the threads */
        ret = RosmiTaskLabelSegments(&config, threads);
        if (ret != ROSMI_RET_OK)
        {
            break;
        }
        run->phase_label = RosmiGetTimeSeconds() - start;

        /* Phase 2: prefix sum and border reconciliation */
        const double merge_start = RosmiGetTimeSeconds();

        ret = RosmiMergeOffsets(work->borders, work->segments, work->offsets);
        if (ret != ROSMI_RET_OK)
        {
            break;
        }
        run->local_sum = work->offsets[work->segments];

        ret = RosmiUnionFindInitialize(&uf, run->local_sum);
        if (ret != ROSMI_RET_OK)
        {
            break;
        }
        for (RosmiLabel_t i = 0; (i < run->local_sum) && (ret == ROSMI_RET_OK); i++)
        {
            RosmiLabel_t created = 0;

            ret = RosmiUnionFindMakeSet(&uf, &created);
        }
        if (ret != ROSMI_RET_OK)
        {
            break;
        }

        const RosmiMergeRequest_t request = {options->seg_rows, options->seg_cols, options->conn, work->borders,
                                             work->offsets};

        ret = RosmiMergeRun(&request, &uf, &run->stats);
        if (ret != ROSMI_RET_OK)
        {
            break;
        }
        run->phase_merge = RosmiGetTimeSeconds() - merge_start;

        /* Phase 3: count the distinct roots */
        const double count_start = RosmiGetTimeSeconds();

        run->objects     = RosmiUnionFindCountRoots(&uf);
        run->phase_count = RosmiGetTimeSeconds() - count_start;
        run->total       = RosmiGetTimeSeconds() - start;

        for (int32_t s = 0; s < work->segments; s++)
        {
            work->components[s] = work->borders[s].components;
        }
    } while (0);

    RosmiUnionFindDeinitialize(&uf);
    return ret;
}
/*!
 *  \brief      Prints the report of a parallel run
 *  \param[in]  options: Parsed options
 *  \param[in]  image: Loaded image
 *  \param[in]  tile_rows: Segment height, K
 *  \param[in]  tile_cols: Segment width, L
 *  \param[in]  threads: Threads used
 *  \param[in]  work: Working set, source of the per tile times
 *  \param[in]  run: Measurements to report
 */
static void RosmiParPrint(const RosmiCli_t *options, const RosmiImage_t *image, int32_t tile_rows,
                          int32_t tile_cols, int32_t threads, const RosmiParWork_t *work,
                          const RosmiParRun_t *run)
{
    double sum = 0.0;
    double max = 0.0;

    for (int32_t s = 0; s < work->segments; s++)
    {
        sum += work->durations[s];
        if (work->durations[s] > max)
        {
            max = work->durations[s];
        }
    }

    (void)printf("ROSMI paralelo (C)\n");
    (void)printf("  imagem            : %s (%d x %d px)\n", options->image, image->height, image->width);
    (void)printf("  segmentacao       : N=%d x M=%d = %d segmentos de K=%d x L=%d\n", options->seg_rows,
                 options->seg_cols, work->segments, tile_rows, tile_cols);
    (void)printf("  conectividade     : %d\n", (int)options->conn);
    (void)printf("  threads           : %d\n", threads);
    (void)printf("  OBJETOS           : %d\n", run->objects);
    (void)printf("  soma local        : %d (%d a mais: objetos distribuidos)\n", run->local_sum,
                 run->local_sum - run->objects);
    (void)printf("  fase 1 (local)    : %.6f s   [soma dos tiles %.6f s, tile mais lento %.6f s]\n",
                 run->phase_label, sum, max);
    (void)printf("  fase 2 (bordas)   : %.6f s   [%lld arestas, %lld bytes, %lld unioes]\n", run->phase_merge,
                 (long long)run->stats.edges, (long long)run->stats.bytes, (long long)run->stats.unions);
    (void)printf("  fase 3 (reducao)  : %.6f s\n", run->phase_count);
    (void)printf("  TEMPO TOTAL       : %.6f s%s\n", run->total,
                 (options->reps > 1) ? "  [melhor das repeticoes]" : "");
}

/** @}*/ // End of RosmiParMain
