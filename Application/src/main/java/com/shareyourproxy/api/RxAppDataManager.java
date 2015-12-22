package com.shareyourproxy.api;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.Constants;
import com.shareyourproxy.INotificationService;
import com.shareyourproxy.ProxyApplication;
import com.shareyourproxy.api.domain.factory.AutoValueClass;
import com.shareyourproxy.api.domain.factory.AutoValueTypeAdapterFactory;
import com.shareyourproxy.api.domain.model.Message;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.RxFabricAnalytics;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.RxMessageSync;
import com.shareyourproxy.api.rx.RxQuery;
import com.shareyourproxy.api.rx.command.AddUserMessageCommand;
import com.shareyourproxy.api.rx.command.BaseCommand;
import com.shareyourproxy.api.rx.command.SyncContactsCommand;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UsersDownloadedEventCallback;
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

import static com.shareyourproxy.api.CommandIntentService.ARG_COMMAND_CLASS;
import static com.shareyourproxy.api.CommandIntentService.ARG_RESULT_BASE_EVENT;

/**
 * Created by Evan on 12/6/15.
 */
public class RxAppDataManager {

    private final RxBusDriver _bus;
    private final ProxyApplication _app;
    private final SharedPreferences _prefs;
    private final RxFabricAnalytics rxFabricAnalytics = RxFabricAnalytics.INSTANCE;
    private final RxQuery rxQuery = RxQuery.INSTANCE;
    private final RxHelper _rxHelper = RxHelper.INSTANCE;
    private final RxMessageSync _rxMessageSync = RxMessageSync.INSTANCE;
    private NotificationManager _notificationManager;
    private INotificationService _notificationService;
    private Subscription _notificationSubscription;
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection _notificationConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            _notificationService = INotificationService.Stub.asInterface(service);
            _notificationSubscription = getNotificationsObservable()
                .subscribe(intervalObserver(_notificationService));
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            _notificationService = null;
            _notificationSubscription.unsubscribe();
        }
    };

    private RxAppDataManager(ProxyApplication app, SharedPreferences prefs, RxBusDriver bus) {
        _bus = bus;
        _app = app;
        _prefs = prefs;
        _bus.toIOThreadObservable().subscribe(getBusObserver());
        initializeNotificationService();
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
        EventCallback event = resultData.getParcelable(ARG_RESULT_BASE_EVENT);
        // update the logged in user from data saved to realm from the BaseCommand issued.
        User realmUser = rxQuery.queryUser(_app, _app.getCurrentUser().id());
        updateUser(realmUser);
        // issue event data to the main messaging system
        if (event != null) {
            _bus.post(event);
        }
        //issue secondary or causal events
        if (event instanceof UsersDownloadedEventCallback) {
            _bus.post(new SyncAllContactsSuccessEvent());
        }
        // log what happened successfully to fabric if we are not on debug
        if (!BuildConfig.DEBUG) {
            if (event != null) {
                rxFabricAnalytics.logAnalytics(Answers.getInstance(), realmUser, event);
            }
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

    private void initializeNotificationService() {
        _notificationManager = (NotificationManager) _app.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(_app, NotificationService.class);
        intent.setPackage("com.android.vending");
        _app.bindService(intent, _notificationConnection, Context.BIND_AUTO_CREATE);
    }

    public Observable<Long> getNotificationsObservable() {
        return Observable.interval(3, TimeUnit.MINUTES).compose(_rxHelper.<Long>observeIO());
    }

    public JustObserver<Long> intervalObserver(final INotificationService _notificationService) {
        return new JustObserver<Long>() {
            @Override
            public void next(Long timesCalled) {
                User currentUser = _app.getCurrentUser();
                Timber.i("Checking for notifications, attempt: %1$s", timesCalled.intValue());
                if (currentUser != null) {
                    try {
                        List<Notification> notifications =
                            _notificationService.getNotifications(currentUser.id());
                        if (notifications != null && notifications.size() > 0) {
                            for (Notification notification : notifications) {
                                _notificationManager.notify(notification.hashCode(), notification);
                            }
                            _rxMessageSync.deleteAllFirebaseMessages(currentUser).subscribe();
                        }
                    } catch (RemoteException e) {
                        Timber.e(Log.getStackTraceString(e));
                    }
                }
            }
        };
    }
}
