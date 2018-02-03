package com.ytempest.selectimagedemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author ytempest
 *         Description: 使用JPEG图片压缩算法对图片进行压缩
 */
public class ImageUtil {
    static {
        System.loadLibrary("compressimg");
        System.loadLibrary("jpeg");
    }

    /**
     * 图片压缩
     *
     * @param bitmap   图片bitmap
     * @param quality  压缩的质量
     * @param fileName 压缩后的路径
     */
    public static void compressImage(Bitmap bitmap, int quality,
                                     String fileName) {
        compressBitmap(bitmap, quality, fileName);
    }


    /**
     * NDK方法加载图片
     *
     * @param bitmap   图片bitmap
     * @param quality  压缩的质量，一般用75比较合适
     * @param fileName 压缩后的路径
     * @return
     */
    public native static int compressBitmap(Bitmap bitmap, int quality,
                                            String fileName);

    /**
     * 根据路径获取图片，并返回规定宽度的图片
     *
     * @param path 图片路径
     */
    public static Bitmap decodeFile(String path) {
        // 规定图片的宽度，按需求自行规定
        int finalWidth = 800;

        // 先获取宽度
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 不加载图片到内存只拿宽高
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int bitmapWidth = options.outWidth;

        int inSampleSize = 1;

        if (bitmapWidth > finalWidth) {
            inSampleSize = bitmapWidth / finalWidth;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }
}
