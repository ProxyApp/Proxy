package com.shareyourproxy.api.rx

import android.content.Context
import android.support.v7.util.SortedList
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.RestClient.getUserChannelService
import com.shareyourproxy.api.RestClient.getUserGroupService
import com.shareyourproxy.api.domain.factory.ChannelFactory.createPublicChannel
import com.shareyourproxy.api.domain.factory.GroupFactory
import com.shareyourproxy.api.domain.factory.UserFactory
import com.shareyourproxy.api.domain.factory.UserFactory.addUserGroups
import com.shareyourproxy.api.domain.factory.UserFactory.addUserPublicChannels
import com.shareyourproxy.api.domain.model.*
import com.shareyourproxy.api.rx.RxHelper.updateRealmUser
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.PublicChannelsUpdatedEvent
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupAddedEventCallback
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType
import rx.Observable
import rx.functions.Func1
import rx.functions.Func2
import java.util.*

/**
 * Update a groups channel map, or update a channel in all groups.
 */
object RxGroupChannelSync {
    fun addUserGroupsChannel(context: Context, user: User, groups: ArrayList<GroupToggle>, channel: Channel): UserGroupAddedEventCallback {
        return Observable.create(addChannelToSelectedGroups(groups, channel))
                .map(zipAndSaveGroups(context, user))
                .toBlocking().single()
    }

    fun addChannelToSelectedGroups(
            groups: ArrayList<GroupToggle>, channel: Channel): Observable.OnSubscribe<HashMap<String, Group>> {
        return Observable.OnSubscribe<HashMap<String, Group>> { subscriber ->
            try {
                val channelId = channel.id()
                val newGroups = HashMap<String, Group>(groups.size)
                for (entryGroup in groups) {
                    if (entryGroup.isChecked) {
                        entryGroup.group.channels()!!.add(channelId)
                    }
                    newGroups.put(entryGroup.group.id(), entryGroup.group)
                }
                subscriber.onNext(newGroups)
                subscriber.onCompleted()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    fun updateGroupChannels(
            context: Context, user: User, newTitle: String, oldGroup: Group, channels: HashSet<String>, groupEditType: GroupEditType):
            EventCallback {
        return Observable.zip(
                saveRealmGroupChannels(context, user, newTitle, oldGroup, channels),
                saveFirebaseGroupChannels(context, user.id(), newTitle, oldGroup, channels),
                zipAddGroupChannels(user, channels, oldGroup, groupEditType)).map(saveSharedLink(context)).toBlocking().single()
    }

    fun updatePublicGroupChannels(context: Context, user: User, channels: ArrayList<ChannelToggle>): EventCallback {
        val newChannels = HashMap<String, Channel>(channels.size)
        for (i in channels.indices) {
            val channelToggle = channels[i]
            val channel = channelToggle.channel
            val isPublic = channelToggle.inGroup()

            val newChannel = createPublicChannel(channel, isPublic)
            newChannels.put(newChannel.id(), newChannel)
        }
        return Observable.zip(
                saveRealmPublicGroupChannels(context, user, newChannels),
                saveFirebasePublicChannels(context, user.id(), newChannels),
                zipAddPublicChannels()).toBlocking().single()
    }

    fun getSelectedChannels(channels: SortedList<ChannelToggle>): HashSet<String> {
        return Observable.just(channels).map(selectedChannels).toBlocking().single()
    }

    private fun zipAndSaveGroups(context: Context, user: User): Func1<HashMap<String, Group>, UserGroupAddedEventCallback> {
        return Func1 { newGroups ->
            Observable.zip(
                    saveRealmGroupChannels(context, user, newGroups),
                    saveFirebaseUserGroups(context, user.id(), newGroups),
                    zipAddGroupsChannel()).toBlocking().single()
        }
    }

    private fun zipAddGroupsChannel(): Func2<User, Group, UserGroupAddedEventCallback> {
        return Func2 { user, group -> UserGroupAddedEventCallback(user, group) }
    }

    private fun saveSharedLink(context: Context): Func1<GroupChannelsUpdatedEventCallback, EventCallback> {
        return Func1 { event ->
            val link = SharedLink.create(event.user, event.group)
            RestClient.getSharedLinkService(context).addSharedLink(link.id(), link).subscribe()
            event
        }
    }

    private fun saveRealmGroupChannels(context: Context, user: User, groups: HashMap<String, Group>): Observable<User> {
        return Observable.create { subscriber ->
            try {
                val newUser = addUserGroups(user, groups)
                updateRealmUser(context, newUser)
                subscriber.onNext(newUser)
                subscriber.onCompleted()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    private fun saveRealmPublicGroupChannels(context: Context, user: User, channels: HashMap<String, Channel>): Observable<User> {
        return Observable.create { subscriber ->
            try {
                val newUser = addUserPublicChannels(user, channels)
                updateRealmUser(context, newUser)
                subscriber.onNext(newUser)
                subscriber.onCompleted()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    private fun saveFirebaseUserGroups(context: Context, userId: String, groups: HashMap<String, Group>): Observable<Group> {
        return RestClient.getUserGroupService(context).updateUserGroups(userId, groups)
    }

    private fun zipAddPublicChannels(): Func2<User, HashMap<String, Channel>, EventCallback> {
        return Func2 { user, newChannels -> PublicChannelsUpdatedEvent(user, newChannels) }
    }

    private fun saveFirebasePublicChannels(
            context: Context, userId: String, newChannels: HashMap<String, Channel>): Observable<HashMap<String, Channel>> {
        return getUserChannelService(context).addUserChannels(userId, newChannels)
    }


    private val selectedChannels: Func1<SortedList<ChannelToggle>, HashSet<String>>
        get() = Func1 { groupEditChannels ->
            val selectedChannels = HashSet<String>()
            for (i in 0..groupEditChannels.size() - 1) {
                val editChannel = groupEditChannels.get(i)
                if (editChannel.inGroup()) {
                    val channel = editChannel.channel
                    selectedChannels.add(channel.id())
                }
            }
            selectedChannels
        }

    private fun saveRealmGroupChannels(context: Context, user: User, newTitle: String, oldGroup: Group, channels: HashSet<String>):
            Observable<Group> {
        return Observable.just(channels).map(addRealmGroupChannels(context, user, newTitle, oldGroup))
    }

    private fun addRealmGroupChannels(context: Context, user: User, newTitle: String, oldGroup: Group): Func1<HashSet<String>, Group> {
        return Func1 { channels ->
            val newGroup = GroupFactory.addGroupChannels(newTitle, oldGroup, channels)
            val newUser = UserFactory.addUserGroup(user, newGroup)
            updateRealmUser(context, newUser)
            newGroup
        }
    }

    private fun saveFirebaseGroupChannels(context: Context, userId: String, newTitle: String, group: Group, channels: HashSet<String>):
            Observable<Group> {
        val groupId = group.id()
        val newGroup = Group.copy(group, newTitle, channels)
        return getUserGroupService(context).addUserGroup(userId, groupId, newGroup)
    }

    private fun zipAddGroupChannels(user: User, channels: HashSet<String>, oldGroup: Group, groupEditType: GroupEditType):
            Func2<Group, Group, GroupChannelsUpdatedEventCallback> {
        return Func2 { group, group2 -> GroupChannelsUpdatedEventCallback(user, oldGroup, group, channels, groupEditType) }
    }
}
