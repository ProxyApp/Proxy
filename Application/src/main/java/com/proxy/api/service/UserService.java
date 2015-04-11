package com.proxy.api.service;


import com.proxy.api.model.User;

import java.util.Map;

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
    @GET("/users.json")
    Observable<Map<String, User>> listUsers();

    /**
     * Get a specific user.
     *
     * @param userId   user unique identifier
     * @param callback async callback
     * @return user
     */
    @GET("/users/{userId}.json")
    void getUser(@Path("userId") String userId, Callback<User> callback);

    /**
     * Save a user.
     *
     * @param userId   unique id for {@link User} table
     * @param user     {@link User} data
     * @param callback registerUser callback
     */
    @PUT("/users/{userId}.json")
    void updateUser(@Path("userId") String userId, @Body User user, Callback<User> callback);

}
