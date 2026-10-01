/*!
 * \file      TestRosmiFixture.h
 * \author    Guilherme Frick de Oliveira
 * \brief     Header file with helpers to build images inside the test suite
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */

/*! \addtogroup  TestRosmiFixture ROSMI Test Fixture
 *  \ingroup TestRosmi
 *  \details Builds images from text so a test case reads like the picture it
 *           is checking. A '#' is an object pixel and anything else is
 *           background, which keeps the expected answer visible next to the
 *           assertion instead of hidden in a binary file.
 * @{
 */
#ifndef TEST_ROSMI_FIXTURE_H
#define TEST_ROSMI_FIXTURE_H
#include "RosmiImage.h"

bool TestImageFromRows(RosmiImage_t *image, const char *const *rows, int32_t count);
void TestImageDestroy(RosmiImage_t *image);

#endif // TEST_ROSMI_FIXTURE_H

/** @}*/ // End of TestRosmiFixture
