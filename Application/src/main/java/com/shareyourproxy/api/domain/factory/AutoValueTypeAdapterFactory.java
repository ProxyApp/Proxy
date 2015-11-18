package com.shareyourproxy.api.domain.factory;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.shareyourproxy.api.domain.model.User;

/**
 * Gson adapter factory responsible for deserializing json responses into immutable autovalue
 * objects.
 */
public class AutoValueTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();

        AutoValueClass annotation = rawType.getAnnotation(AutoValueClass.class);
        // Only deserialize classes decorated with @AutoValueClass.
        if (annotation == null || User.class.isInstance(rawType.getClass())) {
            return null;
        }
        return (TypeAdapter<T>) gson.getAdapter(annotation.autoValueClass());
    }

}
