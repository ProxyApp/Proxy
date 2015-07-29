package com.shareyourproxy.api.service;


import com.shareyourproxy.api.domain.model.Message;

import java.util.HashMap;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Service to get and update messages.
 */
public interface MessageService {
    @GET("/messages/{userId}.json")
    Observable<HashMap<String, Message>> getUserMessages(@Path("userId") String userId);

    @PUT("/messages/{userId}.json")
    Observable<HashMap<String, Message>> addUserMessage(
        @Path("userId") String userId, @Body HashMap<String, Message> message);

    @DELETE("/messages/{userId}.json")
    Observable<Message> deleteAllUserMessages(@Path("userId") String userId);
}
