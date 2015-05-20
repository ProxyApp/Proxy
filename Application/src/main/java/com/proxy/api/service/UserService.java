package com.proxy.api.service;


import com.proxy.api.domain.model.User;

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
    @GET("/users/{id}.json")
    Observable<User> getUser(@Path("id") String userId);

    /**
     * Save a user.
     *
     * @param userId   unique id for {@link User} table
     * @param user     {@link User} data
     */
    @PUT("/users/{id}.json")
    Observable<User> updateUser(@Path("id") String userId, @Body User user);

}
