package com.shareyourproxy.api

import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.*
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.os.ResultReceiver
import android.util.Log
import com.crashlytics.android.answers.Answers
import com.google.gson.GsonBuilder
import com.shareyourproxy.BuildConfig
import com.shareyourproxy.Constants
import com.shareyourproxy.INotificationService
import com.shareyourproxy.ProxyApplication
import com.shareyourproxy.api.CommandIntentService.Companion.ARG_COMMAND_CLASS
import com.shareyourproxy.api.CommandIntentService.Companion.ARG_RESULT_BASE_EVENT
import com.shareyourproxy.api.domain.model.Message
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxBusRelay.toIOThreadObservable
import com.shareyourproxy.api.rx.RxFabricAnalytics.logFabricAnalytics
import com.shareyourproxy.api.rx.RxHelper.observeIO
import com.shareyourproxy.api.rx.RxMessageSync.deleteAllFirebaseMessages
import com.shareyourproxy.api.rx.RxQuery.queryUser
import com.shareyourproxy.api.rx.command.AddUserMessageCommand
import com.shareyourproxy.api.rx.command.BaseCommand
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UsersDownloadedEventCallback
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import rx.Observable
import rx.Subscription
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Manage data at an application context level.
 */
internal final class RxAppDataManager(private val app: ProxyApplication, private val prefs: SharedPreferences) {
    private val notificationManager: NotificationManager= app.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private var notificationSubscription: Subscription? = null
    private val gson = GsonBuilder().create()
    private val notificationsObservable: Observable<Long>get() = Observable.interval(3, TimeUnit.MINUTES).compose(observeIO<Long>())
    private val notificationConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            notificationSubscription = notificationsObservable.subscribe(
                    intervalObserver(INotificationService.Stub.asInterface(service)))
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            notificationSubscription?.unsubscribe()
        }
    }

    init {
        toIOThreadObservable().subscribe(busObserver)
        initializeNotificationService()
    }

    private val busObserver: JustObserver<Any>get() = object : JustObserver<Any>() {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            when (event) {
                event is BaseCommand -> baseCommandEvent(event as BaseCommand)
                event is UserContactAddedEventCallback -> userContactAddedEvent(event as UserContactAddedEventCallback)
            }
        }
    }

    private fun userContactAddedEvent(event: UserContactAddedEventCallback) {
        val message = Message(UUID.randomUUID().toString(), event.user.id, event.user.fullName)
        baseCommandEvent(AddUserMessageCommand(event.contactId, message))
    }

    private fun baseCommandEvent(event: BaseCommand) {
        Timber.i("BaseCommand: ${event.toString()}")
        val intent = Intent(app, CommandIntentService::class.java)
        intent.putExtra(ARG_COMMAND_CLASS, event)
        intent.putExtra(CommandIntentService.ARG_RESULT_RECEIVER, resultReceiver)
        app.startService(intent)
    }

    private val resultReceiver: ResultReceiver
        get() = object : ResultReceiver(null) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                when(resultCode){
                    RESULT_OK -> commandSuccessful(resultData)
                    else ->sendError(resultData)
                }
            }
        }

    private fun commandSuccessful(resultData: Bundle) {
        val event = resultData.getParcelable<EventCallback>(ARG_RESULT_BASE_EVENT)
        // update the logged in user from data saved to realm from the BaseCommand issued.
        val realmUser = queryUser(app, app.currentUser.id)
        updateUser(realmUser)
        // issue event data to the main messaging system
        if (event != null) {
            post(event)
        }
        //issue secondary or causal events
        if (event is UsersDownloadedEventCallback) {
            post(SyncAllContactsSuccessEvent())
        }
        // log what happened successfully to fabric if we are not on debug
        if (!BuildConfig.DEBUG) {
            if (event != null) {
                logFabricAnalytics(Answers.getInstance(), realmUser, event)
            }
        }
    }

    private fun updateUser(user: User) {
        app.currentUser = user
        val userJson = gson.toJson(user, User::class.java)
        prefs.edit().putString(Constants.KEY_LOGGED_IN_USER, userJson).apply()
    }

    private fun sendError(resultData: Bundle) {
        Timber.e("Error receiving result")
        val command = resultData.getParcelable<BaseCommand>(ARG_COMMAND_CLASS)
        if (command is SyncContactsCommand) {
            post(SyncAllContactsErrorEvent())
        }
    }

    private fun initializeNotificationService() {
        val intent = Intent(app, NotificationService::class.java)
        intent.setPackage("com.android.vending")
        app.bindService(intent, notificationConnection, Context.BIND_AUTO_CREATE)
    }

    fun intervalObserver(notificationService: INotificationService): JustObserver<Long> {
        return object : JustObserver<Long>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(timesCalled: Long) {
                val currentUser = app.currentUser
                Timber.i("Checking for notifications, attempt: ${timesCalled.toInt()}")
                try {
                    val notifications = notificationService.getNotifications(currentUser.id)
                    if (notifications != null && notifications.size > 0) {
                        for (notification in notifications) {
                            notificationManager.notify(notification.hashCode(), notification)
                        }
                        deleteAllFirebaseMessages(app, currentUser).subscribe()
                    }
                } catch (e: RemoteException) {
                    Timber.e(Log.getStackTraceString(e))
                }
            }
        }
    }
}
