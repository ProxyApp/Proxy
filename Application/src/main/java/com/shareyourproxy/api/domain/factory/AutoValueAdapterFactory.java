package com.shareyourproxy.api.domain.factory;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Gson adapter factory responsible for deserializing json responses into immutable autovalue
 * objects.
 */
public class AutoValueAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();

        AutoValueClass annotation = rawType.getAnnotation(AutoValueClass.class);
        // Only deserialize classes decorated with @AutoValueClass.
        if (annotation == null) {
            return null;
        }
        return (TypeAdapter<T>) gson.getAdapter(annotation.autoValueClass());
    }
}
