package com.proxy.api.service;


import com.proxy.api.model.User;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * User Observable.
 */
@SuppressWarnings("unused")
public interface UserService {
    /**
     * Return a list of users.
     *
     * @param userId unique id for users table
     * @return user observable
     */
//    @GET("/user/{userId}/")
//    Observable<User> listUser(@Path("userId") String userId);


    /**
     * Return a list of users.
     *
     * @param userId   unique id for users table
     * @param user     userData
     * @param callback registerUser callback
     */
    @PUT("/user/{userId}.json")
    void registerUser(@Path("userId") String userId, @Body User user, Callback<User> callback);
}
