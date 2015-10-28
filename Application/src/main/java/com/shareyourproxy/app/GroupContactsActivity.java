package com.shareyourproxy.app;

import android.os.Bundle;
import android.view.MenuItem;

import com.shareyourproxy.R;
import com.shareyourproxy.app.fragment.GroupContactsFragment;

import timber.log.Timber;

/**
 * Activity to display the contacts that a user has saved in a selected group.
 */
public class GroupContactsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_users);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_view_group_users_container,
                    GroupContactsFragment.newInstance()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                Timber.e("Menu Item ID unknown");
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
    }
}
