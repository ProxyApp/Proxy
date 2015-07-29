package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.InstagramAuthResponse;

import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import rx.Observable;

/**
 * Get the access token
 */
public interface InstagramAuthService {
    @Multipart
    @POST("/access_token")
    Observable<InstagramAuthResponse> getAuth(
        @Part("client_id") String clientId,
        @Part("client_secret") String clientSecret,
        @Part("grant_type") String grantType,
        @Part("redirect_uri") String redirectUri,
        @Part("code") String code);
}
