package com.ytempest.baselibrary.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author ytempest
 *         Description: 文件操作辅助类
 */
public class FileUtils {

    /**
     * copy src to dest
     *
     * @param src  source file
     * @param dest target file
     * @throws IOException
     */
    public static void copyFile(File src, File dest) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dest).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * 递归删除文件夹以及文件下的所有文件和文件夹
     *
     * @param dir
     */
    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (!dir.exists() || !dir.isDirectory()) {
            return true;
        }
        for (File file : dir.listFiles()) {
            if (dir.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                deleteDir(file);
            }
        }
        return dir.delete();
    }

    /**
     * 删除文件夹下的所有文件
     *
     * @param dir
     */
    public static boolean deleteFileInDir(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return false;
        }
        if (!dir.exists()) {
            return true;
        }
        for (File file : dir.listFiles()) {
            if (dir.isFile()) {
                Log.e("TAG", "deleteFileInDir: delete ;" + file.getName());
                file.delete();
            } else {
                Log.e("TAG", "deleteFileInDir: delete ;" + file.getName());
                deleteDir(file);
            }
        }
        return true;
    }

}
