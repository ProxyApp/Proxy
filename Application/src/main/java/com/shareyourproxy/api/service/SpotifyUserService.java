package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.SpotifyUser;

import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import rx.Observable;

/**
 * Created by Evan on 8/14/15.
 */
public interface SpotifyUserService {
    @GET("/me/")
    @Headers({"Accept: application/json","Content-Type: application/json",
        "Authorization: Bearer {authToken}"})
    Observable<SpotifyUser> getUser(@Header("authToken") String authToken);
}
