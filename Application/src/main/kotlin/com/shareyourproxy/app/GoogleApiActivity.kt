package com.shareyourproxy.app

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.common.api.Status
import com.shareyourproxy.BuildConfig
import com.shareyourproxy.BuildConfig.VERSION_CODE
import com.shareyourproxy.Constants.KEY_GOOGLE_PLUS_AUTH
import com.shareyourproxy.R
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustSingle
import com.shareyourproxy.app.dialog.ErrorDialog
import com.shareyourproxy.util.ButterKnife.LazyVal
import com.shareyourproxy.util.StringUtils
import java.util.*

/**
 * Base abstraction for classes to inherit common google plus login callbacks and functions.
 */
internal abstract class GoogleApiActivity : BaseActivity(), ConnectionCallbacks, OnConnectionFailedListener {
    private val googleApiClient: GoogleApiClient by LazyVal { buildGoogleApiClient(this) }

    open fun onGooglePlusSignIn(acct: GoogleSignInAccount?) {
    }

    open fun onGooglePlusError(status: Status) {
    }

    protected fun signInToGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun googleRefreshObserver(): JustSingle<String> {
        return object : JustSingle<String>(GoogleApiActivity::class.java) {
            override fun onSuccess(value: String) {
                sharedPreferences.edit().putString(KEY_GOOGLE_PLUS_AUTH, value)
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
        val currentUser = RestClient(this).herokuUserService.getGooglePlusPerson(id!!).toBlocking().single()
        val userId = StringBuilder(GOOGLE_UID_PREFIX).append(id).toString()
        val firstName = currentUser.name.first
        val lastName = currentUser.name.last
        val fullName = StringUtils.buildFullName(firstName, lastName)
        val email = acct.email
        val profileURL = acct.photoUrl.toString()
        var coverURL: String = currentUser.cover.coverPhoto.url
        //Create a new User with empty groups, contacts, and channels
        return User(userId, firstName, lastName, fullName, email!!, profileURL, coverURL, HashMap(), HashSet(), defaultGroups, VERSION_CODE)
    }

    private val defaultGroups: HashMap<String, Group>
        get() {
            val groups = HashMap<String, Group>(3)
            val groupLabels = resources.getStringArray(R.array.default_groups)
            groupLabels.forEach {
                val group = Group(UUID.randomUUID().toString(), it, HashSet(), HashSet())
                groups.put(group.id, group)
            }
            return groups
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

    override fun onConnectionFailed(result: ConnectionResult) {
        //nada
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            RC_SIGN_IN -> signIn(data)
            else -> {
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun signIn(data: Intent) {
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        if (result.isSuccess) {
            sharedPreferences.edit().putString(KEY_GOOGLE_PLUS_AUTH, result.signInAccount?.serverAuthCode).commit()
            onGooglePlusSignIn(result.signInAccount)
        } else {
            onGooglePlusError(result.status)
        }
    }

    companion object {
        val GOOGLE_UID_PREFIX = "google:"
        val RC_SIGN_IN = 0
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
            ErrorDialog.show(activity.supportFragmentManager, activity.getString(R.string.login_error), "Error authenticating with Google: $error")
        }

        fun buildGoogleApiClient(activity: GoogleApiActivity): GoogleApiClient {
            return GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(activity)
                    .enableAutoManage(activity, activity)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, OPTIONS)
                    .build()
        }
    }
}
