package com.shareyourproxy.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.Constants;
import com.shareyourproxy.api.domain.factory.AutoValueTypeAdapterFactory;
import com.shareyourproxy.api.domain.factory.UserTypeAdapterFactory;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.service.HerokuUserService;
import com.shareyourproxy.api.service.InstagramAuthService;
import com.shareyourproxy.api.service.MessageService;
import com.shareyourproxy.api.service.SharedLinkService;
import com.shareyourproxy.api.service.SpotifyAuthService;
import com.shareyourproxy.api.service.SpotifyUserService;
import com.shareyourproxy.api.service.UserChannelService;
import com.shareyourproxy.api.service.UserContactService;
import com.shareyourproxy.api.service.UserGroupService;
import com.shareyourproxy.api.service.UserService;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Rest client for users.
 */
public class RestClient {

    //    public static final String INSTAGRAM_URL = "https://api.instagram.com/v1";
    public static final String SPOTIFY_URL = "https://api.spotify.com/v1";
    public static final String SPOTIFY_AUTH_URL = "https://accounts.spotify.com/api";
    public static final String INSTAGRAM_AUTH_URL = "https://api.instagram.com/oauth/";
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
    public static UserService getUserService(Context context) {
        return buildRestClient(context, buildGsonConverter())
            .create(UserService.class);
    }

    public static UserGroupService getUserGroupService(Context context) {
        return buildRestClient(context, buildGsonConverter())
            .create(UserGroupService.class);
    }

    public static UserChannelService getUserChannelService(Context context) {
        return buildRestClient(context, buildGsonConverter())
            .create(UserChannelService.class);
    }

    public static UserContactService getUserContactService(Context context) {
        return buildRestClient(context, buildGsonConverter())
            .create(UserContactService.class);
    }

    public static MessageService getMessageService(Context context) {
        return buildRestClient(context, buildGsonConverter())
            .create(MessageService.class);
    }

    public static SharedLinkService getSharedLinkService(Context context) {
        return buildRestClient(context, buildGsonConverter())
            .create(SharedLinkService.class);
    }

    public static InstagramAuthService getInstagramAuthService(Context context) {
        return buildInstagramAuthClient(context, buildGsonConverter())
            .create(InstagramAuthService.class);
    }

    public static SpotifyUserService getSpotifyUserService(Context context) {
        return buildSpotifyUserClient(context, buildGsonConverter())
            .create(SpotifyUserService.class);
    }

    public static SpotifyAuthService getSpotifyAuthService(Context context) {
        return buildSpotifyAuthClient(context, buildGsonConverter())
            .create(SpotifyAuthService.class);
    }

    public static HerokuUserService getHerokuUserervice(Context context) {
        return buildHerokuRestClient(context, buildGsonConverter())
            .create(HerokuUserService.class);
    }

    public static RestAdapter buildRestClient(Context context, Gson gson) {
        SharedPreferences sharedPrefs =
            context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE);
        RxBusDriver rxBus = RxBusDriver.getInstance();
        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient(rxBus, sharedPrefs)))
            .setEndpoint(BuildConfig.FIREBASE_ENDPOINT)
            .setConverter(new GsonConverter(gson))
            .setRequestInterceptor(new FirebaseInterceptor(sharedPrefs))
            .build();
    }

    public static RestAdapter buildHerokuRestClient(
        Context context, Gson gson) {
        SharedPreferences sharedPrefs =
            context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE);
        RxBusDriver rxBus = RxBusDriver.getInstance();
        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient(rxBus, sharedPrefs)))
            .setEndpoint(HEROKU_URL)
            .setConverter(new GsonConverter(gson))
            .setRequestInterceptor(new FirebaseInterceptor(sharedPrefs))
            .build();
    }

    public static RestAdapter buildInstagramAuthClient(Context context, Gson gson) {
        SharedPreferences sharedPrefs =
            context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE);
        RxBusDriver rxBus = RxBusDriver.getInstance();
        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient(rxBus, sharedPrefs)))
            .setEndpoint(INSTAGRAM_AUTH_URL)
            .setConverter(new GsonConverter(gson))
            .build();
    }

    public static RestAdapter buildSpotifyUserClient(Context context, Gson gson) {
        SharedPreferences sharedPrefs =
            context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE);
        RxBusDriver rxBus = RxBusDriver.getInstance();
        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient(rxBus, sharedPrefs)))
            .setEndpoint(SPOTIFY_URL)
            .setConverter(new GsonConverter(gson))
            .build();
    }

    public static RestAdapter buildSpotifyAuthClient(Context context, Gson gson) {
        SharedPreferences sharedPrefs =
            context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE);
        RxBusDriver rxBus = RxBusDriver.getInstance();
        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient(rxBus, sharedPrefs)))
            .setEndpoint(SPOTIFY_AUTH_URL)
            .setConverter(new GsonConverter(gson))
            .build();
    }

    public static Gson buildGsonConverter() {
        return new GsonBuilder()
            .registerTypeAdapterFactory(new AutoValueTypeAdapterFactory())
            .registerTypeAdapterFactory(new UserTypeAdapterFactory())
            .create();
    }

    public static OkHttpClient getClient(RxBusDriver rxBus, SharedPreferences sharedPrefs) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(10, TimeUnit.SECONDS);
        client.setAuthenticator(new FirebaseAuthenticator(rxBus, sharedPrefs));
        return client;
    }
}
