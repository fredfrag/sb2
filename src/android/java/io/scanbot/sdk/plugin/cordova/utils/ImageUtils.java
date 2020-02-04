/*
    Scanbot SDK Cordova Plugin

    Copyright (c) 2017 doo GmbH. All rights reserved.
 */
package io.scanbot.sdk.plugin.cordova.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;

import net.doo.snap.util.FileChooserUtils;
import net.doo.snap.util.bitmap.BitmapUtils;

import java.io.IOException;
import java.io.File;

public final class ImageUtils {

    /**
     * Default JPG image quality
     */
    public static final int JPEG_QUALITY = 95;

    private ImageUtils() {
    }

    public static Bitmap loadImage(final String imageFilePath) {
        return BitmapUtils.decodeQuietly(imageFilePath, null);
    }

    public static Bitmap loadImage(final Uri imageUri, final Context context) throws IOException {
        final String filePath = FileChooserUtils.getPath(context, imageUri);

        if (filePath == null) {
            throw new IOException("Invalid/unsupported image file URI: " + imageUri);
        }

        if (!new File(filePath).exists()) {
            throw new IOException("File does not exist: " + filePath);
        }

        return loadImage(filePath);
    }


    public static Bitmap resizeImage(final Bitmap originalImage, final float width, final float height) {
        final float oldWidth = originalImage.getWidth();
        final float oldHeight = originalImage.getHeight();

        final float scaleFactor;
        if (oldWidth > oldHeight) {
            scaleFactor = width / oldWidth;
        } else {
            scaleFactor = height / oldHeight;
        }

        final float newHeight = oldHeight * scaleFactor;
        final float newWidth = oldWidth * scaleFactor;

        return Bitmap.createScaledBitmap(originalImage, (int) newWidth, (int) newHeight, false);
    }

    public static Bitmap rotateBitmap(Bitmap source, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
