/*!
 * \file      RosmiCli.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with command line parsing shared by both programs
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  RosmiCli ROSMI Cli
 *  \ingroup Rosmi
 *  \details Both programs take the same options, so the parser lives once
 *           here instead of twice in the two main files.
 * @{
 */
#ifndef ROSMI_CLI_H
#define ROSMI_CLI_H
#include "Rosmi.h"

#ifdef __cplusplus
extern "C"
{
#endif

    /*!
     *  \brief Options accepted on the command line
     */
    typedef struct RosmiCliDefinition
    {
        const char *        image;    /**<Path of the .pbm image, positional*/
        const char *        csv;      /**<Results file to append to, or NULL*/
        const char *        trace;    /**<Trace path prefix, or NULL*/
        int32_t             seg_rows; /**<--N, segments down*/
        int32_t             seg_cols; /**<--M, segments across*/
        int32_t             threads;  /**<--threads, 0 means every processor*/
        int32_t             reps;     /**<--reps, measurement repetitions*/
        RosmiConnectivity_e conn;     /**<--conn, defaults to 4*/
    } RosmiCli_t;

    RosmiReturn_e RosmiCliParse(RosmiCli_t *options, int argc, char **argv);
    void          RosmiCliUsage(const char *program);

#ifdef __cplusplus
}
#endif

#endif // ROSMI_CLI_H

/** @}*/ // End of RosmiCli
