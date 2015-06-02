package com.shareyourproxy.api.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Gson adapter factory responsible for deserializing json responses into concrete autovalue
 * objects.
 */
public class AutoParcelAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();

        AutoGson annotation = rawType.getAnnotation(AutoGson.class);
        // Only deserialize classes decorated with @AutoGson.
        if (annotation == null) {
            return null;
        }

        return (TypeAdapter<T>) gson.getAdapter(annotation.autoValueClass());
    }
}
