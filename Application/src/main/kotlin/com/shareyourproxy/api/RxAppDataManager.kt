package com.shareyourproxy.api

import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.ResultReceiver
import com.crashlytics.android.answers.Answers
import com.google.gson.GsonBuilder
import com.shareyourproxy.BuildConfig
import com.shareyourproxy.Constants
import com.shareyourproxy.ProxyApplication
import com.shareyourproxy.api.CommandIntentService.Companion.ARG_COMMAND_CLASS
import com.shareyourproxy.api.CommandIntentService.Companion.ARG_RESULT_BASE_EVENT
import com.shareyourproxy.api.CommandIntentService.Companion.ARG_RESULT_RECEIVER
import com.shareyourproxy.api.domain.model.Message
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.RxFabricAnalytics.logFabricAnalytics
import com.shareyourproxy.api.rx.RxHelper.observeIO
import com.shareyourproxy.api.rx.RxQuery.queryUser
import com.shareyourproxy.api.rx.command.AddUserMessageCommand
import com.shareyourproxy.api.rx.command.BaseCommand
import com.shareyourproxy.api.rx.command.GetUserMessagesCommand
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserMessagesDownloadedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UsersDownloadedEventCallback
import com.shareyourproxy.api.rx.event.SyncContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncContactsSuccessEvent
import rx.Observable
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Manage data at an application context level.
 */
internal final class RxAppDataManager(private val app: ProxyApplication, private val prefs: SharedPreferences) {
    private val notificationManager: NotificationManager = app.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private val gson = GsonBuilder().create()
    private val pollingObservable: Observable<Long> = Observable.interval(3, TimeUnit.MINUTES).compose(observeIO<Long>())
    private val busObserver: JustObserver<Any> = object : JustObserver<Any>(RxAppDataManager::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            when (event) {
                is BaseCommand -> baseCommandEvent(event)
                is UserContactAddedEventCallback -> userContactAddedEvent(event)
                else -> {
                    Timber.e("RxDataManager command listener event error")
                }
            }
        }
    }
    private val intervalObserver = object : JustObserver<Long>(RxAppDataManager::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(timesCalled: Long) {
            Timber.i("Checking for notifications, attempt: ${timesCalled.toInt()}")
            val eventData: UserMessagesDownloadedEventCallback = GetUserMessagesCommand(app.currentUser.id).execute(app)
            val notifications = eventData.notifications
            notifications.forEach { notificationManager.notify(it.hashCode(), it) }
        }
    }
    private val resultReceiver: ResultReceiver = object : ResultReceiver(null) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            when (resultCode) {
                RESULT_OK -> commandSuccessful(resultData)
                else -> sendError(resultData)
            }
        }
    }

    init {
        rxBusObservable().subscribe(busObserver)
        pollingObservable.subscribe(intervalObserver)
    }

    private fun userContactAddedEvent(event: UserContactAddedEventCallback) {
        val message = Message(UUID.randomUUID().toString(), event.user.id, event.user.fullName)
        baseCommandEvent(AddUserMessageCommand(event.contactId, message))
    }

    private fun baseCommandEvent(event: BaseCommand) {
        Timber.i("BaseCommand: ${event.toString()}")
        val intent = Intent(app, CommandIntentService::class.java)
        intent.putExtra(ARG_COMMAND_CLASS, event)
        intent.putExtra(ARG_RESULT_RECEIVER, resultReceiver)
        app.startService(intent)
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
            post(SyncContactsSuccessEvent())
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
            post(SyncContactsErrorEvent())
        }
    }
}
