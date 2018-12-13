package com.ytempest.studentmanage.http.converter;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * @author ytempest
 *         Description：
 */
class InputStreamConverter implements Converter<ResponseBody, InputStream> {
    @Override
    public InputStream convert(ResponseBody responseBody) throws IOException {
        return responseBody.byteStream();
    }
}
