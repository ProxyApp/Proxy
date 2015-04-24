package com.proxy.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.proxy.IntentLauncher;
import com.proxy.R;
import com.proxy.api.domain.model.ChannelType;
import com.proxy.api.domain.model.User;
import com.proxy.app.fragment.UserProfileFragment;
import com.proxy.event.ChannelSelectedEvent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.proxy.Constants.ARG_USER_LOGGED_IN;
import static com.proxy.api.domain.factory.ChannelFactory.getModelChannelType;
import static com.proxy.util.ViewUtils.getMenuIcon;
import static rx.android.app.AppObservable.bindActivity;

/**
 * Activity that handles displaying a {@link User} profile.
 */
public class UserProfileActivity extends BaseActivity {

    @InjectView(R.id.common_toolbar)
    Toolbar mToolbar;
    private boolean mIsLoggedInUser;
    private CompositeSubscription mSubscriptions;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
    }

    private boolean isLoggedInUser() {
        return getIntent().getExtras().getBoolean(ARG_USER_LOGGED_IN);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.inject(this);
        initialize();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_user_profile_container,
                    UserProfileFragment.newInstance()).commit();
        }
    }

    /**
     * Initialize this view.
     */
    private void initialize() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        if (mIsLoggedInUser) {
            inflater.inflate(R.menu.menu_activity_current_user, menu);
        } else {
            inflater.inflate(R.menu.menu_activity_user_profile, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (mIsLoggedInUser) {
            MenuItem addButton = menu.findItem(R.id.menu_current_user_add_channel);
            addButton.setIcon(getMenuIcon(this, R.raw.add));
        } else {
            MenuItem dial = menu.findItem(R.id.menu_user_profile_dial);
            MenuItem favorite = menu.findItem(R.id.menu_user_profile_favorite);
            MenuItem group = menu.findItem(R.id.menu_user_profile_group);

            // Add Icons to the menu items before they are displayed
            dial.setIcon(getMenuIcon(this, R.raw.phone));
            favorite.setIcon(getMenuIcon(this, R.raw.star));
            group.setIcon(getMenuIcon(this, R.raw.groups));
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
        mIsLoggedInUser = isLoggedInUser();
        mSubscriptions = new CompositeSubscription();
        mSubscriptions.add(bindActivity(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof ChannelSelectedEvent) {
                        onChannelSelected((ChannelSelectedEvent) event);
                    }
                }
            }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscriptions.unsubscribe();
    }

    @SuppressWarnings("unused")
    public void onChannelSelected(ChannelSelectedEvent event) {
        ChannelType channelType = getModelChannelType(event.channel.getChannelType());
        switch (channelType) {
            case Phone:
                IntentLauncher.launchPhoneIntent(this, event.channel.getActionAddress());
                break;
            case SMS:
                IntentLauncher.launchSMSIntent(this, event.channel.getActionAddress());
                break;
            case Email:
                IntentLauncher.launchEmailIntent(this, event.channel.getActionAddress());
                break;
            case Web:
                break;
            case Custom:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getIntent().replaceExtras(data);
    }

}
