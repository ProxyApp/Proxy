package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;

import java.util.Map;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by Evan on 6/5/15.
 */
public interface GroupContactService {
    /**
     * Get a {@link User}'s {@link Group}s.
     *
     * @param userId unique id for {@link User} table
     */
    @GET("/users/{userId}/contactGroups/{groupId}/contacts.json")
    Observable<Map<String, Contact>> listGroupContacts(
        @Path("userId") String userId, @Path("groupId") String groupId);
    /**
     * add a {@link Channel}.
     *
     * @param userId unique id for {@link User} table
     */
    @PUT("/users/{userId}/contactGroups/{groupId}/contacts/{contactId}.json")
    Observable<Contact> addGroupContact(
        @Path("userId") String userId, @Path("groupId") String groupId,
        @Path("contactId") String contactId, @Body Contact contact);

    @DELETE("/users/{userId}/contactGroups/{groupId}/contacts/{contactId}.json")
    Observable<Contact> deleteGroupContact(
        @Path("userId") String userId, @Path("groupId") String groupId,
        @Path("contactId") String contactId);
}
