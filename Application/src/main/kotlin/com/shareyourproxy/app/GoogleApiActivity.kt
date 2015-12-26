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
import com.shareyourproxy.BuildConfig.VERSION_CODE
import com.shareyourproxy.R
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.app.dialog.ErrorDialog
import com.shareyourproxy.util.ObjectUtils
import java.util.*

/**
 * Base abstraction for classes to inherit common google plus login callbacks and functions.
 */
abstract class GoogleApiActivity : BaseActivity(), ConnectionCallbacks, OnConnectionFailedListener {
    private var googleApiClient: GoogleApiClient? = null

    open fun onGooglePlusSignIn(acct: GoogleSignInAccount?) {
    }

    open fun onGooglePlusError(status: Status) {
    }

    protected fun signInToGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /**
     * Create a User from their google profile.
     * @return created user
     */
    protected fun createUserFromGoogle(acct: GoogleSignInAccount): User {
        val id = acct.id

        // Retrieve some profile information to personalize our app for the user.
        val currentUser = RestClient.herokuUserService.getCurrentPerson(id).toBlocking().single()
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
        googleApiClient = buildGoogleApiClient(this)
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient!!.isConnected) {
            googleApiClient!!.disconnect()
        }
    }

    override fun onConnected(bundle: Bundle?) {

    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                onGooglePlusSignIn(result.signInAccount)
            } else {
                onGooglePlusError(result.status)
            }
        }
    }

    companion object {
        val GOOGLE_UID_PREFIX = "google:"
        private val RC_SIGN_IN = 0
        private val OPTIONS = signInOptions

        private val signInOptions: GoogleSignInOptions
            get() = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()

        /**
         * Return log in onError dialog based on the type of onError.
         * @param message onError message
         */
        fun showErrorDialog(activity: BaseActivity, message: String?) {
            var error = message
            if (message == null || message.trim { it <= ' ' }.isEmpty()) {
                error = "null error message"
            }
            ErrorDialog.newInstance(activity.getString(R.string.login_error), "Error authenticating with Google: $error").show(activity.supportFragmentManager)
        }

        private fun buildGoogleApiClient(activity: GoogleApiActivity): GoogleApiClient {
            return GoogleApiClient.Builder(activity).addConnectionCallbacks(activity).addOnConnectionFailedListener(activity).enableAutoManage(activity, activity).addApi(Auth.GOOGLE_SIGN_IN_API, OPTIONS).build()
        }
    }
}
