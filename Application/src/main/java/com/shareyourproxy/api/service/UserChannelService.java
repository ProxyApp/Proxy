package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

import java.util.HashMap;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by Evan on 5/15/15.
 */
public interface UserChannelService {
    /**
     * add a {@link Channel}.
     *
     * @param userId unique id for {@link User} table
     */
    @PUT("/users/{userId}/channels/{channelId}.json")
    Observable<Channel> addUserChannel(
        @Path("userId") String userId, @Path("channelId") String channelId, @Body Channel channel);

    /**
     * add multiple {@link Channel}s.
     *
     * @param userId unique id for {@link User} table
     */
    @PUT("/users/{userId}/channels.json")
    Observable<HashMap<String, Channel>> addUserChannels(
        @Path("userId") String userId, @Body HashMap<String, Channel> channel);

    @DELETE("/users/{userId}/channels/{channelId}.json")
    Observable<Channel> deleteUserChannel(
        @Path("userId") String userId, @Path("channelId") String channelId);
}
