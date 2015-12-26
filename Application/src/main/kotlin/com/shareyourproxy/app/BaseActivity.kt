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
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.command.eventcallback.ShareLinkEventCallback
import com.shareyourproxy.api.rx.event.OnBackPressedEvent
import io.realm.Realm
import io.realm.RealmConfiguration
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

/**
 * Base abstraction for all activities to inherit from.
 */
abstract class BaseActivity : AppCompatActivity() {
    private var _subscriptions: CompositeSubscription = CompositeSubscription()
    private val gson = GsonBuilder().create()
    /**
     * Get currently logged in [User] in this [ProxyApplication].

     * @return logged in user
     */
    /**
     * Set the currently logged in [User] in this [ProxyApplication].

     * @param user currently logged in
     */
    var loggedInUser: User
        get() = (application as ProxyApplication).currentUser
        set(user) {
            (application as ProxyApplication).currentUser = user
        }

    /**
     * This prevents the Android status bar and navigation bar from flashing during a transition animation bundled in [ ][IntentLauncher.launchSearchActivity] and [IntentLauncher.launchUserProfileActivity].
     */
    fun preventStatusBarFlash(activity: Activity) {
        ActivityCompat.postponeEnterTransition(activity)
        val decor = activity.window.decorView
        decor.viewTreeObserver.addOnPreDrawListener {
            ActivityCompat.startPostponedEnterTransition(activity)
            true
        }
    }

    /**
     * Get the common shared preferences used to save a copy of the logged in user.

     * @return common shared preferences
     */
    val sharedPreferences: SharedPreferences
        get() = (application as ProxyApplication).sharedPreferences

    fun isLoggedInUser(user: User): Boolean {
        return loggedInUser != null && user.id.equals(loggedInUser!!.id)
    }

    fun buildToolbar(toolbar: Toolbar, title: String, icon: Drawable?) {
        setSupportActionBar(toolbar)
        val bar = supportActionBar
        bar.title = title
        bar.setDisplayHomeAsUpEnabled(true)
        bar.setHomeAsUpIndicator(icon)
    }

    fun buildCustomToolbar(toolbar: Toolbar, customView: View) {
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
        val config = RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().schemaVersion(BuildConfig.VERSION_CODE.toLong()).build()
        Realm.deleteRealm(config)
    }

    val sharedPrefJsonUser: User?
        get() {
            var user: User? = null
            val jsonUser = sharedPreferences.getString(Constants.KEY_LOGGED_IN_USER, null)
            val gson = GsonBuilder().create()
            try {
                user = gson.fromJson(jsonUser, User::class.java)
            } catch (e: Exception) {
                Timber.e(Log.getStackTraceString(e))
            }

            return user
        }

    override fun onResume() {
        super.onResume()
        _subscriptions.add(RxBusDriver.toObservable().subscribe(onNextEvent(this)))
    }

    override fun onPause() {
        super.onPause()
        _subscriptions.unsubscribe()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        post(OnBackPressedEvent())
    }

    private fun onNextEvent(activity: Activity): JustObserver<Any> {
        return object : JustObserver<Any>() {
            override fun next(event: Any?) {
                if (event is ShareLinkEventCallback) {
                    launchShareLinkIntent(activity, event)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragments = supportFragmentManager.fragments
        if (fragments != null && fragments.size > 0) {
            for (fragment in fragments) {
                fragment?.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    companion object {

        val SCOPE_EMAIL = "https://www.googleapis.com/auth/plus.profile.emails.read"
    }
}
