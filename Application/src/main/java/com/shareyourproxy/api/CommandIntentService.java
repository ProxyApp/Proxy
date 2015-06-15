package com.shareyourproxy.api;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.shareyourproxy.api.rx.command.BaseCommand;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Evan on 6/7/15.
 */
public class CommandIntentService extends IntentService {

    public static final String TAG = CommandIntentService.class.getSimpleName();
    public static final String ARG_COMMAND_CLASS =
        "com.shareyourproxy.api.CommandIntentService.command_class";
    public static final String ARG_RESULT_RECEIVER =
        "com.shareyourproxy.api.CommandIntentService.api.result_receiver";
    public static final String ARG_RESULT_BASE_EVENTS =
        "com.shareyourproxy.api.CommandIntentService.api.result_receiver";

    public CommandIntentService() {
        super(TAG);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onHandleIntent(Intent intent) {
        BaseCommand command = intent.getExtras().getParcelable(ARG_COMMAND_CLASS);
        ResultReceiver result = intent.getExtras().getParcelable(ARG_RESULT_RECEIVER);
        try {
            Class<?> clazz = Class.forName(command.componentName.getClassName());
            Object obj = clazz.getConstructor(BaseCommand.class)
                .newInstance(command);
            Method method = clazz.getMethod("execute", IntentService.class);
            List<CommandEvent> events = (List<CommandEvent>) method.invoke(obj, this);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(ARG_RESULT_BASE_EVENTS, new ArrayList(events));
            result.send(Activity.RESULT_OK, bundle);
        } catch (Exception e) {
            logError(result, e);
        }
    }

    private void logError(ResultReceiver result, Exception e) {
        Timber.e(Log.getStackTraceString(e));
        result.send(Activity.RESULT_CANCELED, null);
    }
}
