package com.shareyourproxy.api

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import com.shareyourproxy.api.rx.command.BaseCommand
import timber.log.Timber

/**
 * Take in a [BaseCommand] and call its [BaseCommand.execute] method. Get a List of EventCallback data and return the result in the
 * ProxyApplication's EventCallback Subscription.
 */
internal final class CommandIntentService : IntentService(CommandIntentService.TAG) {

    override fun onHandleIntent(intent: Intent) {
        val command = intent.extras.getParcelable<BaseCommand>(ARG_COMMAND_CLASS)
        val result = intent.extras.getParcelable<ResultReceiver>(ARG_RESULT_RECEIVER)
        try {
            val event = command?.execute(this)
            if (result != null) {
                val bundle = Bundle()
                bundle.putParcelable(ARG_RESULT_BASE_EVENT, event)
                result.send(Activity.RESULT_OK, bundle)
            }
        } catch (e: Exception) {
            logError(intent, result, e)
        }
    }

    private fun logError(intent: Intent, result: ResultReceiver, e: Exception) {
        Timber.e(Log.getStackTraceString(e))
        result.send(Activity.RESULT_CANCELED, intent.extras)
    }

    companion object {
        val TAG = CommandIntentService::class.java.simpleName
        val ARG_COMMAND_CLASS = "com.shareyourproxy.api.CommandIntentService.command_class"
        val ARG_RESULT_RECEIVER = "com.shareyourproxy.api.CommandIntentService.api.result_receiver"
        val ARG_RESULT_BASE_EVENT = "com.shareyourproxy.api.CommandIntentService.api.base_events"
    }
}
