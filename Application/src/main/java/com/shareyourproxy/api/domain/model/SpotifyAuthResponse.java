package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;

import com.shareyourproxy.api.gson.AutoGson;

import auto.parcel.AutoParcel;

/**
 * Created by Evan on 8/14/15.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_SpotifyAuthResponse.class)
public abstract class SpotifyAuthResponse implements Parcelable {
    public static SpotifyAuthResponse create(
        String accessToken, String type, String expires, String refreshToken) {
        return builder().access_token(accessToken).token_type(type)
            .expires_in(expires).refresh_token(refreshToken).build();
    }

    public static Builder builder() {
        return new AutoParcel_SpotifyAuthResponse.Builder();
    }

    public abstract String access_token();

    public abstract String token_type();

    public abstract String expires_in();

    public abstract String refresh_token();

    @AutoParcel.Builder
    public interface Builder {

        Builder access_token(String token);

        Builder token_type(String type);

        Builder expires_in(String time);

        Builder refresh_token(String token);

        SpotifyAuthResponse build();
    }
}

