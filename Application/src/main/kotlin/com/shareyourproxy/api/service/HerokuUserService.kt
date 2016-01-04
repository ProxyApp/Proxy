package com.shareyourproxy.api.service

import com.google.android.gms.plus.model.people.Person
import com.shareyourproxy.api.domain.model.*
import com.shareyourproxy.api.service.HerokuPaths.GOOGLE_PERSON
import com.shareyourproxy.api.service.HerokuPaths.MESSAGES
import com.shareyourproxy.api.service.HerokuPaths.SEARCH
import com.shareyourproxy.api.service.HerokuPaths.SHARED
import com.shareyourproxy.api.service.HerokuPaths.USER
import com.shareyourproxy.api.service.HerokuPaths.USERS
import com.shareyourproxy.api.service.HerokuPaths.USER_CHANNELS
import com.shareyourproxy.api.service.HerokuPaths.USER_CONTACTS
import com.shareyourproxy.api.service.HerokuPaths.USER_FOLLOWER_COUNT
import com.shareyourproxy.api.service.HerokuPaths.USER_GROUPS
import com.shareyourproxy.api.service.HerokuPaths.USER_SHARED
import com.shareyourproxy.api.service.HerokuPaths.USER_VERSION
import retrofit2.http.*
import rx.Observable
import java.util.*

/**
 * Connect to Proxy API.
 */
internal interface HerokuUserService {
    /**
     * Get a specific [User] based of their UUID.
     * @return user observable
     */
    @GET(USER)
    fun getUser(@Query("id") userId: String): Observable<User>

    /**
     * Save a [User].
     * @param userId unique id for [User] table
     * @param user   [User] data
     */
    @PUT(USER)
    fun updateUser(@Query("userId") userId: String, @Body user: User): Observable<User>

    /**
     * Update multiple [User] [Group]s.
     */
    @PUT(USER_GROUPS)
    fun updateUserGroups(@Query("userId") userId: String, @Body group: HashMap<String, Group>): Observable<Group>

    /**
     * Add a [User] [Group].
     */
    @PUT(USER_GROUPS)
    fun addUserGroup(@Query("userId") userId: String, @Body group: Group): Observable<Group>

    /**
     * Delete a [User] [Group].
     */
    @DELETE(USER_GROUPS)
    fun deleteUserGroup(@Query("userId") userId: String, @Query("groupId") groupId: String): Observable<Group>

    /**
     * Add a [User] contact id.
     * @param userId unique id for [User] table
     */
    @PUT(USER_CONTACTS)
    fun addUserContact(@Query("userId") userId: String, @Body contactId: String): Observable<String>

    /**
     * Delete a [User] contact.
     */
    @DELETE(USER_CONTACTS)
    fun deleteUserContact(@Query("userId") userId: String, @Query("contactId") contactId: String): Observable<String>

    /**
     * Add a [Channel].
     * @param userId unique id for [User] table
     */
    @PUT(USER_CHANNELS)
    fun addUserChannel(@Query("userId") userId: String, @Body channel: Channel): Observable<Channel>

    /**
     * Add multiple [Channel]s.
     * @param userId unique id for [User] table
     */
    @PUT(USER_CHANNELS)
    fun addUserChannels(@Query("userId") userId: String, @Body channel: HashMap<String, Channel>): Observable<HashMap<String, Channel>>

    /**
     * Get a [User]'s [Group]s.
     * @param userId unique id for [User] table
     */
    @GET(USER_CHANNELS)
    fun listGroupChannels(@Query("userId") userId: String, @Query("groupId") groupId: String): Observable<HashMap<String, Channel>>

    /**
     * Add a [Channel].
     * @param userId unique id for [User] table
     */
    @PUT(USER_CHANNELS)
    fun addGroupChannel(@Query("userId") userId: String, @Query("groupId") groupId: String, @Body channels: ArrayList<String>): Observable<AbstractMap.SimpleEntry<String, String>>

    /**
     * Delete a [User]s [Channel]s
     */
    @DELETE(USER_CHANNELS)
    fun deleteUserChannel(@Query("userId") userId: String, @Body channelId: String): Observable<Channel>

    /**
     * Get a users shared link.
     * @return user observable
     */
    @GET(USER_SHARED)
    fun getSharedLink(@Query("userId") userId: String, @Query("groupId") groupId: String): Observable<SharedLink>

    /**
     * Get the total number of user followers.
     * @return user observable
     */
    @GET(USER_FOLLOWER_COUNT)
    fun userFollowerCount(@Query("userId") userId: String): Observable<Int>
    /**
     * Save a user's android version.
     * @param userId unique id for [User] table
     */
    @PUT(USER_VERSION)
    fun updateUserVersion(@Query("userId") userId: String, @Body version: Int): Observable<String>

    /**
     * Add a [SharedLink]
     * @param sharedId shared link identifier
     */
    @PUT(SHARED)
    fun addSharedLink(@Path("sharedId") sharedId: String, @Body link: SharedLink): Observable<SharedLink>

    /**
     * Delete Shared Link
     */
    @DELETE(SHARED)
    fun deleteSharedLink(@Path("sharedId") sharedId: String): Observable<SharedLink>

    /**
     * Get a user's messages.
     */
    @GET(MESSAGES)
    fun getUserMessages(@Path("userId") userId: String): Observable<HashMap<String, Message>>

    /**
     * Add a user message.
     */
    @PUT(MESSAGES)
    fun addUserMessage(@Path("userId") userId: String, @Body message: HashMap<String, Message>): Observable<HashMap<String, Message>>

    /**
     * Delete a user's messages.
     */
    @DELETE(MESSAGES)
    fun deleteAllUserMessages(@Query("userId") userId: String): Observable<Message>

    /**
     * Return a list of [User]s from an input set of UUIDs.
     * @return array list of users observable
     */
    @GET(USERS)
    fun listUsers(@Query("users") users: HashSet<String>): Observable<ArrayList<User>>

    /**
     * Search for users who's first, last and first + last name match the query string.
     * @return user observable
     */
    @GET(SEARCH)
    fun searchUsers(@Query("name") name: String): Observable<ArrayList<User>>

    /**
     * Get basic profile information from a google plus user.
     */
    @GET(GOOGLE_PERSON)
    fun getGooglePlusPerson(@Query("userId") userId: String): Observable<Person>
}
