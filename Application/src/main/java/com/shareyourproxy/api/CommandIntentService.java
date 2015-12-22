package com.shareyourproxy.api;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.shareyourproxy.api.rx.command.BaseCommand;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import timber.log.Timber;

/**
 * Take in a {@link BaseCommand} and call its {@link BaseCommand#execute(Service)} method. Get a List of EventCallback data and return the result in the
 * ProxyApplication's EventCallback Subscription.
 */
public class CommandIntentService extends IntentService {

    public static final String TAG = CommandIntentService.class.getSimpleName();
    public static final String ARG_COMMAND_CLASS =
        "com.shareyourproxy.api.CommandIntentService.command_class";
    public static final String ARG_RESULT_RECEIVER =
        "com.shareyourproxy.api.CommandIntentService.api.result_receiver";
    public static final String ARG_RESULT_BASE_EVENT =
        "com.shareyourproxy.api.CommandIntentService.api.base_events";

    public CommandIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BaseCommand command = intent.getExtras().getParcelable(ARG_COMMAND_CLASS);
        ResultReceiver result = intent.getExtras().getParcelable(ARG_RESULT_RECEIVER);
        try {
            EventCallback event = command != null ? command.execute(this) : null;
            if (result != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(ARG_RESULT_BASE_EVENT, event);
                result.send(Activity.RESULT_OK, bundle);
            }
        } catch (Exception e) {
            logError(intent, result, e);
        }
    }

    private void logError(Intent intent, ResultReceiver result, Exception e) {
        Timber.e(Log.getStackTraceString(e));
        result.send(Activity.RESULT_CANCELED, intent.getExtras());
    }
}
