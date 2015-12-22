package com.shareyourproxy.api;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareyourproxy.api.domain.factory.AutoValueTypeAdapterFactory;
import com.shareyourproxy.api.domain.factory.UserTypeAdapterFactory;
import com.shareyourproxy.api.service.HerokuUserService;
import com.shareyourproxy.api.service.MessageService;
import com.shareyourproxy.api.service.SharedLinkService;
import com.shareyourproxy.api.service.UserChannelService;
import com.shareyourproxy.api.service.UserContactService;
import com.shareyourproxy.api.service.UserGroupService;
import com.shareyourproxy.api.service.UserService;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import timber.log.Timber;

import static com.shareyourproxy.BuildConfig.FIREBASE_ENDPOINT;
import static com.squareup.okhttp.logging.HttpLoggingInterceptor.Level.BASIC;

/**
 * Rest client for users.
 */
public class RestClient {
    public static final String HEROKU_URL = "https://proxy-api.herokuapp.com/";

    /**
     * Constructor.
     */
    private RestClient() {
    }

    /**
     * Get the {@link UserService}.
     *
     * @return userService
     */
    public static UserService getUserService() {
        return buildClient(FIREBASE_ENDPOINT).create(UserService.class);
    }

    public static UserGroupService getUserGroupService() {
        return buildClient(FIREBASE_ENDPOINT).create(UserGroupService.class);
    }

    public static UserChannelService getUserChannelService() {
        return buildClient(FIREBASE_ENDPOINT).create(UserChannelService.class);
    }

    public static UserContactService getUserContactService() {
        return buildClient(FIREBASE_ENDPOINT).create(UserContactService.class);
    }

    public static MessageService getMessageService() {
        return buildClient(FIREBASE_ENDPOINT).create(MessageService.class);
    }

    public static SharedLinkService getSharedLinkService() {
        return buildClient(FIREBASE_ENDPOINT).create(SharedLinkService.class);
    }

    public static HerokuUserService getHerokuUserService() {
        return buildClient(HEROKU_URL).create(HerokuUserService.class);
    }

    private static Retrofit buildClient(String endPoint) {
        return new Retrofit.Builder()
            .baseUrl(endPoint)
            .client(getClient())
            .addConverterFactory(GsonConverterFactory.create(buildGsonConverter()))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();
    }

    private static Gson buildGsonConverter() {
        return new GsonBuilder()
            .registerTypeAdapterFactory(new AutoValueTypeAdapterFactory())
            .registerTypeAdapterFactory(new UserTypeAdapterFactory())
            .create();
    }

    public static OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(getHttpLoggingInterceptor());
        return client;
    }

    @NonNull
    private static HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(getTimberLogger());
        logging.setLevel(BASIC);
        return logging;
    }

    @NonNull
    private static HttpLoggingInterceptor.Logger getTimberLogger() {
        return new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Timber.tag("OkHttp").d(message);
            }
        };
    }
}
