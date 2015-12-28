package com.shareyourproxy.api

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import com.shareyourproxy.INotificationService
import com.shareyourproxy.api.rx.command.GetUserMessagesCommand
import com.shareyourproxy.api.rx.command.eventcallback.UserMessagesDownloadedEventCallback
import java.util.*

/**
 * Service to pull for notification messages.
 */
class NotificationService : Service() {
    private val _binder = object : INotificationService.Stub() {
        @Throws(RemoteException::class)
        override fun getNotifications(userId: String): ArrayList<Notification>? {
            val eventData = GetUserMessagesCommand(userId).execute(this@NotificationService)
                val notifications = (eventData as UserMessagesDownloadedEventCallback).notifications
                    return notifications
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder? {
        return _binder
    }
}
