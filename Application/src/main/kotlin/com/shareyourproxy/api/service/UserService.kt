package com.shareyourproxy.api.service


import com.shareyourproxy.api.domain.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import rx.Observable
import java.util.*

/**
 * User Observable.
 */
@SuppressWarnings("unused")
interface UserService {
    /**
     * Return a list of users.
     * @return user observable
     */
    @GET("/users.json")
    fun listUsers(): Observable<HashMap<String, User>>

    /**
     * Get a specific user.
     * @param userId user unique identifier
     * @return user
     */
    @GET("/users/{id}.json")
    fun getUser(@Path("id") userId: String): Observable<User>

    /**
     * Save a user.

     * @param userId unique id for [User] table
     * *
     * @param user   [User] data
     */
    @PUT("/users/{id}.json")
    fun updateUser(@Path("id") userId: String, @Body user: User): Observable<User>

    /**
     * Save a user.

     * @param userId unique id for [User] table
     */
    @PUT("/users/{id}/version.json")
    fun updateUserVersion(@Path("id") userId: String, @Body version: Int?): Observable<String>
}
