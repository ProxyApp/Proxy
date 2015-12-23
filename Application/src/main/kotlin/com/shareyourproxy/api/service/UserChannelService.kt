package com.shareyourproxy.api.service

import com.shareyourproxy.api.domain.model.Channel
import retrofit.http.Body
import retrofit.http.DELETE
import retrofit.http.PUT
import retrofit.http.Path
import rx.Observable
import java.util.*

/**
 * Created by Evan on 5/15/15.
 */
interface UserChannelService {
    /**
     * add a [Channel].
     * @param userId unique id for [User] table
     */
    @PUT("/users/{userId}/channels/{channelId}.json")
    fun addUserChannel(@Path("userId") userId: String, @Path("channelId") channelId: String, @Body channel: Channel): Observable<Channel>

    /**
     * add multiple [Channel]s.
     * @param userId unique id for [User] table
     */
    @PUT("/users/{userId}/channels.json")
    fun addUserChannels(@Path("userId") userId: String, @Body channel: HashMap<String, Channel>): Observable<HashMap<String, Channel>>

    @DELETE("/users/{userId}/channels/{channelId}.json")
    fun deleteUserChannel(@Path("userId") userId: String, @Path("channelId") channelId: String): Observable<Channel>
}
