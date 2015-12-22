package com.shareyourproxy;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.RxAppDataManager;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.RxGoogleAnalytics;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

import static com.shareyourproxy.Constants.MASTER_KEY;

/**
 * Proxy application that handles syncing the current user and handling BaseCommands.
 */
public class ProxyApplication extends Application {

    private static RefWatcher _refWatcher;
    private final RxBusDriver _bus = RxBusDriver.INSTANCE;
    private User _currentUser;
    private SharedPreferences _sharedPreferences;

    public static void watchForLeak(Object object) {
        if (_refWatcher != null)
            _refWatcher.watch(object);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    public void initialize() {
        FacebookSdk.sdkInitialize(this);
        MultiDex.install(this);
        if (BuildConfig.USE_LEAK_CANARY) {
            _refWatcher = LeakCanary.install(this);
        }
        _sharedPreferences = getSharedPreferences(MASTER_KEY, Context.MODE_PRIVATE);
        RxAppDataManager.newInstance(this, _sharedPreferences, _bus);
        initializeBuildConfig();
        initializeRealm();
        initializeFresco();
    }

    public void initializeFresco() {
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
            .newBuilder(this, RestClient.getClient())
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
        TwitterAuthConfig authConfig = new TwitterAuthConfig(BuildConfig.TWITTER_KEY, BuildConfig.TWITTER_SECRET);
        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Twitter(authConfig), new Crashlytics(), new Answers());
        } else {
            Fabric.with(this, new Twitter(authConfig), new Answers());
        }
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
            _currentUser = currentUser;
    }

    public RxBusDriver getRxBus() {
        return _bus;
    }

    public SharedPreferences getSharedPreferences() {
        return _sharedPreferences;
    }

}
