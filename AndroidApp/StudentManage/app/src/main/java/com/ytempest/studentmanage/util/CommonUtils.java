package com.ytempest.studentmanage.util;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ytempest
 *         Description：
 */
public class CommonUtils {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeToFile(InputStream inputStream, String targetPath) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(targetPath);

            byte[] buffer = new byte[1024 * 5];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CommonUtils.close(inputStream);
            CommonUtils.close(outputStream);
        }

    }
}
