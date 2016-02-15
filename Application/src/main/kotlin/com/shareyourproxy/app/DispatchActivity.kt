package com.shareyourproxy.app

import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.Status
import com.shareyourproxy.IntentLauncher.launchLoginActivity
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.RxLoginHelper.loginSubscription
import com.shareyourproxy.api.rx.event.SyncContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncContactsSuccessEvent
import com.shareyourproxy.app.fragment.AggregateFeedFragment.Companion.ARG_SELECT_PROFILE_TAB
import com.shareyourproxy.app.fragment.DispatchFragment

/**
 * Activity to check if we have a cached user in SharedPreferences. Send the user to the [AggregateFeedActivity] if we have a cached user or send them to
 * [LoginActivity] if we need to login to google services and download a current user. Delete cached Realm data on startup. Fullscreen activity.
 */
internal final class DispatchActivity : GoogleApiActivity() {
    private val rxBusObserver: JustObserver<Any> = object : JustObserver<Any>(DispatchActivity::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            when (event) {
                is SyncContactsSuccessEvent -> goToUserFeedActivity()
                is SyncContactsErrorEvent -> goToLoginActivity()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(android.R.id.content, DispatchFragment()).commit()
        }
        initialize()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onGooglePlusSignIn(acct: GoogleSignInAccount?) {
        rxBusObservable().subscribe(rxBusObserver)
        loginSubscription(this)
    }

    override fun onGooglePlusError(status: Status) {
        goToLoginActivity()
    }

    /**
     * Delete any saved realm file and go full screen for the loading UI.
     */
    private fun initialize() {
        deleteRealm()
        signInToGoogle()
    }

    /**
     * Hide Navigation bar and go full screen.
     */
    private fun goFullScreen() {
        val decorView = window.decorView
        val uiOptions = SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
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
    private fun goToLoginActivity() {
        launchLoginActivity(this)
        finish()
    }
}
