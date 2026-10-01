/*!
 * \file      TestRosmiHost.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Wrapper for uTest running on a hosted platform
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  TestRosmiHost ROSMI Test Host
 *  \ingroup TestRosmi
 *  \details   TestRosmiHost Wrapper Guide
 *   =========================================
 *
 *   =========================================
 *   Making this wrapper functional
 *   =========================================
 *  - Build this source file in the test project to override the uTest weak
 *    functions \ref TestWrite and \ref TestGetTick \n
 *
 *  uTest leaves both as weak stubs so that the same suite can run on a board,
 *  where the output goes to a serial port and the tick comes from the RTOS.
 *  On a desktop the output is stdout and the tick is the monotonic clock, and
 *  that is all this file does.
 * @{
 */
#include <stdio.h>
#include <stdint.h>
#include <stdbool.h>
#include "uTest.h"
#include "Rosmi.h"

/*
 * uTest.h nao declara estes dois: sao pontos de extensao que cada projeto
 * define. Os prototipos ficam aqui para satisfazer -Wmissing-prototypes.
 */
bool     TestWrite(uint8_t *data, uint32_t size);
uint32_t TestGetTick(void);

/*!
 *  \overload bool TestWrite(uint8_t *data, uint32_t size)
 */
bool TestWrite(uint8_t *data, uint32_t size)
{
    const size_t written = fwrite(data, 1u, (size_t)size, stdout);

    (void)fflush(stdout);
    return (written == (size_t)size);
}
/*!
 *  \overload uint32_t TestGetTick(void)
 */
uint32_t TestGetTick(void)
{
    return (uint32_t)(RosmiGetTimeSeconds() * 1000.0);
}

/** @}*/ // End of TestRosmiHost
