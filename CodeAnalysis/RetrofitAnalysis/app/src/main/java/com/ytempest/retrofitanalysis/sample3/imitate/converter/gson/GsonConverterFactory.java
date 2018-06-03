package com.ytempest.retrofitanalysis.sample3.imitate.converter.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.ytempest.retrofitanalysis.sample3.imitate.Converter;
import com.ytempest.retrofitanalysis.sample3.imitate.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * @author ytempest
 *         Description：
 */

public final class GsonConverterFactory extends Converter.Factory {
    private final Gson gson;

    public GsonConverterFactory(Gson gson) {
        this.gson = gson;
    }

    public static GsonConverterFactory create() {
        return create(new Gson());
    }

    private static GsonConverterFactory create(Gson gson) {
        if (gson == null) {
            throw new NullPointerException("gson is null");
        }
        return new GsonConverterFactory(gson);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonResponseConverter<>(gson, adapter);
    }
}
