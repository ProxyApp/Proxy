package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.MenuItem;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.fragment.AggregateFeedFragment;
import com.shareyourproxy.app.fragment.ContactProfileFragment;

import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;

/**
 * Activity that handles displaying a {@link User} profile.
 */
public class UserContactActivity extends BaseActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
        //if we launched from a notification go back to the AggregateFeedActivity explicitly
        if (this.isTaskRoot()) {
            IntentLauncher.launchMainActivity(this, AggregateFeedFragment.ARG_SELECT_CONTACTS_TAB, false, null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preventStatusBarFlash(this);
        if (savedInstanceState == null) {
            User user = getUserExtra();
            getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content,
                    ContactProfileFragment.newInstance(user, getLoggedInUser().id)).commit();
        }
    }

    /**
     * Get parceled user.
     */
    private User getUserExtra() {
        return getIntent().getExtras().getParcelable(ARG_USER_SELECTED_PROFILE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                Timber.e("Option item selected is unknown");
        }
        return super.onOptionsItemSelected(item);
    }
}
