package com.shareyourproxy.api.service

import com.shareyourproxy.BuildConfig.GOOGLE_API_KEY
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
    fun getUser(@Header("userId") userId: String): Observable<User>

    /**
     * Save a [User].
     * @param userId unique id for [User] table
     * @param user   [User] data
     */
    @PUT(USER)
    fun updateUser(@Header("userId") userId: String, @Body user: User): Observable<User>

    /**
     * Update multiple [User] [Group]s.
     */
    @PUT(USER_GROUPS)
    fun updateUserGroups(@Header("userId") userId: String, @Header("groups") group: HashMap<String, Group>): Observable<Group>

    /**
     * Add a [User] [Group].
     */
    @PUT(USER_GROUPS)
    fun addUserGroup(@Header("userId") userId: String, @Header("group") group: Group): Observable<Group>

    /**
     * Delete a [User] [Group].
     */
    @DELETE(USER_GROUPS)
    fun deleteUserGroup(@Header("userId") userId: String, @Header("groupId") groupId: String): Observable<Group>

    /**
     * Add a [User] contact id.
     * @param userId unique id for [User] table
     */
    @PUT(USER_CONTACTS)
    fun addUserContact(@Header("userId") userId: String, @Header("contactId") contactId: String): Observable<String>

    /**
     * Delete a [User] contact.
     */
    @DELETE(USER_CONTACTS)
    fun deleteUserContact(@Header("userId") userId: String, @Header("contactId") contactId: String): Observable<String>

    /**
     * Add a [Channel].
     * @param userId unique id for [User] table
     */
    @PUT(USER_CHANNELS)
    fun addUserChannel(@Header("userId") userId: String, @Header("channel") channel: Channel): Observable<Channel>

    /**
     * Add multiple [Channel]s.
     * @param userId unique id for [User] table
     */
    @PUT(USER_CHANNELS)
    fun addUserChannels(@Header("userId") userId: String, @Header("channel") channel: HashMap<String, Channel>): Observable<HashMap<String, Channel>>

    /**
     * Get a [User]'s [Group]s.
     * @param userId unique id for [User] table
     */
    @GET(USER_CHANNELS)
    fun listGroupChannels(@Header("userId") userId: String, @Header("groupId") groupId: String): Observable<HashMap<String, Channel>>

    /**
     * Add a [Channel].
     * @param userId unique id for [User] table
     */
    @PUT(USER_CHANNELS)
    fun addGroupChannel(@Header("userId") userId: String, @Header("groupId") groupId: String, @Header("channels") channels: ArrayList<String>): Observable<AbstractMap.SimpleEntry<String, String>>

    /**
     * Delete a [User]s [Channel]s
     */
    @DELETE(USER_CHANNELS)
    fun deleteUserChannel(@Header("userId") userId: String, @Header("channelId") channelId: String): Observable<Channel>

    /**
     * Get a users shared link.
     * @return user observable
     */
    @GET(USER_SHARED)
    fun getSharedLink(@Header("userId") userId: String, @Header("groupId") groupId: String): Observable<SharedLink>

    /**
     * Get the total number of user followers.
     * @return user observable
     */
    @GET(USER_FOLLOWER_COUNT)
    fun userFollowerCount(@Header("userId") userId: String): Observable<Int>

    /**
     * Save a user's android version.
     * @param userId unique id for [User] table
     */
    @PUT(USER_VERSION)
    fun updateUserVersion(@Header("userId") userId: String, @Header("version") version: Int): Observable<String>

    /**
     * Add a [SharedLink]
     * @param sharedId shared link identifier
     */
    @PUT(SHARED)
    fun addSharedLink(@Header("sharedId") sharedId: String, @Header("link") link: SharedLink): Observable<SharedLink>

    /**
     * Delete Shared Link
     */
    @DELETE(SHARED)
    fun deleteSharedLink(@Header("sharedId") sharedId: String): Observable<SharedLink>

    /**
     * Get a user's messages.
     */
    @GET(MESSAGES)
    fun getUserMessages(@Header("userId") userId: String): Observable<ArrayList<Message>>

    /**
     * Delete a user's messages.
     */
    @GET(MESSAGES)
    fun downloadAndPurgeUserMessages(@Header("userId") userId: String): Observable<ArrayList<Message>>

    /**
     * Add a user message.
     */
    @PUT(MESSAGES)
    fun addUserMessage(@Header("userId") userId: String, @Header("message") message: Message): Observable<Message>

    /**
     * Delete a user's messages.
     */
    @DELETE(MESSAGES)
    fun deleteAllUserMessages(@Header("userId") userId: String): Observable<ArrayList<Message>>

    /**
     * Return a list of [User]s from an input set of UUIDs.
     * @return array list of users observable
     */
    @GET(USERS)
    fun listUsers(@Header("users") users: HashSet<String>): Observable<ArrayList<User>>

    /**
     * Search for users who's first, last and first + last name match the header string.
     * @return user observable
     */
    @GET(SEARCH)
    fun searchUsers(@Header("name") name: String): Observable<ArrayList<User>>

    /**
     * Get basic profile information from a google plus user.
     */
    @Headers("auth:"+ GOOGLE_API_KEY)
    @GET(GOOGLE_PERSON)
    fun getGooglePlusPerson(@Header("userId") userId: String): Observable<GooglePerson>
}
