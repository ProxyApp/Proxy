package com.shareyourproxy.api;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.crashlytics.android.answers.Answers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.Constants;
import com.shareyourproxy.ProxyApplication;
import com.shareyourproxy.api.domain.factory.AutoValueClass;
import com.shareyourproxy.api.domain.factory.AutoValueTypeAdapterFactory;
import com.shareyourproxy.api.domain.model.Message;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.RxFabricAnalytics;
import com.shareyourproxy.api.rx.RxQuery;
import com.shareyourproxy.api.rx.command.AddUserMessageCommand;
import com.shareyourproxy.api.rx.command.BaseCommand;
import com.shareyourproxy.api.rx.command.SyncContactsCommand;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UsersDownloadedEventCallback;
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent;

import java.util.UUID;

import timber.log.Timber;

import static com.shareyourproxy.api.CommandIntentService.ARG_COMMAND_CLASS;

/**
 * Created by Evan on 12/6/15.
 */
public class RxAppDataManager {

    private final RxBusDriver _bus;
    private final ProxyApplication _app;
    private final SharedPreferences _prefs;
    private final RxFabricAnalytics rxFabricAnalytics = RxFabricAnalytics.INSTANCE;
    private final RxQuery rxQuery = RxQuery.INSTANCE;

    private RxAppDataManager(ProxyApplication app, SharedPreferences prefs, RxBusDriver bus) {
        _bus = bus;
        _app = app;
        _prefs = prefs;
        _bus.toIOThreadObservable().subscribe(getBusObserver());
    }

    public static RxAppDataManager newInstance(ProxyApplication app, SharedPreferences prefs, RxBusDriver bus) {
        return new RxAppDataManager(app, prefs, bus);
    }

    private JustObserver<Object> getBusObserver() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof BaseCommand) {
                    baseCommandEvent((BaseCommand) event);
                } else if (event instanceof UserContactAddedEventCallback) {
                    userContactAddedEvent((UserContactAddedEventCallback) event);
                }
            }
        };
    }

    private void userContactAddedEvent(UserContactAddedEventCallback event) {
        Message message = Message.create(UUID.randomUUID().toString(), event.user.id(), event.user.fullName());
        baseCommandEvent(new AddUserMessageCommand(event.contactId, message));
    }

    private void baseCommandEvent(BaseCommand event) {
        Timber.i("BaseCommand: %1$s", event);
        Intent intent = new Intent(_app, CommandIntentService.class);
        intent.putExtra(ARG_COMMAND_CLASS, event);
        intent.putExtra(CommandIntentService.ARG_RESULT_RECEIVER, getResultReceiver());
        _app.startService(intent);
    }

    private ResultReceiver getResultReceiver() {
        return new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, final Bundle resultData) {
                if (resultCode == Activity.RESULT_OK) {
                    commandSuccessful(resultData);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    sendError(resultData);
                } else {
                    sendError(resultData);
                }
            }
        };
    }

    private void commandSuccessful(Bundle resultData) {
        EventCallback event =
            resultData.getParcelable(CommandIntentService.ARG_RESULT_BASE_EVENT);
        // update the logged in user from data saved to realm from the BaseCommand issued.
        User realmUser = rxQuery.queryUser(_app, _app.getCurrentUser().id());
        updateUser(realmUser);
        // issue event data to the main messaging system
        _bus.post(event);
        //issue secondary or causal events
        if (event instanceof UsersDownloadedEventCallback) {
            _bus.post(new SyncAllContactsSuccessEvent());
        }
        // log what happened successfully to fabric if we are not on debug
        if (!BuildConfig.DEBUG) {
            rxFabricAnalytics.logAnalytics(Answers.getInstance(), realmUser, event);
        }
    }

    private void updateUser(User user) {
        _app.setCurrentUser(user);
        Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new AutoValueTypeAdapterFactory())
            .create();
        String userJson = gson.toJson(user, User.class.getAnnotation(AutoValueClass.class).autoValueClass());
        _prefs.edit()
            .putString(Constants.KEY_LOGGED_IN_USER, userJson)
            .apply();
    }

    private void sendError(Bundle resultData) {
        Timber.e("Error receiving result");
        BaseCommand command = resultData.getParcelable(ARG_COMMAND_CLASS);
        if (command instanceof SyncContactsCommand) {
            _bus.post(new SyncAllContactsErrorEvent());
        }
    }
}
