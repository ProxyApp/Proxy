package com.shareyourproxy.api.service;


import com.shareyourproxy.api.domain.model.User;

import java.util.HashMap;

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
    Observable<HashMap<String, User>> listUsers();

    /**
     * Get a specific user.
     *
     * @param userId   user unique identifier
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
