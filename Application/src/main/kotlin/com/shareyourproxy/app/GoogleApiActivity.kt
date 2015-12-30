package com.shareyourproxy.app

import android.content.Intent
import android.os.Bundle
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
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxLoginHelper.refreshGooglePlusToken
import com.shareyourproxy.app.dialog.ErrorDialog
import com.shareyourproxy.util.ObjectUtils
import java.util.*
import kotlin.reflect.KProperty

/**
 * Base abstraction for classes to inherit common google plus login callbacks and functions.
 */
abstract class GoogleApiActivity : BaseActivity(), ConnectionCallbacks, OnConnectionFailedListener {
    private val googleApiClient: GoogleApiClient by lazy {}
    operator fun Any.getValue(activity: GoogleApiActivity, property: KProperty<*>): GoogleApiClient {
        return buildOldGoogleApiClient(activity)
    }

    open fun onGooglePlusTokenGen(acct: GoogleSignInAccount?) {
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
        refreshGooglePlusToken(this, googleApiClient).subscribe(object : JustObserver<String>(){
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(string: String) {
                sharedPreferences.edit().putString(Constants.KEY_GOOGLE_PLUS_AUTH, string)
            }
        })
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
        val fullName = ObjectUtils.buildFullName(firstName, lastName)
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
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient.isConnected) {
            googleApiClient.disconnect()
        }
    }

    override fun onConnected(bundle: Bundle?) {
        //nada
    }

    override fun onConnectionSuspended(i: Int) {
        //nada
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        //nada
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_NEW_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                sharedPreferences.edit().putString(Constants.KEY_GOOGLE_PLUS_AUTH, result.signInAccount.serverAuthCode)
                onGooglePlusSignIn(result.signInAccount)
            } else {
                onGooglePlusError(result.status)
            }
        }
    }

    companion object {
        val GOOGLE_UID_PREFIX = "google:"
        private val RC_SIGN_IN = 0
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
