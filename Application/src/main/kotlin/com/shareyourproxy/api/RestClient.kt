package com.shareyourproxy.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shareyourproxy.Constants
import com.shareyourproxy.api.domain.factory.UserTypeAdapterFactory
import com.shareyourproxy.api.service.HerokuInterceptor
import com.shareyourproxy.api.service.HerokuUserService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import timber.log.Timber

/**
 * Rest client for users.
 */
internal final class RestClient(private val context: Context) {
    val HEROKU_URL = "https://proxy-api.herokuapp.com/"
    val herokuUserService: HerokuUserService get() = buildClient(HEROKU_URL).create(HerokuUserService::class.java)

    private fun buildClient(endPoint: String): Retrofit {
        return Retrofit.Builder().baseUrl(endPoint).client(client).addConverterFactory(GsonConverterFactory.create(buildGsonConverter())).addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build()
    }

    private fun buildGsonConverter(): Gson {
        return GsonBuilder().registerTypeAdapterFactory(UserTypeAdapterFactory).create()
    }

    val client: OkHttpClient get() {
        val client = OkHttpClient().newBuilder()
                .addNetworkInterceptor(HerokuInterceptor(context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE)))
                .addNetworkInterceptor(httpLoggingInterceptor)
        return client.build()
    }

    val oldClient: com.squareup.okhttp.OkHttpClient get() {
        return com.squareup.okhttp.OkHttpClient()
    }

    private val httpLoggingInterceptor: HttpLoggingInterceptor get() {
        val logging = HttpLoggingInterceptor(timberLogger)
        logging.setLevel(BASIC)
        return logging
    }

    private val timberLogger: HttpLoggingInterceptor.Logger get() = HttpLoggingInterceptor.Logger { message -> Timber.tag("OkHttp").d(message) }
}
