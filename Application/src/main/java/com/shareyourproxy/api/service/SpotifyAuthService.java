package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.SpotifyAuthResponse;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by Evan on 8/14/15.
 */
public interface SpotifyAuthService {
    @FormUrlEncoded
    @POST("/token")
    @Headers({"Authorization: Basic {authCode}"})
    Observable<SpotifyAuthResponse> getAuth(
        @Header("authCode") String authHeader,
        @Field("grant_type") String grantType,
        @Field("code") String code,
        @Field("redirect_uri") String redirectUri);

    @POST("/token")
    @FormUrlEncoded
    Observable<SpotifyAuthResponse> getAuth(
        @Field("client_id") String clientId,
        @Field("client_secret") String clientSecret,
        @Field("grant_type") String grantType,
        @Field("code") String code,
        @Field("redirect_uri") String redirectUri);
}
