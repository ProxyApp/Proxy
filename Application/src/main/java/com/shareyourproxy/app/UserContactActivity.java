package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.MenuItem;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.fragment.ContactProfileFragment;
import com.shareyourproxy.app.fragment.MainFragment;

import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;

/**
 * Activity that handles displaying a {@link User} profile.
 */
public class UserContactActivity extends BaseActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
        //if we launched from a notification go back to the MainActivity explicitly
        if (this.isTaskRoot()) {
            launchMainActivity(this, MainFragment.ARG_SELECT_CONTACTS_TAB, false, null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        preventStatusBarFlash(this);
        if (savedInstanceState == null) {
            User user = getUserExtra();
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_user_profile_container,
                    ContactProfileFragment.newInstance(user, getLoggedInUser().id())).commit();
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
