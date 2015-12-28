package com.shareyourproxy.api.service

import com.google.android.gms.plus.model.people.Person
import com.shareyourproxy.api.domain.model.SharedLink
import com.shareyourproxy.api.domain.model.User
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable
import java.util.*

/**
 * Created by Evan on 10/26/15.
 */
interface HerokuUserService {
    /**
     * Return a list of users.
     * @return user observable
     */
    @GET("/users")
    fun listUsers(@Query("users") users: HashSet<String>): Observable<ArrayList<User>>

    /**
     * Search for users who's first, last and first + last name match the query string.
     * @return user observable
     */
    @GET("/users/search")
    fun searchUsers(@Query("name") name: String): Observable<ArrayList<User>>

    /**
     * Get a specific user based of their UUID.
     * @return user observable
     */
    @GET("/users/user")
    fun getUser(@Query("id") userId: String): Observable<ArrayList<User>>

    /**
     * Get the total number of user followers.
     * @return user observable
     */
    @GET("/users/user/following")
    fun userFollowerCount(@Query("id") userId: String): Observable<Int>

    /**
     * Get the total number of user followers.
     * @return user observable
     */
    @GET("/users/user/shared")
    fun getSharedLink(@Query("groupId") groupId: String, @Query("userId") userId: String): Observable<SharedLink>

    /**
     * Get the total number of user followers.
     * @return user observable
     */
    @PUT("/shared")
    fun putSharedLinks(@Query("groupId") groupIds: ArrayList<String>, @Query("userId") userId: String): Observable<String>

    @GET("/current")
    fun getCurrentPerson(@Path("userId") userId: String): Observable<Person>
}
