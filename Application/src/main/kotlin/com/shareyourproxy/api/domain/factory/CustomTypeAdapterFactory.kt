package com.shareyourproxy.api.domain.factory

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

import java.io.IOException

/**
 * Thanks Jesse, implement hooks in to a custom type adapter.
 */
internal abstract class CustomTypeAdapterFactory<C>(private val customizedClass: Class<C>) : TypeAdapterFactory {

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        return checkType(gson, type)
    }

    private fun <T> checkType(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        return when (type.rawType) {
            customizedClass -> customizeMyClassAdapter(gson, type) as TypeAdapter<T>
            else -> null
        }
    }

    private fun <T> customizeMyClassAdapter(gson: Gson, type: TypeToken<T>): TypeAdapter<C> {
        val delegate = gson.getAdapter(customizedClass)
        val elementAdapter = gson.getAdapter(JsonElement::class.java)
        return getTypeAdapter(type, delegate, elementAdapter)
    }

    private fun <T> getTypeAdapter(type: TypeToken<T>, delegate: TypeAdapter<C>, elementAdapter: TypeAdapter<JsonElement>): TypeAdapter<C> {
        return object : TypeAdapter<C>() {
            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: C) {
                val tree = delegate.toJsonTree(value)
                beforeWrite(value, tree)
                elementAdapter.write(out, tree)
            }

            @Throws(IOException::class)
            override fun read(input: JsonReader): C {
                val tree = elementAdapter.read(input)
                afterRead(tree)
                return delegate.fromJsonTree(tree)
            }
        }
    }

    /**
     * Override this to muck with `toSerialize` before it is written to the outgoing JSON stream.
     */
    protected open fun beforeWrite(source: C, toSerialize: JsonElement) {
    }

    /**
     * Override this to muck with `deserialized` before it parsed into the application type.
     */
    protected open fun afterRead(deserialized: JsonElement) {
    }
}