/*!
 * \file      RosmiCli.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with command line parsing
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "RosmiCli.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/*! \addtogroup  RosmiCliPrivate ROSMI Cli Private
 *  \ingroup RosmiCli
 * @{
 */

/*!
 *  \brief Option that carries an integer value
 */
typedef struct RosmiCliIntOptionDefinition
{
    const char *name;   /**<Option text, including the leading dashes*/
    int32_t *   target; /**<Field of the options structure to fill*/
} RosmiCliIntOption_t;

/*!
 *  \brief Option that carries a string value
 */
typedef struct RosmiCliStrOptionDefinition
{
    const char * name;   /**<Option text, including the leading dashes*/
    const char **target; /**<Field of the options structure to fill*/
} RosmiCliStrOption_t;

static void RosmiCliDefaults(RosmiCli_t *options, int32_t *conn);
static bool RosmiCliTake(char **argv, int argc, int *index, const RosmiCliIntOption_t *ints,
                         size_t int_count, const RosmiCliStrOption_t *strs, size_t str_count);
static RosmiReturn_e RosmiCliValidate(const RosmiCli_t *options, int32_t conn);

/** @}*/ // End of RosmiCliPrivate

/*!
 *  \brief      Parses the command line into the options structure
 *  \details    The options are described by two tables rather than by a chain
 *              of comparisons, which keeps the parser flat as options are
 *              added and puts the name of each option next to the field it
 *              fills.
 *
 *              Unknown options and a missing image, --N or --M are rejected
 *              rather than defaulted: silently guessing a segmentation would
 *              produce a plausible but meaningless answer.
 *  \param[out] options: Receives the parsed options, defaults applied first
 *  \param[in]  argc: Argument count as received by main
 *  \param[in]  argv: Argument vector as received by main
 *  \return     \ref ROSMI_RET_OK or \ref ROSMI_INV_PARAM
 */
RosmiReturn_e RosmiCliParse(RosmiCli_t *options, int argc, char **argv)
{
    RosmiReturn_e ret  = ROSMI_INV_PARAM;
    int32_t       conn = 0;

    do
    {
        if ((options == NULL) || (argv == NULL))
        {
            break;
        }
        RosmiCliDefaults(options, &conn);

        const RosmiCliIntOption_t ints[] = {
            {"--N", &options->seg_rows},
            {"--M", &options->seg_cols},
            {"--conn", &conn},
            {"--threads", &options->threads},
            {"--reps", &options->reps},
        };
        const RosmiCliStrOption_t strs[] = {
            {"--csv", &options->csv},
            {"--trace", &options->trace},
        };

        bool valid = true;

        for (int i = 1; (i < argc) && valid; i++)
        {
            if (RosmiCliTake(argv, argc, &i, ints, sizeof(ints) / sizeof(ints[0]), strs,
                             sizeof(strs) / sizeof(strs[0])))
            {
                continue;
            }
            if ((argv[i][0] != '-') && (options->image == NULL))
            {
                options->image = argv[i];
                continue;
            }
            valid = false;
        }

        if (valid)
        {
            ret = RosmiCliValidate(options, conn);
        }
        if (ret == ROSMI_RET_OK)
        {
            options->conn = (RosmiConnectivity_e)conn;
        }
    } while (0);

    return ret;
}
/*!
 *  \brief      Prints the usage text on the standard error stream
 *  \param[in]  program: Program name, usually argv[0]
 */
void RosmiCliUsage(const char *program)
{
    (void)fprintf(stderr,
                  "uso: %s <imagem.pbm> --N <n> --M <m> [--conn 4|8] [--threads <p>]\n"
                  "            [--reps <r>] [--csv <arquivo>] [--trace <prefixo>]\n"
                  "  --N, --M   numero de segmentos na vertical e na horizontal\n"
                  "  --conn     conectividade de 'pixels contiguos' (padrao: 4)\n"
                  "  --threads  numero de threads (padrao: todos os processadores)\n"
                  "  --reps     repeticoes da medicao; reporta a melhor\n"
                  "  --csv      anexa uma linha de resultados ao arquivo indicado\n"
                  "  --trace    grava <prefixo>_tiles.csv e <prefixo>_edges.csv\n",
                  program);
}
/*!
 *  \brief      Fills the options structure with the default values
 *  \param[out] options: Structure to reset
 *  \param[out] conn: Connectivity as a plain integer, validated later
 */
static void RosmiCliDefaults(RosmiCli_t *options, int32_t *conn)
{
    options->image    = NULL;
    options->csv      = NULL;
    options->trace    = NULL;
    options->seg_rows = 0;
    options->seg_cols = 0;
    options->threads  = 0;
    options->reps     = 1;
    options->conn     = ROSMI_CONN_4;
    *conn             = (int32_t)ROSMI_CONN_4;
}
/*!
 *  \brief      Consumes one option and its value, if the argument matches one
 *  \details    On a match the index is advanced past the value, so the caller
 *              loop simply moves on.
 *  \param[in]  argv: Argument vector
 *  \param[in]  argc: Argument count
 *  \param[in,out] index: Position of the argument being examined
 *  \param[in]  ints: Table of options carrying an integer
 *  \param[in]  int_count: Entries of the integer table
 *  \param[in]  strs: Table of options carrying a string
 *  \param[in]  str_count: Entries of the string table
 *  \return     true when the argument was an option and was consumed
 */
static bool RosmiCliTake(char **argv, int argc, int *index, const RosmiCliIntOption_t *ints,
                         size_t int_count, const RosmiCliStrOption_t *strs, size_t str_count)
{
    bool        taken = false;
    const char *arg   = argv[*index];

    /* An option sitting at the very end, with no value after it, is not a
       match: it falls through and the caller rejects the command line. */
    if ((*index + 1) < argc)
    {
        for (size_t i = 0; (i < int_count) && !taken; i++)
        {
            if (strcmp(arg, ints[i].name) == 0)
            {
                *ints[i].target = (int32_t)strtol(argv[*index + 1], NULL, 10);
                *index += 1;
                taken = true;
            }
        }
        for (size_t i = 0; (i < str_count) && !taken; i++)
        {
            if (strcmp(arg, strs[i].name) == 0)
            {
                *strs[i].target = argv[*index + 1];
                *index += 1;
                taken = true;
            }
        }
    }
    return taken;
}
/*!
 *  \brief      Checks that the parsed options describe a runnable job
 *  \param[in]  options: Parsed options
 *  \param[in]  conn: Connectivity as read from the command line
 *  \return     \ref ROSMI_RET_OK or \ref ROSMI_INV_PARAM
 */
static RosmiReturn_e RosmiCliValidate(const RosmiCli_t *options, int32_t conn)
{
    RosmiReturn_e ret = ROSMI_INV_PARAM;

    do
    {
        if ((options->image == NULL) || (options->seg_rows <= 0) || (options->seg_cols <= 0))
        {
            break;
        }
        if ((conn != (int32_t)ROSMI_CONN_4) && (conn != (int32_t)ROSMI_CONN_8))
        {
            break;
        }
        if (options->reps < 1)
        {
            break;
        }
        ret = ROSMI_RET_OK;
    } while (0);

    return ret;
}
