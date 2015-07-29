package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Pokemon that a user wants to collect.
 */
public interface UserContactService {
    /**
     * add a User {@link Contact}.
     *
     * @param userId unique id for {@link User} table
     */
    @PUT("/users/{userId}/contacts/{contactId}.json")
    Observable<String> addUserContact(
        @Path("userId") String userId, @Path("contactId") String contactId, @Body() Id contact);

    @DELETE("/users/{userId}/contacts/{contactId}.json")
    Observable<String> deleteUserContact(
        @Path("userId") String userId, @Path("contactId") String contactId);

}
