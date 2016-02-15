package com.shareyourproxy.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.google.gson.GsonBuilder
import com.shareyourproxy.BuildConfig
import com.shareyourproxy.Constants
import com.shareyourproxy.IntentLauncher.launchShareLinkIntent
import com.shareyourproxy.ProxyApplication
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.command.eventcallback.ShareLinkEventCallback
import com.shareyourproxy.api.rx.event.OnBackPressedEvent
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

/**
 * Base abstraction for all activities to inherit from.
 */
internal abstract class BaseActivity : AppCompatActivity() {
    /**
     * Get the common shared preferences used to save a copy of the logged in user.
     * @return common shared preferences
     */
    val sharedPreferences: SharedPreferences
        get() = (application as ProxyApplication).sharedPreferences

    val sharedPrefJsonUser: User
        get() {
            var user: User = User()
            val jsonUser = sharedPreferences.getString(Constants.KEY_LOGGED_IN_USER, null)
            val gson = GsonBuilder().create()
            try {
                user = gson.fromJson(jsonUser, User::class.java)
            } catch (e: Exception) {
                Timber.e(Log.getStackTraceString(e))
            }
            return user
        }
    /**
     * Get currently logged in [User] in this [ProxyApplication].
     * @return logged in user
     */
    internal var loggedInUser: User
        get() = (application as ProxyApplication).currentUser
        set(user) {
            (application as ProxyApplication).currentUser = user
        }

    /**
     * This prevents the Android status bar and navigation bar from flashing during a transition animation bundled in [ ][IntentLauncher.launchSearchActivity] and [IntentLauncher.launchUserProfileActivity].
     */
    protected fun preventStatusBarFlash(activity: Activity) {
        ActivityCompat.postponeEnterTransition(activity)
        val decor = activity.window.decorView
        decor.viewTreeObserver.addOnPreDrawListener {
            ActivityCompat.startPostponedEnterTransition(activity)
            true
        }
    }

    internal fun isLoggedInUser(user: User): Boolean {
        return user.id.equals(loggedInUser.id)
    }

    internal fun buildToolbar(toolbar: Toolbar, title: String, icon: Drawable?) {
        setSupportActionBar(toolbar)
        val bar = supportActionBar
        bar?.title = title
        bar?.setDisplayHomeAsUpEnabled(true)
        bar?.setHomeAsUpIndicator(icon)
    }

    internal fun buildCustomToolbar(toolbar: Toolbar, customView: View) {
        toolbar.removeAllViews()
        toolbar.addView(customView)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
    }

    /**
     * Function to delete the main realm configuration.
     */
    protected fun deleteRealm() {
        val config = RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(BuildConfig.VERSION_CODE.toLong())
                .build()
        Realm.deleteRealm(config)
    }

    override fun onResume() {
        super.onResume()
        rxBusObservable().subscribe(onNextEvent(this))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        post(OnBackPressedEvent())
    }

    private fun onNextEvent(activity: Activity): JustObserver<Any> {
        return object : JustObserver<Any>(BaseActivity::class.java) {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any) {
                if (event is ShareLinkEventCallback) {
                    launchShareLinkIntent(activity, event)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragments = supportFragmentManager?.fragments
        if (fragments?.size!! > 0) {
            fragments?.forEach { it?.onActivityResult(requestCode, resultCode, data) }
        }
    }
}
