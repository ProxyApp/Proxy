package com.shareyourproxy

import android.app.Application
import android.content.SharedPreferences
import android.support.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory
import com.shareyourproxy.BuildConfig.*
import com.shareyourproxy.Constants.MASTER_KEY
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.RxAppDataManager
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

/**
 * Proxy application that handles syncing the current user and handling BaseCommands.
 */
internal final class ProxyApplication : Application() {
    internal var currentUser: User = User()
    internal val sharedPreferences: SharedPreferences get() = getSharedPreferences(MASTER_KEY, MODE_PRIVATE)

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

   private fun initialize() {
        MultiDex.install(this)
        RxAppDataManager(this, sharedPreferences)
        if (USE_LEAK_CANARY) {
            refWatcher = LeakCanary.install(this)
        }
        initializeBuildConfig()
        initializeRealm()
        initializeFresco()
    }

    private fun initializeFresco() {
        val config = OkHttpImagePipelineConfigFactory.newBuilder(this, RestClient(this).oldClient).build()
        Fresco.initialize(this, config)
    }

    private fun initializeRealm() {
        val config = RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().schemaVersion(BuildConfig.VERSION_CODE.toLong()).build()
        Realm.setDefaultConfiguration(config)
    }

    private fun initializeBuildConfig() {
        if (DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        val analytics = RxGoogleAnalytics(this)
        if (USE_GOOGLE_ANALYTICS) {
            analytics.analytics.enableAdvertisingIdCollection(true)
            analytics.analytics.enableAutoActivityReports(this)
        } else {
            analytics.analytics.appOptOut = true
        }
        //Answers and Crashlytics
        if (USE_CRASHLYTICS) {
            Fabric.with(this, Crashlytics(), Answers())
        } else {
            Fabric.with(this, Answers())
        }
    }

    companion object {
        private var refWatcher: RefWatcher? = null
        fun watchForLeak(obj: Any) {
            refWatcher?.watch(obj)
        }
    }
}

