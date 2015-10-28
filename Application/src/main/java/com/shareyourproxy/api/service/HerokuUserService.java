package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.SharedLink;
import com.shareyourproxy.api.domain.model.User;

import java.util.ArrayList;
import java.util.HashSet;

import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Evan on 10/26/15.
 */
public interface HerokuUserService {
    /**
     * Return a list of users.
     *
     * @return user observable
     */
    @GET("/users")
    Observable<ArrayList<User>> listUsers(@Query("users") HashSet<String> users);

    /**
     * Search for users who's first, last and first + last name match the query string.
     *
     * @return user observable
     */
    @GET("/users/search")
    Observable<ArrayList<User>> searchUsers(@Query("name") String name);

    /**
     * Get a specific user based of their UUID.
     *
     * @return user observable
     */
    @GET("/users/user")
    Observable<ArrayList<User>> getUser(@Query("id") String userId);

    /**
     * Get the total number of user followers.
     *
     * @return user observable
     */
    @GET("/users/user/following")
    Observable<Integer> userFollowerCount(@Query("id") String userId);

    /**
     * Get the total number of user followers.
     *
     * @return user observable
     */
    @GET("/users/user/shared")
    Observable<SharedLink> getSharedLink(
        @Query("groupId") String groupId, @Query("userId") String userId);

    /**
     * Get the total number of user followers.
     *
     * @return user observable
     */
    @PUT("/shared")
    Observable<String> putSharedLinks(
        @Query("groupId") ArrayList<String> groupIds, @Query("userId") String userId);
}
