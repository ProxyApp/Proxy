package com.shareyourproxy.app;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.shareyourproxy.Constants;
import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.app.fragment.ViewGroupUsersFragment;

import butterknife.ButterKnife;
import timber.log.Timber;

import static com.shareyourproxy.util.ViewUtils.getMenuIcon;

/**
 * Activity to display a groups contacts.
 */
public class ViewGroupUsersActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_users);
        ButterKnife.inject(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_view_group_users_container,
                    ViewGroupUsersFragment.newInstance()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_view_group_users, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editGroup = menu.findItem(R.id.menu_view_group_users_edit_group);
        // Add Icons to the menu items before they are displayed
        editGroup.setIcon(getMenuIcon(this, R.raw.ic_mode_edit));
        return super.onPrepareOptionsMenu(menu);
    }

    private Group getSelectedGroup() {
        return (Group) getIntent().getExtras().getParcelable(Constants.ARG_SELECTED_GROUP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_view_group_users_edit_group:
                IntentLauncher.launchEditGroupActivity(this, getSelectedGroup());
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
