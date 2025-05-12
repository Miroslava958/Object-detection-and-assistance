package com.miroslava958.objectdetectionandassistance;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Utility class for image processing tasks.
 * Contains helper methods to convert YUV_420_888 images into standard Bitmap format.
 * CameraX outputs images in YUV must be converted to RGB Bitmaps for visualisation
 * and TensorFlow Lite processing.
 *
 * Author: Miroslava Milcheva
 * Course: BSc Computing - Final Year Project
 */
public class ImageUtils {

    /**
     * Converts a YUV image from CameraX into a Bitmap.
     * This method repacks the YUV image data into NV21 format and compresses it into a JPEG,
     * and decoded into a Bitmap.
     *
     * @param image A camera frame in YUV_420_888 format
     * @return A Bitmap suitable for visualisation and TensorFlow Lite processing
     * @throws IllegalArgumentException if the image format is unsupported
     */
    public static Bitmap toBitmapFromYUV(Image image) {
        // Check that the image is in the correct format
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("Unsupported image format");
        }

        // Access each plane - Y=luminance, U=chrominance, and V=chrominance
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        // Calculate the total size of each plane
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        // Allocate a byte array for NV21 format
        byte[] nv21 = new byte[ySize + uSize + vSize];
        // Copy the Y, V, and U planes into the NV21 array
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        // Retrieve image dimensions
        int width = image.getWidth();
        int height = image.getHeight();
        // Convert the NV21 byte array into a YuvImage
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);

        // Compress the YuvImage into JPEG format
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        byte[] jpegBytes = out.toByteArray();

        // Decode the JPEG byte array into a Bitmap
        return android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
    }
}