package com.shareyourproxy.app;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent;
import com.shareyourproxy.app.fragment.MainFragment;
import com.shareyourproxy.app.fragment.UserProfileFragment;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.util.ViewUtils.getMenuIcon;
import static rx.android.app.AppObservable.bindActivity;

/**
 * Activity that handles displaying a {@link User} profile.
 */
public class UserProfileActivity extends BaseActivity {

    private CompositeSubscription _subscriptions;
    private boolean _isLoggedInUser;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
        //if we launched from a notification go back to the MainActivity explicitly
        if (this.isTaskRoot()) {
            IntentLauncher.launchMainActivity(this, MainFragment.ARG_SELECT_CONTACTS_TAB);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_user_profile_container,
                    UserProfileFragment.newInstance()).commit();
        }
        User userContact = getIntent().getExtras().getParcelable
            (ARG_USER_SELECTED_PROFILE);
        _isLoggedInUser = isLoggedInUser(userContact);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        if (_isLoggedInUser) {
            inflater.inflate(R.menu.menu_activity_current_user, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (_isLoggedInUser) {
            MenuItem addButton = menu.findItem(R.id.menu_current_user_add_channel);
            addButton.setIcon(getMenuIcon(this, R.raw.ic_add));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_current_user_add_channel:
                IntentLauncher.launchChannelListActivity(this);
                break;
            default:
                Timber.e("Option item selected is unknown");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.i("onResume");
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindActivity(this, getRxBus().toObserverable())
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof SelectUserChannelEvent) {
                        onChannelSelected((SelectUserChannelEvent) event);
                    }
                }
            }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
    }

    /**
     * Handle channel selected events to launch the correct android process.
     * @param event data
     */
    public void onChannelSelected(SelectUserChannelEvent event) {
        ChannelType channelType = event.channel.channelType();
        String actionAddress = event.channel.actionAddress();
        switch (channelType) {
            case Phone:
                IntentLauncher.launchPhoneIntent(this,actionAddress);
                break;
            case SMS:
                IntentLauncher.launchSMSIntent(this, actionAddress);
                break;
            case Email:
                IntentLauncher.launchEmailIntent(this, actionAddress);
                break;
            case Web:
                IntentLauncher.launchWebIntent(this, actionAddress);
                break;
            case Facebook:
                IntentLauncher.launchFacebookIntent(this, actionAddress);
                break;
            case Custom:
                break;
        }
    }
}
