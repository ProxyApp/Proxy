package com.shareyourproxy.api.service

import com.google.android.gms.plus.model.people.Person
import com.shareyourproxy.api.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable
import java.util.*

/**
 * Connect to Proxy API.
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
    fun getUser(@Query("id") userId: String): Observable<User>

    //TODO
    /**
     * Save a user.
     * @param userId unique id for [User] table
     * @param user   [User] data
     */
    //    @PUT("/users/{id}.json")
    fun updateUser(@Path("id") userId: String, @Body user: User): Observable<User>

    //TODO
    /**
     * Save a user.
     * @param userId unique id for [User] table
     */
    //    @PUT("/users/{id}/version.json")
    fun updateUserVersion(@Path("id") userId: String, @Body version: Int?): Observable<String>

    //TODO
    //    @PUT("/users/{userId}/groups.json")
    fun updateUserGroups(@Path("userId") userId: String, @Body group: HashMap<String, Group>): Observable<Group>

    //TODO
    //    @PUT("/users/{userId}/groups/{groupId}.json")
    fun addUserGroup(@Path("userId") userId: String, @Path("groupId") groupId: String, @Body group: Group): Observable<Group>

    //TODO
    //    @DELETE("/users/{userId}/groups/{groupId}.json")
    fun deleteUserGroup(@Path("userId") userId: String, @Path("groupId") groupId: String): Observable<Group>
    //TODO
    /**
     * add a User contact id.
     * @param userId unique id for [User] table
     */
    //    @PUT("/users/{userId}/contacts.json")
    fun addUserContact(@Path("userId") userId: String, @Body contactId: String): Observable<String>

    //TODO
    //    @DELETE("/users/{userId}/contacts/{contactId}.json")
    fun deleteUserContact(@Path("userId") userId: String, @Path("contactId") contactId: String): Observable<String>
    //TODO
    /**
     * add a [Channel].
     * @param userId unique id for [User] table
     */
    //    @PUT("/users/{userId}/channels/{channelId}.json")
    fun addUserChannel(@Path("userId") userId: String, @Path("channelId") channelId: String, @Body channel: Channel): Observable<Channel>
    //TODO
    /**
     * add multiple [Channel]s.
     * @param userId unique id for [User] table
     */
    //    @PUT("/users/{userId}/channels.json")
    fun addUserChannels(@Path("userId") userId: String, @Body channel: HashMap<String, Channel>): Observable<HashMap<String, Channel>>

    //TODO
    //    @DELETE("/users/{userId}/channels/{channelId}.json")
    fun deleteUserChannel(@Path("userId") userId: String, @Path("channelId") channelId: String): Observable<Channel>

    //TODO
    /**
     * Get the total number of user followers.
     * @return user observable
     */
    //    @GET("/users/user/following")
    fun userFollowerCount(@Query("id") userId: String): Observable<Int>

    //TODO
    /**
     * Get the total number of user followers.
     * @return user observable
     */
    //    @GET("/users/user/shared")
    fun getSharedLink(@Query("groupId") groupId: String, @Query("userId") userId: String): Observable<SharedLink>
    //TODO
    /**
     * Get the total number of user followers.
     * @return user observable
     */
    //    @PUT("/shared")
    fun putSharedLinks(@Query("groupId") groupIds: ArrayList<String>, @Query("userId") userId: String): Observable<String>

    //TODO
    //    @GET("/current")
    fun getCurrentPerson(@Path("userId") userId: String): Observable<Person>
    //TODO
    /**
     * Get a [User]'s [Group]s.
     * @param userId unique id for [User] table
     */
    //    @GET("/users/{userId}/groups/{groupId}/channels.json")
    fun listGroupChannels(@Path("userId") userId: String, @Path("groupId") groupId: String): Observable<HashMap<String, Channel>>
    //TODO
    /**
     * add a [Channel].
     * @param userId unique id for [User] table
     */
    //    @PUT("/users/{userId}/groups/{groupId}/channels.json")
    fun addGroupChannel(@Path("userId") userId: String, @Path("groupId") groupId: String, @Body channels: ArrayList<String>): Observable<AbstractMap.SimpleEntry<String, String>>

    //TODO
    //    @GET("/shared.json")
    fun sharedLinks(): Observable<HashMap<String, SharedLink>>
    //TODO
    /**
     * add a [SharedLink]
     * @param sharedId shared link identifier
     */
    //    @PUT("/shared/{sharedId}.json")
    fun addSharedLink(@Path("sharedId") sharedId: String, @Body link: SharedLink): Observable<SharedLink>

    //TODO
    //    @DELETE("/shared/{sharedId}.json")
    fun deleteSharedLink(@Path("sharedId") sharedId: String): Observable<SharedLink>
    //TODO
    //    @GET("/messages/{userId}.json")
    fun getUserMessages(@Path("userId") userId: String): Observable<HashMap<String, Message>>
    //TODO
    //    @PUT("/messages/{userId}.json")
    fun addUserMessage(@Path("userId") userId: String, @Body message: HashMap<String, Message>): Observable<HashMap<String, Message>>
    //TODO
    //    @DELETE("/messages/{userId}.json")
    fun deleteAllUserMessages(@Path("userId") userId: String): Observable<Message>
}
