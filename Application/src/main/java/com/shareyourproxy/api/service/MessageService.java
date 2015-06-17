package com.shareyourproxy.api.service;


import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Messages;

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
    @GET("/users/{userId}/messages.json")
    Observable<HashMap<String, Contact>> getUserMessages(@Path("userId") String userId);

    @PUT("/users/{userId}/messages/{messageId}.json")
    Observable<Messages> addUserMessage(
        @Path("userId") String userId, @Path("messageId") String messageId, @Body Contact contact);

    @DELETE("/users/{userId}/messages/{messageId}.json")
    Observable<Messages> deleteUserMessage(
        @Path("userId") String userId, @Path("messageId") String messageId);

    @DELETE("/users/{userId}/messages.json")
    Observable<Messages> deleteAllUserMessages(@Path("userId") String userId);
}
