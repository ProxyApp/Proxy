package com.shareyourproxy.app

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes.EMAIL
import com.google.android.gms.common.Scopes.PLUS_ME
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.Status
import com.google.android.gms.plus.Plus
import com.google.android.gms.plus.Plus.*
import com.shareyourproxy.BuildConfig
import com.shareyourproxy.BuildConfig.VERSION_CODE
import com.shareyourproxy.Constants
import com.shareyourproxy.R
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustSingle
import com.shareyourproxy.api.rx.RxHelper.singleObserveMain
import com.shareyourproxy.api.rx.RxLoginHelper.refreshGooglePlusToken
import com.shareyourproxy.app.dialog.ErrorDialog
import com.shareyourproxy.util.StringUtils
import timber.log.Timber
import java.util.*

/**
 * Base abstraction for classes to inherit common google plus login callbacks and functions.
 */
abstract class GoogleApiActivity : BaseActivity(), ConnectionCallbacks, OnConnectionFailedListener {
    private var googleApiClient: GoogleApiClient?  = null

    protected fun connectGoogleApiClient() {
        when{
            googleApiClient!!.isConnected -> onConnected(null)
            googleApiClient!!.isConnecting ->{}
            else -> googleApiClient?.connect()
        }
    }

    open fun onOldGooglePlusTokenGen() {
    }

    open fun onGooglePlusSignIn(acct: GoogleSignInAccount?) {
    }

    open fun onGooglePlusError(status: Status) {
    }

    protected fun signInToGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_NEW_SIGN_IN)
    }

    protected fun oldSignInToGoogle() {
        refreshGooglePlusToken(this, googleApiClient).compose(singleObserveMain<String>()).subscribe(googleRefreshObserver())
    }

    private fun googleRefreshObserver(): JustSingle<String> {
        return object : JustSingle<String>() {
            override fun onSuccess(value: String) {
                sharedPreferences.edit().putString(Constants.KEY_GOOGLE_PLUS_AUTH, value)
                onOldGooglePlusTokenGen()
            }
        }
    }

    /**
     * Create a User from their google profile.
     * @return created user
     */
    protected fun createUserFromGoogle(acct: GoogleSignInAccount): User {
        val id = acct.id
        // Retrieve some profile information to personalize our app for the user.
        val currentUser = RestClient(this).herokuUserService.getCurrentPerson(id).toBlocking().single()
        val userId = StringBuilder(GOOGLE_UID_PREFIX).append(id).toString()
        val firstName = currentUser.name.givenName
        val lastName = currentUser.name.familyName
        val fullName = StringUtils.buildFullName(firstName, lastName)
        val email = acct.email
        val profileURL = acct.photoUrl.toString()
        val cover = currentUser.cover
        val coverPhoto = cover?.coverPhoto
        var coverURL: String = if (coverPhoto != null) coverPhoto.url else ""
        //Create a new User with empty groups, contacts, and channels
        return User(userId, firstName, lastName, fullName, email, profileURL, coverURL, HashMap(), HashSet(), defaultGroups, VERSION_CODE)
    }

    private val defaultGroups: HashMap<String, Group>
        get() {
            val groups = HashMap<String, Group>(3)
            val groupLabels = resources.getStringArray(R.array.default_groups)
            for (label in groupLabels) {
                val group = Group(UUID.randomUUID().toString(), label, HashSet(), HashSet())
                groups.put(group.id, group)
            }
            return groups
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleApiClient = buildOldGoogleApiClient(this)
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient!!.isConnected) {
            googleApiClient?.disconnect()
        }
    }

    override fun onConnected(bundle: Bundle?) {
        //nada
    }

    override fun onConnectionSuspended(i: Int) {
        //nada
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        //nada
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when(requestCode){
            RC_NEW_SIGN_IN -> rcNewSignIn(data)
            else ->{}
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Starts an appropriate intent or dialog for user interaction to resolve the current onError preventing the user from being signed in.  This could be a
     * dialog allowing the user to select an account, an activity allowing the user to consent to the permissions being requested by your app, a setting to
     * enable device networking, etc.
     */
    protected  fun resolveSignInError(intent : PendingIntent) {
        try {
            startIntentSenderForResult(intent.intentSender, RC_SIGN_IN, null, 0, 0, 0);
        } catch (e: Throwable) {
            Timber.i("Sign in intent could not be sent: ${Log.getStackTraceString(e)}")
            connectGoogleApiClient()
        }
    }

    private fun rcNewSignIn(data: Intent) {
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        if (result.isSuccess) {
            sharedPreferences.edit().putString(Constants.KEY_GOOGLE_PLUS_AUTH, result.signInAccount.serverAuthCode)
            onGooglePlusSignIn(result.signInAccount)
        } else {
            onGooglePlusError(result.status)
        }
    }

    companion object {
        val GOOGLE_UID_PREFIX = "google:"
        val RC_SIGN_IN = 0
        private val RC_NEW_SIGN_IN = 1
        private val OPTIONS = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestServerAuthCode(BuildConfig.GOOGLE_CLIENT_ID).requestEmail().build()
        /**
         * Return log in onError dialog based on the type of onError.
         * @param message onError message
         */
        fun showErrorDialog(activity: BaseActivity, message: String) {
            var error = message
            if (message.trim { it <= ' ' }.isEmpty()) {
                error = "null error message"
            }
            ErrorDialog(activity.getString(R.string.login_error), "Error authenticating with Google: $error").show(activity.supportFragmentManager)
        }

        private fun buildGoogleApiClient(activity: GoogleApiActivity): GoogleApiClient {
            return GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(activity)
                    .enableAutoManage(activity, activity)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, OPTIONS)
                    .build()
        }

        private fun buildOldGoogleApiClient(activity: GoogleApiActivity): GoogleApiClient {
            return GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(activity)
                    .addOnConnectionFailedListener(activity)
                    .addApi(API, Plus.PlusOptions.builder().build())
                    .addScope(SCOPE_PLUS_LOGIN)
                    .addScope(Scope(PLUS_ME))
                    .addScope(Scope(EMAIL))
                    .addScope(SCOPE_PLUS_PROFILE)
                    .build();
        }
    }
}
