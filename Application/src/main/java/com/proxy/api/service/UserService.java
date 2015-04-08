package com.proxy.api.service;


import com.proxy.api.model.User;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * User Observable.
 */
@SuppressWarnings("unused")
public interface UserService {
    /**
     * Return a list of users.
     *
     * @return user observable
     */
    @GET("/user/")
    Observable<User> listUser();

    /**
     * Get a specific user.
     *
     * @param userId   user unique identifier
     * @param callback async callback
     * @return user
     */
    @GET("/user/{userId}.json")
    User getUser(@Path("userId") String userId, Callback<User> callback);

    /**
     * Save a user.
     *
     * @param userId   unique id for users table
     * @param user     userData
     * @param callback registerUser callback
     */
    @PUT("/user/{userId}.json")
    void registerUser(@Path("userId") String userId, @Body User user, Callback<User> callback);
}
