/*!
 * \file      TestRosmiImage.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with RosmiImage unity test functions
 * \version   1.0
 * \date      2026-09-28
 * \copyright Copyright (c) 2026
 */
#include "uTest.h"
#include <stdio.h>
#include "RosmiImage.h"
#include "TestRosmi.h"
#include "TestRosmiAlloc.h"

/*! \addtogroup  TestRosmiImagePrivate ROSMI Image Test Private
 *  \ingroup TestRosmi
 * @{
 */

/*!
 *  \brief Temporary file used to exercise the reader
 */
static const char TEST_IMAGE_PATH[] = "test_image_tmp.pbm";

static void TestImageLoadValid(void);
static void TestImageBitPolarity(void);
static void TestImageComments(void);
static void TestImageRejectsBadFiles(void);
static void TestImageNoMemory(void);
static bool TestImageWrite(const char *content, size_t length);
/*! @}*/ // End of TestRosmiImagePrivate

/*!
 * \brief     Function to test all RosmiImage functions
 */
void TestRosmiImage(void)
{
    SetUp();

    TestImageLoadValid();
    TestImageBitPolarity();
    TestImageComments();
    TestImageRejectsBadFiles();
    TestImageNoMemory();

    (void)remove(TEST_IMAGE_PATH);
    TearDown();
}
/*!
 *  \brief  Tests loading a well formed image and its dimensions
 */
static void TestImageLoadValid(void)
{
    /* 8 x 2 image: first row 0xC0 (two leftmost pixels set), second 0x03 */
    static const char content[] = "P4\n8 2\n\xC0\x03";
    RosmiImage_t      image     = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageWrite(content, sizeof(content) - 1u));
    EXPECT_EQ(ROSMI_RET_OK, RosmiImageLoadPbm(&image, TEST_IMAGE_PATH));
    EXPECT_EQ(8, image.width);
    EXPECT_EQ(2, image.height);
    EXPECT_EQ(1, image.stride);

    RosmiImageRelease(&image);
    RosmiImageRelease(&image); /* releasing twice must be safe */
    RosmiImageRelease(NULL);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests the project convention that bit 1 is an object pixel
 *  \details Worth an explicit test: writing the file with a library that
 *           treats 255 as white would silently invert the whole image, and
 *           every count would still look plausible.
 */
static void TestImageBitPolarity(void)
{
    static const char content[] = "P4\n8 2\n\xC0\x03";
    RosmiImage_t      image     = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageWrite(content, sizeof(content) - 1u));
    EXPECT_EQ(ROSMI_RET_OK, RosmiImageLoadPbm(&image, TEST_IMAGE_PATH));

    /* 0xC0 is 11000000: the two leftmost pixels are objects */
    EXPECT_EQ(1, RosmiImageAt(&image, 0, 0));
    EXPECT_EQ(1, RosmiImageAt(&image, 0, 1));
    EXPECT_EQ(0, RosmiImageAt(&image, 0, 2));
    EXPECT_EQ(0, RosmiImageAt(&image, 0, 7));
    /* 0x03 is 00000011: the two rightmost pixels are objects */
    EXPECT_EQ(0, RosmiImageAt(&image, 1, 0));
    EXPECT_EQ(1, RosmiImageAt(&image, 1, 6));
    EXPECT_EQ(1, RosmiImageAt(&image, 1, 7));

    RosmiImageRelease(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that header comments are skipped
 */
static void TestImageComments(void)
{
    static const char content[] = "P4\n# gerado pelo teste\n8 2\n# outro comentario\n\xC0\x03";
    RosmiImage_t      image     = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageWrite(content, sizeof(content) - 1u));
    EXPECT_EQ(ROSMI_RET_OK, RosmiImageLoadPbm(&image, TEST_IMAGE_PATH));
    EXPECT_EQ(8, image.width);
    EXPECT_EQ(2, image.height);

    RosmiImageRelease(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that malformed files are refused with the right code
 */
static void TestImageRejectsBadFiles(void)
{
    RosmiImage_t      image = {0};
    static const char p5[]  = "P5\n8 2\n\xC0\x03";
    static const char zero[] = "P4\n0 2\n";
    static const char text[] = "P4\nabc 2\n";
    static const char cut[]  = "P4\n8 4\n\xC0";

    TestAllocReset();
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiImageLoadPbm(NULL, TEST_IMAGE_PATH));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiImageLoadPbm(&image, NULL));
    EXPECT_EQ(ROSMI_IO_ERROR, RosmiImageLoadPbm(&image, "arquivo_que_nao_existe.pbm"));

    EXPECT_TRUE(TestImageWrite(p5, sizeof(p5) - 1u));
    EXPECT_EQ(ROSMI_FORMAT_ERROR, RosmiImageLoadPbm(&image, TEST_IMAGE_PATH));

    EXPECT_TRUE(TestImageWrite(zero, sizeof(zero) - 1u));
    EXPECT_EQ(ROSMI_FORMAT_ERROR, RosmiImageLoadPbm(&image, TEST_IMAGE_PATH));

    EXPECT_TRUE(TestImageWrite(text, sizeof(text) - 1u));
    EXPECT_EQ(ROSMI_FORMAT_ERROR, RosmiImageLoadPbm(&image, TEST_IMAGE_PATH));

    EXPECT_TRUE(TestImageWrite(cut, sizeof(cut) - 1u));
    EXPECT_EQ(ROSMI_FORMAT_ERROR, RosmiImageLoadPbm(&image, TEST_IMAGE_PATH));

    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that a failed allocation leaks nothing
 */
static void TestImageNoMemory(void)
{
    static const char content[] = "P4\n8 2\n\xC0\x03";
    RosmiImage_t      image     = {0};

    TestAllocReset();
    EXPECT_TRUE(TestImageWrite(content, sizeof(content) - 1u));
    TestAllocFailAfter(0);
    EXPECT_EQ(ROSMI_MALLOC_ERROR, RosmiImageLoadPbm(&image, TEST_IMAGE_PATH));
    TestAllocUnlimited();
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief      Writes the temporary file used by the reader tests
 *  \param[in]  content: Bytes to write
 *  \param[in]  length: Number of bytes
 *  \return     true when the file was written
 */
static bool TestImageWrite(const char *content, size_t length)
{
    bool  ret  = false;
    FILE *file = fopen(TEST_IMAGE_PATH, "wb");

    if (file != NULL)
    {
        ret = (fwrite(content, 1u, length, file) == length);
        (void)fclose(file);
    }
    return ret;
}
