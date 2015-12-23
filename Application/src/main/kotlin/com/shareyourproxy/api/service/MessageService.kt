package com.shareyourproxy.api.service


import com.shareyourproxy.api.domain.model.Message
import retrofit.http.*
import rx.Observable
import java.util.*

/**
 * Service to get and update messages.
 */
interface MessageService {
    @GET("/messages/{userId}.json")
    fun getUserMessages(@Path("userId") userId: String): Observable<HashMap<String, Message>>

    @PUT("/messages/{userId}.json")
    fun addUserMessage(
            @Path("userId") userId: String, @Body message: HashMap<String, Message>): Observable<HashMap<String, Message>>

    @DELETE("/messages/{userId}.json")
    fun deleteAllUserMessages(@Path("userId") userId: String): Observable<Message>
}
