package com.shareyourproxy.api.service

import com.shareyourproxy.api.domain.model.Channel
import retrofit.http.Body
import retrofit.http.GET
import retrofit.http.PUT
import retrofit.http.Path
import rx.Observable
import java.util.*
import java.util.AbstractMap.SimpleEntry

/**
 * Save and get group channels.
 */
interface GroupChannelService {
    /**
     * Get a [User]'s [Group]s.
     * @param userId unique id for [User] table
     */
    @GET("/users/{userId}/groups/{groupId}/channels.json")
    fun listGroupChannels(@Path("userId") userId: String, @Path("groupId") groupId: String): Observable<HashMap<String, Channel>>

    /**
     * add a [Channel].
     * @param userId unique id for [User] table
     */
    @PUT("/users/{userId}/groups/{groupId}/channels.json")
    fun addGroupChannel(@Path("userId") userId: String, @Path("groupId") groupId: String, @Body channels: ArrayList<String>): Observable<SimpleEntry<String, String>>
}
