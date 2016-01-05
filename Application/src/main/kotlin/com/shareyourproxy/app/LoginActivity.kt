package com.shareyourproxy.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.ConnectionResult.API_UNAVAILABLE
import com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.SignInButton.COLOR_DARK
import com.google.android.gms.common.SignInButton.SIZE_WIDE
import com.google.android.gms.common.api.Status
import com.shareyourproxy.BuildConfig.VERSION_CODE
import com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION
import com.shareyourproxy.IntentLauncher.launchIntroductionActivity
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.string.login_error_update_play_service
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.shareyourproxy.api.rx.RxHelper
import com.shareyourproxy.api.rx.RxHelper.observeMain
import com.shareyourproxy.api.rx.command.AddUserCommand
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.app.fragment.AggregateFeedFragment
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.*


/**
 * Log in with a google plus account.
 */
private final class LoginActivity : GoogleApiActivity() {
    private val analytics = RxGoogleAnalytics(this)
    private val proxyLogo: TextView by bindView(R.id.activity_login_title)
    private val signInButton: SignInButton by bindView(R.id.activity_login_sign_in_button)
    private val svgUltraMinor: Int  by bindDimen(R.dimen.common_svg_ultra_minor)
    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private val rxBusObserver: JustObserver<Any> = object : JustObserver<Any>() {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is SyncAllContactsSuccessEvent || event is SyncAllContactsErrorEvent) {
                login()
            }
        }

        override fun error(e: Throwable) {
            GoogleApiActivity.showErrorDialog(this@LoginActivity, getString(R.string.rx_eventbus_error))
            signInButton.isEnabled = true
        }
    }
    private val onClickSignIn: View.OnClickListener = View.OnClickListener {
        signInToGoogle()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusRelay.rxBusObservable().subscribe(rxBusObserver))
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }

    override fun onGooglePlusSignIn(acct: GoogleSignInAccount?) {
        if (acct != null) {
            getUserFromFirebase(acct)
        } else {
            GoogleApiActivity.showErrorDialog(this, getString(R.string.login_error_retrieving_user))
            signInButton.isEnabled = true
        }
    }

    override fun onGooglePlusError(status: Status) {
        GoogleApiActivity.showErrorDialog(this, status.toString())
        signInButton.isEnabled = true
    }

    /**
     * onConnectionFailed is called when our Activity could not connect to Google Play services. onConnectionFailed indicates that the user needs to select an
     * account, grant permissions or resolve an onError in order to sign in.
     */
    override fun onConnectionFailed(result: ConnectionResult) {
        // Refer to the javadoc for ConnectionResult to see what onError codes might
        // be returned in onConnectionFailed.
        Timber.i("onConnectionFailed, Error Code = ${result.errorCode}")
        when (result.errorCode) {
            API_UNAVAILABLE -> apiUnavailable()
            SERVICE_VERSION_UPDATE_REQUIRED -> updateServiceVersion()
            else -> {}
        }
    }

    private fun updateServiceVersion() {
        showErrorDialog(this, getString(login_error_update_play_service))
        signInButton.isEnabled = true
    }

    private fun apiUnavailable() {
        val error = getString(R.string.login_error_api_unavailable)
        Timber.w(error)
        showErrorDialog(this, error)
        signInButton.isEnabled = true
    }

    private fun initialize() {
        initializeValues()
        drawLogo()
    }

    /**
     * Set the Logo image drawable on this activities ImageView.
     */
    private fun drawLogo() {
        val draw = svgToBitmapDrawable(this, R.raw.ic_proxy_logo_typed, svgUltraMinor)
        proxyLogo.setCompoundDrawablesWithIntrinsicBounds(null, draw, null, null)
    }

    private fun initializeValues() {
        signInButton.setStyle(SIZE_WIDE, COLOR_DARK)
        signInButton.isEnabled = true
        signInButton.setOnClickListener (onClickSignIn)
    }

    private fun login() {
        if (sharedPreferences.getBoolean(KEY_PLAY_INTRODUCTION, true)) {
            launchIntroductionActivity(this)
        } else {
            launchMainActivity(this, AggregateFeedFragment.ARG_SELECT_PROFILE_TAB, false, null)
        }
        finish()
    }

    /**
     * Get Database [User]..\
     * @param account user account
     */
    private fun getUserFromFirebase(account: GoogleSignInAccount) {
        val userId = StringBuilder(GoogleApiActivity.GOOGLE_UID_PREFIX).append(account.id).toString()
        RestClient(this).herokuUserService.getUser(userId).compose(observeMain<User>()).subscribe(getUserObserver(this, account))
    }

    /**
     * This Observer eventually calls SyncAllContactsCommand which calls login.
     * @param activity context
     * @param acct user account
     * @return current user
     */
    private fun getUserObserver(activity: BaseActivity, acct: GoogleSignInAccount): JustObserver<User> {
        return object : JustObserver<User>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(user: User) {
                if (user == User()) {
                    addUserToDatabase(createUserFromGoogle(acct))
                } else {
                    RxHelper.updateRealmUser(activity, user)
                    loggedInUser = user
                    RestClient(activity).herokuUserService.updateUserVersion(user.id, VERSION_CODE).compose(observeMain<String>()).subscribe()
                    post(SyncContactsCommand(user))
                }
            }

            override fun error(e: Throwable) {
                GoogleApiActivity.showErrorDialog(activity, getString(R.string.retrofit_general_error))
                signInButton.isEnabled = true
            }
        }
    }

    /**
     * Add a [User] to FireBase.
     * @param newUser the [User] to log in
     */
    private fun addUserToDatabase(newUser: User) {
        loggedInUser = newUser
        val userGroups = newUser.groups
        val groupIds = ArrayList<String>(userGroups.size)
        for (group in userGroups.values) {
            groupIds.add(group.id)
        }
        post(AddUserCommand(newUser))
        post(SyncContactsCommand(newUser))
        analytics.userAdded(newUser)
    }

}
