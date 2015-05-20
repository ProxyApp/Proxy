package com.proxy.api.service;

import com.proxy.api.domain.model.Group;
import com.proxy.api.domain.model.User;

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
    @GET("/users/{id}/groups.json")
    Observable<Map<String, Group>> getUserGroups(@Path("id") String userId);

    @PUT("/users/{id}/groups/{groupId}.json")
    Observable<Group> addUserGroup(
        @Path("id") String userId, @Path("groupId") String groupId, @Body Group group);

    @DELETE("/users/{id}/groups/{groupId}.json")
    Observable<Group> deleteUserGroup(
        @Path("id") String userId, @Path("groupId") String groupId);
}
