package com.shareyourproxy.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shareyourproxy.BuildConfig.FIREBASE_ENDPOINT
import com.shareyourproxy.api.domain.factory.UserTypeAdapterFactory
import com.shareyourproxy.api.service.*
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
object RestClient {
    val HEROKU_URL = "https://proxy-api.herokuapp.com/"

    /**
     * Get the [UserService].
     * @return userService
     */
    val userService: UserService get() = buildClient(FIREBASE_ENDPOINT).create(UserService::class.java)

    val userGroupService: UserGroupService get() = buildClient(FIREBASE_ENDPOINT).create(UserGroupService::class.java)

    val userChannelService: UserChannelService get() = buildClient(FIREBASE_ENDPOINT).create(UserChannelService::class.java)

    val userContactService: UserContactService get() = buildClient(FIREBASE_ENDPOINT).create(UserContactService::class.java)

    val messageService: MessageService get() = buildClient(FIREBASE_ENDPOINT).create(MessageService::class.java)

    val sharedLinkService: SharedLinkService get() = buildClient(FIREBASE_ENDPOINT).create(SharedLinkService::class.java)

    val herokuUserService: HerokuUserService get() = buildClient(HEROKU_URL).create(HerokuUserService::class.java)

    private fun buildClient(endPoint: String): Retrofit {
        return Retrofit.Builder().baseUrl(endPoint).client(client).addConverterFactory(GsonConverterFactory.create(buildGsonConverter())).addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build()
    }

    private fun buildGsonConverter(): Gson {
        return GsonBuilder().registerTypeAdapterFactory(UserTypeAdapterFactory()).create()
    }

    val client: OkHttpClient get() {
        val client = OkHttpClient()
        client.networkInterceptors().add(httpLoggingInterceptor)
        return client
    }

    val oldClient: com.squareup.okhttp.OkHttpClient get() {
        val oldClient =com.squareup.okhttp.OkHttpClient()
        client.networkInterceptors().add(httpLoggingInterceptor)
        return oldClient
    }

    private val httpLoggingInterceptor: HttpLoggingInterceptor get() {
        val logging = HttpLoggingInterceptor(timberLogger)
        logging.setLevel(BASIC)
        return logging
    }

    private val timberLogger: HttpLoggingInterceptor.Logger get() = HttpLoggingInterceptor.Logger { message -> Timber.tag("OkHttp").d(message) }
}
