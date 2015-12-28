package com.shareyourproxy.api.rx

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.shareyourproxy.Intents.getUserProfileIntent
import com.shareyourproxy.R
import com.shareyourproxy.R.string.added_you
import com.shareyourproxy.R.string.app_name
import com.shareyourproxy.api.RestClient.herokuUserService
import com.shareyourproxy.api.domain.model.Message
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxQuery.getRealmUser
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserMessageAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserMessagesDownloadedEventCallback
import com.shareyourproxy.app.UserContactActivity
import rx.Observable
import rx.functions.Func1
import java.util.*

/**
 * Cold Rx.Observable calls to handle syncing messages for Users.
 */
object RxMessageSync {
    fun getFirebaseMessages(context: Context, userId: String): EventCallback {
        return herokuUserService.getUserMessages(userId).map { messages ->
            val notifications = ArrayList<Notification>()
            if (messages == null) {
                UserMessagesDownloadedEventCallback(notifications)
            } else {
                for (message in messages.entries) {
                    val fullName = message.value.fullName
                    val intent = getPendingUserProfileIntent(context, userId, message.value)
                    val _builder = NotificationCompat.Builder(context)
                            .setLargeIcon(getProxyIcon(context))
                            .setSmallIcon(R.mipmap.ic_proxy_notification)
                            .setAutoCancel(true)
                            .setVibrate(longArrayOf(1000, 1000))
                            .setLights(Color.MAGENTA, 1000, 1000)
                            .setContentTitle(context.getString(app_name))
                            .setContentText(context.getString(added_you, fullName))
                            .setContentIntent(intent)

                    notifications.add(_builder.build())
                }
                UserMessagesDownloadedEventCallback(notifications)
            }
        }.compose(RxHelper.observeMain<EventCallback>()).toBlocking().single()
    }

    fun saveFirebaseMessage(userId: String, message: Message): EventCallback {
        val messages = HashMap<String, Message>()
        messages.put(message.id, message)
        return herokuUserService.addUserMessage(userId, messages).map(userMessageCallback).compose(RxHelper.observeMain<EventCallback>()).toBlocking().single()
    }

    fun deleteAllFirebaseMessages(user: User): Observable<Message> {
        val contactId = user.id
        return herokuUserService.deleteAllUserMessages(contactId).compose(RxHelper.observeMain<Message>())
    }

    private fun getProxyIcon(context: Context): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.mipmap.ic_proxy)
    }

    private fun getPendingUserProfileIntent(context: Context, loggedInUserId: String, message: Message): PendingIntent {
        // Creates an explicit intent for an Activity in your app
        val contact = getRealmUser(context, message.contactId)
        val resultIntent = getUserProfileIntent(contact, loggedInUserId)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(UserContactActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val userMessageCallback: Func1<HashMap<String, Message>, EventCallback> get() = Func1 { message -> UserMessageAddedEventCallback(message) }
}

