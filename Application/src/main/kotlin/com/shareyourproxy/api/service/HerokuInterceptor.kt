package com.shareyourproxy.api.service

import android.content.SharedPreferences
import com.shareyourproxy.Constants.KEY_GOOGLE_PLUS_AUTH
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Add Heroku authentication headers.
 */
internal final class HerokuInterceptor(private val sharedPrefs: SharedPreferences) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sharedPrefs.getString(KEY_GOOGLE_PLUS_AUTH, null)
        if (token != null) {
            val newRequest = chain.request().newBuilder()
                    .addHeader("token", token)
                    .build()
            return chain.proceed(newRequest)
        } else {
            return chain.proceed(chain.request())
        }
    }
}

