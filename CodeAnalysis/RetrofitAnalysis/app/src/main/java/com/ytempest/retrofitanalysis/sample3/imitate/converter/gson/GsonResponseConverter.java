package com.ytempest.retrofitanalysis.sample3.imitate.converter.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.ytempest.retrofitanalysis.sample3.imitate.Converter;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * @author ytempest
 *         Description：
 */
class GsonResponseConverter<T> implements Converter<ResponseBody, T> {

    private final Gson gson;
    private final TypeAdapter<T> adapter;

    public GsonResponseConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        JsonReader jsonReader = gson.newJsonReader(value.charStream());
        T result = adapter.read(jsonReader);
        if (result == null) {
            return null;
        }
        return result;
    }
}
