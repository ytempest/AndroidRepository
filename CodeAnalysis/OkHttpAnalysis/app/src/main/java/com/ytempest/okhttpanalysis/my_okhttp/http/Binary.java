package com.ytempest.okhttpanalysis.my_okhttp.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author ytempest
 *         Description：
 */
public interface Binary {
    long fileLength();

    String fileName();

    String mimeType();

    void onWrite(OutputStream outputStream) throws IOException;
}
