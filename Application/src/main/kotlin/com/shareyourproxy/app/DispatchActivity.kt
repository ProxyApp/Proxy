package com.shareyourproxy.app

import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import com.shareyourproxy.IntentLauncher.launchLoginActivity
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.app.fragment.AggregateFeedFragment.Companion.ARG_SELECT_PROFILE_TAB
import com.shareyourproxy.app.fragment.DispatchFragment
import rx.subscriptions.CompositeSubscription

/**
 * Activity to check if we have a cached user in SharedPreferences. Send the user to the [AggregateFeedActivity] if we have a cached user or send them to
 * [LoginActivity] if we need to login to google services and download a current user. Delete cached Realm data on startup. Fullscreen activity.
 */
class DispatchActivity : GoogleApiActivity() {
    private var subscriptions: CompositeSubscription = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(android.R.id.content, DispatchFragment.newInstance()).commitAllowingStateLoss()
        }
        initialize()
    }

    public override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusDriver.rxBusObservable().subscribe(rxBusObserver))
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }

    /**
     * Delete any saved realm file and go full screen for the loading UI.
     */
    private fun initialize() {
        deleteRealm()
        goFullScreen()
    }

    /**
     * Hide Navigation bar and go full screen.
     */
    private fun goFullScreen() {
        val decorView = window.decorView
        val uiOptions = SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
    }

    val rxBusObserver: JustObserver<Any> get() = object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any?) {
                if (event is SyncAllContactsSuccessEvent) {
                    goToUserFeedActivity()
                } else if (event is SyncAllContactsErrorEvent) {
                    goToLoginActivity()
                }
            }
        }

    /**
     * Go to the main user feed activity and finish this one.
     */
    private fun goToUserFeedActivity() {
        launchMainActivity(this, ARG_SELECT_PROFILE_TAB, false, null)
        finish()
    }

    /**
     * Launch the login activity and finish this dispatch activity.
     */
    fun goToLoginActivity() {
        launchLoginActivity(this)
        finish()
    }
}
