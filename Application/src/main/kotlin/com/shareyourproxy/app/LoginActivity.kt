package com.shareyourproxy.app

import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.SignInButton.COLOR_DARK
import com.google.android.gms.common.SignInButton.SIZE_WIDE
import com.google.android.gms.common.api.Status
import com.shareyourproxy.BuildConfig.VERSION_CODE
import com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION
import com.shareyourproxy.IntentLauncher.launchIntroductionActivity
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.R
import com.shareyourproxy.api.RestClient.herokuUserService
import com.shareyourproxy.api.RestClient.userService
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.shareyourproxy.api.rx.RxHelper
import com.shareyourproxy.api.rx.RxHelper.observeMain
import com.shareyourproxy.api.rx.command.AddUserCommand
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.app.fragment.AggregateFeedFragment
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.util.bindView
import org.jetbrains.anko.onClick
import rx.subscriptions.CompositeSubscription
import java.util.*


/**
 * Log in with a google plus account.
 */
class LoginActivity : GoogleApiActivity() {

    private val analytics = RxGoogleAnalytics(this)
    private val proxyLogo: TextView by bindView(R.id.activity_login_title)
    private val signInButton: SignInButton by bindView(R.id.activity_login_sign_in_button)
    private val svgUltraMinor: Int = resources.getDimensionPixelSize(R.dimen.common_svg_ultra_minor)
    private var subscriptions: CompositeSubscription = CompositeSubscription()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initialize()
    }

    private fun initialize() {
        initializeValues()
        drawLogo()
    }

    private fun initializeValues() {
        signInButton.setStyle(SIZE_WIDE, COLOR_DARK)
        signInButton.isEnabled = true
        signInButton.onClick { signInToGoogle() }
    }

    /**
     * Set the Logo image.drawable on this activities [ImageView].
     */
    private fun drawLogo() {
        val draw = svgToBitmapDrawable(this, R.raw.ic_proxy_logo_typed, svgUltraMinor)
        proxyLogo.setCompoundDrawablesWithIntrinsicBounds(null, draw, null, null)
    }

    public override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusDriver.rxBusObservable().subscribe(rxBusObserver))
    }

    val rxBusObserver: JustObserver<Any> get() = object : JustObserver<Any>() {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any?) {
            if (event is SyncAllContactsSuccessEvent || event is SyncAllContactsErrorEvent) {
                login()
            }
        }

        override fun error(e: Throwable) {
            GoogleApiActivity.showErrorDialog(this@LoginActivity, getString(R.string.rx_eventbus_error))
            signInButton.isEnabled = true
        }
    }

    fun login() {
        if (sharedPreferences.getBoolean(KEY_PLAY_INTRODUCTION, true)) {
            launchIntroductionActivity(this)
        } else {
            launchMainActivity(this, AggregateFeedFragment.ARG_SELECT_PROFILE_TAB, false, null)
        }
        finish()
    }

    public override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }

    /**
     * Get Database [User]..\
     * @param account user account
     */
    private fun getUserFromFirebase(account: GoogleSignInAccount) {
        val userId = StringBuilder(GoogleApiActivity.GOOGLE_UID_PREFIX).append(account.id).toString()
        userService.getUser(userId).compose(observeMain<User>()).subscribe(getUserObserver(this, account))
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
            override fun next(user: User?) {
                if (user == null) {
                    addUserToDatabase(createUserFromGoogle(acct))
                } else {
                    RxHelper.updateRealmUser(activity, user)
                    loggedInUser = user
                    userService.updateUserVersion(user.id, VERSION_CODE).compose(observeMain<String>()).subscribe()
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
        herokuUserService.putSharedLinks(groupIds, newUser.id).compose(observeMain<Any>()).subscribe()
        post(AddUserCommand(newUser))
        post(SyncContactsCommand(newUser))
        analytics.userAdded(newUser)
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
        GoogleApiActivity.showErrorDialog(this, status.statusMessage)
        signInButton.isEnabled = true
    }
}
