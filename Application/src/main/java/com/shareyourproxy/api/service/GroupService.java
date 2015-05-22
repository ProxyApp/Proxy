package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;

import java.util.Map;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Group services for {@link Group}s.
 */
public interface GroupService {

    /**
     * Get a {@link User}'s {@link Group}s.
     *
     * @param userId unique id for {@link User} table
     */
    @GET("/users/{userId}/groups.json")
    Observable<Map<String, Group>> listUserGroups(@Path("userId") String userId);

    @PUT("/users/{userId}/groups/{groupId}.json")
    Observable<Group> addUserGroup(
        @Path("userId") String userId, @Path("groupId") String groupId, @Body Group group);

    @DELETE("/users/{userId}/groups/{groupId}.json")
    Observable<Group> deleteUserGroup(
        @Path("userId") String userId, @Path("groupId") String groupId);
}
