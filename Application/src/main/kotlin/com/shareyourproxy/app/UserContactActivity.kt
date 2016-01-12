package com.shareyourproxy.app

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.MenuItem
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.app.fragment.AggregateFeedFragment
import com.shareyourproxy.app.fragment.ContactProfileFragment
import com.shareyourproxy.util.ButterKnife.LazyVal
import timber.log.Timber

/**
 * Activity that handles displaying a [User] profile.
 */
internal final class UserContactActivity : BaseActivity() {
    private val userExtra: User by LazyVal { intent.extras.getParcelable<User>(ARG_USER_SELECTED_PROFILE) }

    override fun onBackPressed() {
        super.onBackPressed()
        ActivityCompat.finishAfterTransition(this)
        //if we launched from a notification go back to the AggregateFeedActivity explicitly
        if (this.isTaskRoot) {
            launchMainActivity(this, AggregateFeedFragment.ARG_SELECT_CONTACTS_TAB, false, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preventStatusBarFlash(this)
        if (savedInstanceState == null) {
            val user = userExtra
            supportFragmentManager.beginTransaction().replace(android.R.id.content, ContactProfileFragment(user, loggedInUser.id)).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> Timber.e("Option item selected is unknown")
        }
        return super.onOptionsItemSelected(item)
    }
}
