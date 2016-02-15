package com.shareyourproxy.api.rx


import android.content.Context
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.factory.UserFactory.addUserChannel
import com.shareyourproxy.api.domain.factory.UserFactory.deleteUserChannel
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback
import rx.Observable
import rx.functions.Func1

/**
 * Sync newChannel operations.
 */
internal object RxUserChannelSync {
    fun saveUserChannel(context: Context, oldUser: User, oldChannel: Channel, newChannel: Channel): UserChannelAddedEventCallback {
        return Observable.just(oldUser)
                .map(putUserChannel(newChannel))
                .map(addRealmUser(context))
                .map(saveChannelToFirebase(context, newChannel))
                .map(userChannelAddedEventCallback(oldChannel, newChannel))
                .toBlocking().single()
    }

    fun deleteChannel(context: Context, oldUser: User, channel: Channel, position: Int): EventCallback {
        return Observable.just(oldUser)
                .map(removeUserChannel(channel))
                .map(addRealmUser(context))
                .map(deleteChannelFromFirebase(context, channel))
                .map(userChannelDeletedEventCallback(channel, position))
                .toBlocking().single()
    }

    private fun addRealmUser(context: Context): Func1<User, User> {
        return Func1 { user ->
            RxHelper.updateRealmUser(context, user)
            user
        }
    }

    /**
     * Add the new channel to the users channel list and all groups.
     * @param newChannel
     * @return
     */
    private fun putUserChannel(newChannel: Channel): Func1<User, User> {
        return Func1 { oldUser ->
            val newUser = addUserChannel(oldUser, newChannel)
            newUser
        }
    }

    private fun saveChannelToFirebase(context: Context, channel: Channel): Func1<User, User> {
        return Func1 { user ->
            val userId = user.id
            RestClient(context).herokuUserService.addUserChannel(userId, channel).subscribe()
            RestClient(context).herokuUserService.updateUserGroups(userId, user.groups).subscribe()
            user
        }
    }

    private fun userChannelAddedEventCallback(oldChannel: Channel, newChannel: Channel): Func1<User, UserChannelAddedEventCallback> {
        return Func1 { user -> UserChannelAddedEventCallback(user, oldChannel, newChannel) }
    }

    private fun removeUserChannel(channel: Channel): Func1<User, User> {
        return Func1 { oldUser ->
            val newUser = deleteUserChannel(oldUser, channel)
            newUser
        }
    }

    private fun deleteChannelFromFirebase(context: Context, channel: Channel): Func1<User, User> {
        return Func1 { user ->
            val userId = user.id
            val channelId = channel.id
            RestClient(context).herokuUserService.deleteUserChannel(userId, channelId).subscribe()
            RestClient(context).herokuUserService.updateUserGroups(userId, user.groups).subscribe()
            user
        }
    }

    private fun userChannelDeletedEventCallback(
            channel: Channel, position: Int): Func1<User, EventCallback> {
        return Func1 { user -> UserChannelDeletedEventCallback(user, channel, position) }
    }
}

