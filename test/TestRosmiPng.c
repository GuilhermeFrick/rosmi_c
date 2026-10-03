/*!
 * \file      TestRosmiPng.c
 * \author    Guilherme Frick de Oliveira
 * \brief     Source file with RosmiPng unity test functions
 * \version   1.0
 * \date      2026-10-03
 * \copyright Copyright (c) 2026
 */
#include "uTest.h"
#include <png.h>
#include <stdio.h>
#include <string.h>
#include "RosmiPng.h"
#include "TestRosmi.h"
#include "TestRosmiAlloc.h"

/*! \addtogroup  TestRosmiPngPrivate ROSMI Png Test Private
 *  \ingroup TestRosmi
 * @{
 */

/*!
 *  \brief Temporary files used to exercise the readers
 */
static const char TEST_PNG_PATH[]       = "test_image_tmp.png";
static const char TEST_PNG_UPPER_PATH[] = "test_image_tmp_upper.PNG";
static const char TEST_PNG_PBM_PATH[]   = "test_image_tmp_png.pbm";

static void TestPngLoadGray(void);
static void TestPngColorAndAlpha(void);
static void TestPngDispatch(void);
static void TestPngRejectsBadFiles(void);
static void TestPngNoMemory(void);
static bool TestPngWrite(const char *path, png_uint_32 format, png_uint_32 width, png_uint_32 height,
                         const void *pixels);
static bool TestPngWriteRaw(const char *path, const char *content, size_t length);
/*! @}*/ // End of TestRosmiPngPrivate

/*!
 * \brief     Function to test all RosmiPng functions
 */
void TestRosmiPng(void)
{
    SetUp();

    TestPngLoadGray();
    TestPngColorAndAlpha();
    TestPngDispatch();
    TestPngRejectsBadFiles();
    TestPngNoMemory();

    (void)remove(TEST_PNG_PATH);
    (void)remove(TEST_PNG_UPPER_PATH);
    (void)remove(TEST_PNG_PBM_PATH);
    TearDown();
}
/*!
 *  \brief  Tests dimensions, packing and the threshold on a gray image
 *  \details 10 pixels wide, so a row spans two bytes and the second one is
 *           only partly used. The values 127 and 128 sit on both sides of
 *           \ref ROSMI_PNG_THRESHOLD.
 */
static void TestPngLoadGray(void)
{
    static const uint8_t pixels[2][10] = {
        {0u, 255u, 127u, 128u, 255u, 255u, 255u, 255u, 255u, 0u},
        {255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 0u, 255u},
    };
    RosmiImage_t image = {0};

    TestAllocReset();
    EXPECT_TRUE(TestPngWrite(TEST_PNG_PATH, PNG_FORMAT_GRAY, 10u, 2u, pixels));
    EXPECT_EQ(ROSMI_RET_OK, RosmiImageLoadPng(&image, TEST_PNG_PATH));
    EXPECT_EQ(10, image.width);
    EXPECT_EQ(2, image.height);
    EXPECT_EQ(2, image.stride);

    EXPECT_EQ(1, RosmiImageAt(&image, 0, 0)); /* black */
    EXPECT_EQ(0, RosmiImageAt(&image, 0, 1)); /* white */
    EXPECT_EQ(1, RosmiImageAt(&image, 0, 2)); /* 127, just below the threshold */
    EXPECT_EQ(0, RosmiImageAt(&image, 0, 3)); /* 128, the threshold itself */
    EXPECT_EQ(1, RosmiImageAt(&image, 0, 9)); /* in the second byte */
    EXPECT_EQ(0, RosmiImageAt(&image, 1, 0));
    EXPECT_EQ(1, RosmiImageAt(&image, 1, 8));
    EXPECT_EQ(0, RosmiImageAt(&image, 1, 9));

    RosmiImageRelease(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that colour is reduced to gray and transparency to white
 *  \details A fully transparent black pixel must be background: it is
 *           invisible in any viewer, and counting it would add objects the
 *           person looking at the image cannot see.
 */
static void TestPngColorAndAlpha(void)
{
    static const uint8_t pixels[4][4] = {
        {0u, 0u, 0u, 255u},       /* opaque black */
        {255u, 255u, 255u, 255u}, /* opaque white */
        {0u, 0u, 0u, 0u},         /* transparent black */
        {50u, 50u, 50u, 255u},    /* opaque dark gray */
    };
    RosmiImage_t image = {0};

    TestAllocReset();
    EXPECT_TRUE(TestPngWrite(TEST_PNG_PATH, PNG_FORMAT_RGBA, 4u, 1u, pixels));
    EXPECT_EQ(ROSMI_RET_OK, RosmiImageLoadPng(&image, TEST_PNG_PATH));
    EXPECT_EQ(1, RosmiImageAt(&image, 0, 0));
    EXPECT_EQ(0, RosmiImageAt(&image, 0, 1));
    EXPECT_EQ(0, RosmiImageAt(&image, 0, 2));
    EXPECT_EQ(1, RosmiImageAt(&image, 0, 3));

    RosmiImageRelease(&image);
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that the loader picks the reader by the file extension
 */
static void TestPngDispatch(void)
{
    static const uint8_t pixels[1][8] = {{0u, 255u, 255u, 255u, 255u, 255u, 255u, 0u}};
    static const char    pbm[]        = "P4\n8 1\n\x81";
    RosmiImage_t         image        = {0};

    TestAllocReset();
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiImageLoad(&image, NULL));

    /* ".PNG" in upper case still goes to the PNG reader */
    EXPECT_TRUE(TestPngWrite(TEST_PNG_UPPER_PATH, PNG_FORMAT_GRAY, 8u, 1u, pixels));
    EXPECT_EQ(ROSMI_RET_OK, RosmiImageLoad(&image, TEST_PNG_UPPER_PATH));
    EXPECT_EQ(8, image.width);
    EXPECT_EQ(1, RosmiImageAt(&image, 0, 0));
    EXPECT_EQ(1, RosmiImageAt(&image, 0, 7));
    RosmiImageRelease(&image);

    /* any other extension goes to the PBM reader */
    EXPECT_TRUE(TestPngWriteRaw(TEST_PNG_PBM_PATH, pbm, sizeof(pbm) - 1u));
    EXPECT_EQ(ROSMI_RET_OK, RosmiImageLoad(&image, TEST_PNG_PBM_PATH));
    EXPECT_EQ(8, image.width);
    EXPECT_EQ(1, RosmiImageAt(&image, 0, 0));
    EXPECT_EQ(0, RosmiImageAt(&image, 0, 1));
    RosmiImageRelease(&image);

    /* a name too short to carry the extension is not a PNG */
    EXPECT_EQ(ROSMI_IO_ERROR, RosmiImageLoad(&image, "png"));
    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that invalid calls and files are refused with the right code
 */
static void TestPngRejectsBadFiles(void)
{
    static const char pbm[] = "P4\n8 1\n\x81";
    RosmiImage_t      image = {0};

    TestAllocReset();
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiImageLoadPng(NULL, TEST_PNG_PATH));
    EXPECT_EQ(ROSMI_INV_PARAM, RosmiImageLoadPng(&image, NULL));
    EXPECT_EQ(ROSMI_IO_ERROR, RosmiImageLoadPng(&image, "arquivo_que_nao_existe.png"));

    /* a PBM saved under a .png name is not a PNG */
    EXPECT_TRUE(TestPngWriteRaw(TEST_PNG_PATH, pbm, sizeof(pbm) - 1u));
    EXPECT_EQ(ROSMI_FORMAT_ERROR, RosmiImageLoadPng(&image, TEST_PNG_PATH));
    EXPECT_EQ(0, (image.data != NULL));

    EXPECT_EQ(0, TestAllocOutstanding());
}
/*!
 *  \brief  Tests that a failed allocation leaks nothing
 *  \details The reader makes two allocations, the gray buffer and the packed
 *           image; each one is made to fail in turn.
 */
static void TestPngNoMemory(void)
{
    static const uint8_t pixels[1][8] = {{0u, 255u, 255u, 255u, 255u, 255u, 255u, 0u}};
    RosmiImage_t         image        = {0};

    TestAllocReset();
    EXPECT_TRUE(TestPngWrite(TEST_PNG_PATH, PNG_FORMAT_GRAY, 8u, 1u, pixels));

    TestAllocFailAfter(0);
    EXPECT_EQ(ROSMI_MALLOC_ERROR, RosmiImageLoadPng(&image, TEST_PNG_PATH));
    TestAllocUnlimited();
    EXPECT_EQ(0, TestAllocOutstanding());

    TestAllocFailAfter(1);
    EXPECT_EQ(ROSMI_MALLOC_ERROR, RosmiImageLoadPng(&image, TEST_PNG_PATH));
    TestAllocUnlimited();
    EXPECT_EQ(0, TestAllocOutstanding());
    EXPECT_EQ(0, (image.data != NULL));
}
/*!
 *  \brief      Writes a PNG with the libpng simplified API
 *  \param[in]  path: Destination file
 *  \param[in]  format: PNG_FORMAT_* of the pixel buffer
 *  \param[in]  width: Width in pixels
 *  \param[in]  height: Height in pixels
 *  \param[in]  pixels: Tightly packed pixel rows
 *  \return     true when the file was written
 */
static bool TestPngWrite(const char *path, png_uint_32 format, png_uint_32 width, png_uint_32 height,
                         const void *pixels)
{
    png_image png;

    (void)memset(&png, 0, sizeof(png));
    png.version = PNG_IMAGE_VERSION;
    png.width   = width;
    png.height  = height;
    png.format  = format;
    return (png_image_write_to_file(&png, path, 0, pixels, 0, NULL) != 0);
}
/*!
 *  \brief      Writes arbitrary bytes to a file
 *  \param[in]  path: Destination file
 *  \param[in]  content: Bytes to write
 *  \param[in]  length: Number of bytes
 *  \return     true when the file was written
 */
static bool TestPngWriteRaw(const char *path, const char *content, size_t length)
{
    bool  ret  = false;
    FILE *file = fopen(path, "wb");

    if (file != NULL)
    {
        ret = (fwrite(content, 1u, length, file) == length);
        (void)fclose(file);
    }
    return ret;
}
