package com.shareyourproxy.api.service

import com.shareyourproxy.api.domain.model.Group
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path
import rx.Observable
import java.util.*

/**
 * Group services for [Group]s.
 */
interface UserGroupService {

    @PUT("/users/{userId}/groups.json")
    fun updateUserGroups(@Path("userId") userId: String, @Body group: HashMap<String, Group>): Observable<Group>

    @PUT("/users/{userId}/groups/{groupId}.json")
    fun addUserGroup(@Path("userId") userId: String, @Path("groupId") groupId: String, @Body group: Group): Observable<Group>

    @DELETE("/users/{userId}/groups/{groupId}.json")
    fun deleteUserGroup(@Path("userId") userId: String, @Path("groupId") groupId: String): Observable<Group>
}
