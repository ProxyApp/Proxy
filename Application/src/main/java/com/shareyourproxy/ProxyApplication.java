package com.shareyourproxy;

import android.app.Activity;
import android.app.Application;
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
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareyourproxy.api.CommandIntentService;
import com.shareyourproxy.api.NotificationService;
import com.shareyourproxy.api.domain.model.Message;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.gson.AutoValueAdapterFactory;
import com.shareyourproxy.api.gson.AutoValueClass;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.command.AddUserMessageCommand;
import com.shareyourproxy.api.rx.command.BaseCommand;
import com.shareyourproxy.api.rx.command.SyncAllUsersCommand;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback;
import com.shareyourproxy.api.rx.event.SyncAllUsersErrorEvent;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

import static com.shareyourproxy.Constants.MASTER_KEY;
import static com.shareyourproxy.api.CommandIntentService.ARG_COMMAND_CLASS;
import static com.shareyourproxy.api.rx.RxMessageSync.deleteAllFirebaseMessages;
import static com.shareyourproxy.api.rx.RxQuery.queryUser;
import static com.shareyourproxy.api.rx.SaveFabricAnalytics.logAnalytics;

/**
 * Proxy application that handles syncing the current user and handling BaseCommands.
 */
public class ProxyApplication extends Application {

    private final RxBusDriver _bus = RxBusDriver.getInstance();
    private User _currentUser;
    private SharedPreferences _sharedPreferences;
    private INotificationService _notificationService;
    private Subscription _notificationSubscription;
    private NotificationManager _notificationManager;
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
            _notificationSubscription =
                getNotificationsObservable().subscribe(intervalObserver(_notificationService));
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            _notificationService = null;
            _notificationSubscription.unsubscribe();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //this complains its not initialized before everything else....wtf?
        Firebase.setAndroidContext(this);
        initialize();
    }

    public void initialize() {
        FacebookSdk.sdkInitialize(this);
        MultiDex.install(this);

        _bus.toObservable().subscribe(getRequest(this));
        _sharedPreferences = getSharedPreferences(MASTER_KEY, Context.MODE_PRIVATE);

        initializeBuildConfig();

        initializeRealm();
        initializeNotificationService();
    }

    public void initializeRealm() {
        RealmConfiguration config = new RealmConfiguration.Builder(this)
            .deleteRealmIfMigrationNeeded()
            .schemaVersion(BuildConfig.VERSION_CODE)
            .build();
        Realm.setDefaultConfiguration(config);
    }

    public void initializeBuildConfig() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        if (BuildConfig.USE_GOOGLE_ANALYTICS) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.newTracker(BuildConfig.GA_TRACKER_ID);
            analytics.enableAdvertisingIdCollection(true);
            analytics.enableAutoActivityReports(this);
        }
        //Twitter and Crashlytics
        TwitterAuthConfig authConfig =
            new TwitterAuthConfig(BuildConfig.TWITTER_KEY, BuildConfig.TWITTER_SECRET);
        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Twitter(authConfig), new Crashlytics(), new Answers());
        } else {
            Fabric.with(this, new Twitter(authConfig), new Answers());
        }
    }

    public void initializeNotificationService() {
        _notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, NotificationService.class);
        intent.setPackage("com.android.vending");
        bindService(intent, _notificationConnection, Context.BIND_AUTO_CREATE);
    }

    public Observable<Long> getNotificationsObservable() {
        return Observable.interval(3, TimeUnit.MINUTES).compose(RxHelper.<Long>applySchedulers());
    }

    public JustObserver<Long> intervalObserver(final INotificationService _notificationService) {
        return new JustObserver<Long>() {
            @Override
            public void success(Long timesCalled) {
                Timber.i("Checking for notifications, attempt:" + timesCalled.intValue());
                if (_currentUser != null) {
                    try {
                        List<Notification> notifications =
                            _notificationService.getNotifications(getRxBus(), _currentUser.id());
                        if (notifications != null && notifications.size() > 0) {
                            for (Notification notification : notifications) {
                                _notificationManager.notify(notification.hashCode(), notification);
                            }
                            deleteAllFirebaseMessages(ProxyApplication.this, getRxBus(),
                                _currentUser).subscribe();
                        }
                    } catch (RemoteException e) {
                        Timber.e(Log.getStackTraceString(e));
                    }
                }
            }

            @Override
            public void error(Throwable e) {
                Timber.e("Notification Observable Error");
            }
        };
    }

    public Action1<Object> getRequest(final Context context) {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof BaseCommand) {
                    baseCommandEvent((BaseCommand) event);
                } else if (event instanceof UserContactAddedEventCallback) {
                    userContactAddedEvent((UserContactAddedEventCallback) event);
                }
            }
        };
    }

    private void userContactAddedEvent(UserContactAddedEventCallback event) {
        baseCommandEvent(new AddUserMessageCommand(getRxBus(),
            event.contactId, Message.create(UUID.randomUUID().toString(), event.user.id(),
            event.user.first(), event.user.last())));
    }

    private void baseCommandEvent(BaseCommand event) {
        Timber.i("BaseCommand:" + event);
        Intent intent = new Intent(ProxyApplication.this, CommandIntentService.class);
        intent.putExtra(ARG_COMMAND_CLASS, event);
        intent.putExtra(
            CommandIntentService.ARG_RESULT_RECEIVER, new ResultReceiver(null) {
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData) {
                    if (resultCode == Activity.RESULT_OK) {
                        commandSuccessful(resultData);
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        sendError(resultData);
                    } else {
                        Timber.e("Error receiving result");
                        sendError(resultData);
                    }
                }
            });
        startService(intent);
    }

    private void commandSuccessful(Bundle resultData) {
        ArrayList<EventCallback> events =
            resultData.getParcelableArrayList(
                CommandIntentService.ARG_RESULT_BASE_EVENTS);
        // update the logged in user from data saved to realm from the BaseCommand issued.
        User realmUser = queryUser(
            ProxyApplication.this, _currentUser.id());
        updateUser(realmUser);
        // issue event data to the main messaging system
        for (EventCallback event : events) {
            getRxBus().post(event);
        }
        // log what happened successfully to fabric if we arent on debug
        if (!BuildConfig.DEBUG) {
            logAnalytics(Answers.getInstance(), realmUser, events);
        }
    }

    private void sendError(Bundle resultData) {
        BaseCommand command = resultData.getParcelable(ARG_COMMAND_CLASS);
        if (command instanceof SyncAllUsersCommand) {
            getRxBus().post(new SyncAllUsersErrorEvent());
        }
    }

    private void updateUser(User user) {
        setCurrentUser(user);
        Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new AutoValueAdapterFactory())
            .create();
        String userJson = gson.toJson(
            user, User.class.getAnnotation(AutoValueClass.class).autoValueClass());
        _sharedPreferences
            .edit()
            .putString(Constants.KEY_LOGGED_IN_USER, userJson)
            .apply();
    }

    /**
     * Getter.
     *
     * @return currerntly logged in user
     */
    public User getCurrentUser() {
        return _currentUser;
    }

    /**
     * Setter.
     *
     * @param currentUser currently logged in user
     */
    public void setCurrentUser(User currentUser) {
        synchronized (Application.class) {
            _currentUser = currentUser;
        }
    }

    public RxBusDriver getRxBus() {
        return _bus;
    }

    public SharedPreferences getSharedPreferences() {
        return _sharedPreferences;
    }

}
