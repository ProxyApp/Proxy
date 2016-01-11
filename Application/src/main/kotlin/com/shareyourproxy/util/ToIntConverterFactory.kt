package com.shareyourproxy.util

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal final class ToIntConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
        if (Integer::class.java == type) {
            return Converter<ResponseBody, Int> { it.string().toInt() }
        }
        return null
    }

    override fun requestBodyConverter(type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody>? {
        if (Integer::class.java == type) {
            return Converter<Int, RequestBody> { RequestBody.create(MEDIA_TYPE, it.toString()) }
        }
        return null
    }

    companion object {
        val MEDIA_TYPE = MediaType.parse("text/plain")
    }
}