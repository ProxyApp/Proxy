package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Save and get group channels.
 */
public interface GroupChannelService {
    /**
     * Get a {@link User}'s {@link Group}s.
     *
     * @param userId unique id for {@link User} table
     */
    @GET("/users/{userId}/groups/{groupId}/channels.json")
    Observable<HashMap<String, Channel>> listGroupChannels(
        @Path("userId") String userId, @Path("groupId") String groupId);

    /**
     * add a {@link Channel}.
     *
     * @param userId unique id for {@link User} table
     */
    @PUT("/users/{userId}/groups/{groupId}/channels.json")
    Observable<HashMap<String, Channel>> addGroupChannels(
        @Path("userId") String userId, @Path("groupId") String groupId,
        @Body ArrayList<Channel> channels);
}
