package com.shareyourproxy.api.service

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path
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
