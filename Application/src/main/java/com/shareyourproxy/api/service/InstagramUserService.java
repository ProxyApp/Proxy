package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.InstagramUser;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Instagram user api.
 */
public interface InstagramUserService {
    @GET("/users/{user_id}")
    Observable<InstagramUser> getUser(
        @Path("user_id") String userId,
        @Query("access_token") String accessToken);
}
