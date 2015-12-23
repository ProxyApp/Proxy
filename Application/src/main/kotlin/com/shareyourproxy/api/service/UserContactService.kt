package com.shareyourproxy.api.service

import retrofit.http.Body
import retrofit.http.DELETE
import retrofit.http.PUT
import retrofit.http.Path
import rx.Observable

/**
 * Pokemon that a user wants to collect.
 */
interface UserContactService {
    /**
     * add a User contact id.
     * @param userId unique id for [User] table
     */
    @PUT("/users/{userId}/contacts.json")
    fun addUserContact(@Path("userId") userId: String, @Body contactId: String): Observable<String>

    @DELETE("/users/{userId}/contacts/{contactId}.json")
    fun deleteUserContact(@Path("userId") userId: String, @Path("contactId") contactId: String): Observable<String>

}
