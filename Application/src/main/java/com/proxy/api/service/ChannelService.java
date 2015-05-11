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
     * @param userId unique userId for {@link User} table
     */
    @GET("/users/{userId}/channels.json")
    Observable<Map<String, Channel>> getUserChannels(@Path("userId") String userId);

    /**
     * add a {@link Channel}.
     *
     * @param userId unique userId for {@link User} table
     */
    @PUT("/users/{userId}/channels/{channelId}.json")
    Observable<Channel> addUserChannel(
        @Path("userId") String userId, @Path("channelId") String channelId, @Body Channel channel);

    @DELETE("/users/{userId}/channels/{channelId}.json")
    Observable<Group> deleteUserChannel(
        @Path("userId") String userId, @Path("groupId") String channelId);
}
