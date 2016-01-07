package com.shareyourproxy.api.rx

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory.decodeResource
import android.graphics.Color
import android.support.v4.app.NotificationCompat.Builder
import android.support.v4.app.TaskStackBuilder
import com.shareyourproxy.Intents.getUserProfileIntent
import com.shareyourproxy.R
import com.shareyourproxy.R.mipmap.ic_proxy
import com.shareyourproxy.R.string.added_you
import com.shareyourproxy.R.string.app_name
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.model.Message
import com.shareyourproxy.api.rx.RxHelper.observeMain
import com.shareyourproxy.api.rx.RxQuery.getRealmUser
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserMessageAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserMessagesDownloadedEventCallback
import com.shareyourproxy.app.UserContactActivity
import rx.Subscription
import rx.functions.Func1
import java.util.*

/**
 * Cold Rx.Observable calls to handle syncing messages for Users.
 */
internal object RxMessageSync {
    private val userMessageCallback: Func1<Message, EventCallback> get() = Func1 {UserMessageAddedEventCallback(it) }

    fun getFirebaseMessages(context: Context, userId: String): UserMessagesDownloadedEventCallback {
        return RestClient(context).herokuUserService.downloadAndPurgeUserMessages(userId).map({ mapNewMessages(context, it, userId) })
                .compose(observeMain<UserMessagesDownloadedEventCallback>())
                .toBlocking().single()
    }

    private fun mapNewMessages(context: Context, messages: ArrayList<Message>, userId: String): UserMessagesDownloadedEventCallback {
        val notifications = ArrayList<Notification>()
        for (message in messages) {
            val fullName = message.fullName
            val intent = getPendingUserProfileIntent(context, userId, message)
            val builder = Builder(context)
                    .setLargeIcon(getProxyIcon(context))
                    .setSmallIcon(R.mipmap.ic_proxy_notification)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(1000, 1000))
                    .setLights(Color.MAGENTA, 1000, 1000)
                    .setContentTitle(context.getString(app_name))
                    .setContentText(context.getString(added_you, fullName))
                    .setContentIntent(intent)

            notifications.add(builder.build())
        }
        return UserMessagesDownloadedEventCallback(notifications)
    }

    fun saveFirebaseMessage(context: Context, userId: String, message: Message): EventCallback {
        return RestClient(context).herokuUserService
                .addUserMessage(userId, message)
                .map(userMessageCallback)
                .compose(observeMain<EventCallback>())
                .toBlocking().single()
    }

    fun deleteAllFirebaseMessages(context: Context, userId: String): Subscription {
        return RestClient(context).herokuUserService.deleteAllUserMessages(userId).compose(observeMain<ArrayList<Message>>()).subscribe()
    }

    private fun getProxyIcon(context: Context): Bitmap {
        return decodeResource(context.resources, ic_proxy)
    }

    private fun getPendingUserProfileIntent(context: Context, loggedInUserId: String, message: Message): PendingIntent {
        // Creates an explicit intent for an Activity in your app
        val contact = getRealmUser(context, message.contactId)
        val resultIntent = getUserProfileIntent(contact, loggedInUserId)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(UserContactActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        return stackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT)
    }
}

