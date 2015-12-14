package com.shareyourproxy;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.firebase.client.Firebase;
import com.shareyourproxy.api.NotificationService;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.RxAppDataManager;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.RxGoogleAnalytics;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.RxMessageSync;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

import static com.shareyourproxy.Constants.MASTER_KEY;

/**
 * Proxy application that handles syncing the current user and handling BaseCommands.
 */
public class ProxyApplication extends Application {

    private static RefWatcher _refWatcher;
    private final RxBusDriver _bus = RxBusDriver.INSTANCE;
    private final RxHelper _rxHelper = RxHelper.INSTANCE;
    private final RxMessageSync _rxMessageSync = RxMessageSync.INSTANCE;
    private User _currentUser;
    private SharedPreferences _sharedPreferences;
    private INotificationService _notificationService;
    private Subscription _notificationSubscription;
    private NotificationManager _notificationManager;
    private RxAppDataManager _rxDataManager;
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
                .subscribe(intervalObserver(ProxyApplication.this, _notificationService));
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            _notificationService = null;
            _notificationSubscription.unsubscribe();
        }
    };

    public static void watchForLeak(Object object) {
        if (_refWatcher != null)
            _refWatcher.watch(object);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        initialize();
    }

    public void initialize() {
        FacebookSdk.sdkInitialize(this);
        MultiDex.install(this);
        if (BuildConfig.USE_LEAK_CANARY) {
            _refWatcher = LeakCanary.install(this);
        }
        _sharedPreferences = getSharedPreferences(MASTER_KEY, Context.MODE_PRIVATE);
        _rxDataManager = RxAppDataManager.newInstance(this, _sharedPreferences, _bus);
        initializeBuildConfig();
        initializeRealm();
        initializeNotificationService();
        initializeFresco();
    }

    public void initializeFresco() {
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
            .newBuilder(this, RestClient.getClient(getRxBus(), getSharedPreferences()))
            .build();
        Fresco.initialize(this, config);
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
        RxGoogleAnalytics analytics = new RxGoogleAnalytics(this);
        if (BuildConfig.USE_GOOGLE_ANALYTICS) {
            analytics.getAnalytics().enableAdvertisingIdCollection(true);
            analytics.getAnalytics().enableAutoActivityReports(this);
        } else {
            analytics.getAnalytics().setAppOptOut(true);
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
        return Observable.interval(3, TimeUnit.MINUTES)
            .compose(_rxHelper.<Long>observeIO());
    }

    public JustObserver<Long> intervalObserver(
        final Application app, final INotificationService
        _notificationService) {
        return new JustObserver<Long>() {

            @Override
            public void next(Long timesCalled) {
                Timber.i("Checking for notifications, attempt: %1$s", timesCalled.intValue());
                if (_currentUser != null) {
                    try {
                        List<Notification> notifications =
                            _notificationService.getNotifications(_currentUser.id());
                        if (notifications != null && notifications.size() > 0) {
                            for (Notification notification : notifications) {
                                _notificationManager.notify(notification.hashCode(), notification);
                            }
                            _rxMessageSync.deleteAllFirebaseMessages(app, _currentUser).subscribe();
                        }
                    } catch (RemoteException e) {
                        Timber.e(Log.getStackTraceString(e));
                    }
                }
            }
        };
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
