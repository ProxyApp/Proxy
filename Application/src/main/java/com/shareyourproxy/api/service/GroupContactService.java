package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by Evan on 6/5/15.
 */
public interface GroupContactService {
    /**
     * add a {@link Channel}.
     *
     * @param userId unique id for {@link User} table
     */
    @PUT("/users/{userId}/groups/{groupId}/contacts/{contactId}.json")
    Observable<Contact> addGroupContact(
        @Path("userId") String userId, @Path("groupId") String groupId,
        @Path("contactId") String contactId, @Body Id contact);

    @DELETE("/users/{userId}/groups/{groupId}/contacts/{contactId}.json")
    Observable<Contact> deleteGroupContact(
        @Path("userId") String userId, @Path("groupId") String groupId,
        @Path("contactId") String contactId);
}
