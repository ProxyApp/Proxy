package com.proxy.api.service;

import com.proxy.api.domain.model.Channel;
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
 * Created by Evan on 5/15/15.
 */
public interface ChannelService {
    /**
     * Get a {@link User}'s {@link Group}s.
     *
     * @param userId unique id for {@link User} table
     */
    @GET("/users/{id}/channels.json")
    Observable<Map<String, Channel>> getUserChannels(@Path("id") String userId);

    /**
     * add a {@link Channel}.
     *
     * @param userId unique id for {@link User} table
     */
    @PUT("/users/{id}/channels/{id}.json")
    Observable<Channel> addUserChannel(
        @Path("id") String userId, @Path("id") String channelId, @Body Channel channel);

    @DELETE("/users/{id}/channels/{id}.json")
    Observable<Group> deleteUserChannel(
        @Path("id") String userId, @Path("groupId") String channelId);
}
