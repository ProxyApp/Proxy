package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent;
import com.shareyourproxy.app.dialog.ShareLinkDialog;
import com.shareyourproxy.app.fragment.MainFragment;
import com.shareyourproxy.app.fragment.UserProfileFragment;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.IntentLauncher.launchAddressIntent;
import static com.shareyourproxy.IntentLauncher.launchElloIntent;
import static com.shareyourproxy.IntentLauncher.launchEmailIntent;
import static com.shareyourproxy.IntentLauncher.launchFBMessengerIntent;
import static com.shareyourproxy.IntentLauncher.launchFacebookIntent;
import static com.shareyourproxy.IntentLauncher.launchGithubIntent;
import static com.shareyourproxy.IntentLauncher.launchGooglePlusIntent;
import static com.shareyourproxy.IntentLauncher.launchHangoutsIntent;
import static com.shareyourproxy.IntentLauncher.launchInstagramIntent;
import static com.shareyourproxy.IntentLauncher.launchLinkedInIntent;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.IntentLauncher.launchMediumIntent;
import static com.shareyourproxy.IntentLauncher.launchMeerkatIntent;
import static com.shareyourproxy.IntentLauncher.launchPhoneIntent;
import static com.shareyourproxy.IntentLauncher.launchRedditIntent;
import static com.shareyourproxy.IntentLauncher.launchSMSIntent;
import static com.shareyourproxy.IntentLauncher.launchSkypeIntent;
import static com.shareyourproxy.IntentLauncher.launchSnapChatIntent;
import static com.shareyourproxy.IntentLauncher.launchSoundCloudIntent;
import static com.shareyourproxy.IntentLauncher.launchSpotifyIntent;
import static com.shareyourproxy.IntentLauncher.launchTumblrIntent;
import static com.shareyourproxy.IntentLauncher.launchTwitterIntent;
import static com.shareyourproxy.IntentLauncher.launchVenmoIntent;
import static com.shareyourproxy.IntentLauncher.launchWebIntent;
import static com.shareyourproxy.IntentLauncher.launchWhatsAppIntent;
import static com.shareyourproxy.IntentLauncher.launchYoIntent;
import static com.shareyourproxy.IntentLauncher.launchYoutubeIntent;
import static com.shareyourproxy.util.ViewUtils.getMenuIcon;

/**
 * Activity that handles displaying a {@link User} profile.
 */
public class UserProfileActivity extends BaseActivity {

    private CompositeSubscription _subscriptions;
    private boolean _isLoggedInUser;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
        //if we launched from a notification go back to the MainActivity explicitly
        if (this.isTaskRoot()) {
            launchMainActivity(
                this, MainFragment.ARG_SELECT_CONTACTS_TAB, false, null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        preventStatusBarFlash(this);
        setIsLoggedInUser();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_user_profile_container,
                    UserProfileFragment.newInstance()).commit();
        }
    }

    /**
     * Initialize _isLoggedInUser field.
     */
    private void setIsLoggedInUser() {
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
            MenuItem sharedLinkButton = menu.findItem(R.id.menu_current_user_shared_links);
            sharedLinkButton.setIcon(getMenuIcon(this, R.raw.ic_share));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_current_user_shared_links:
                ShareLinkDialog.newInstance(getLoggedInUser().groups())
                    .show(getSupportFragmentManager());
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
        _subscriptions.add(getRxBus().toObservable()
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Handle channel selected events to launch the correct android process.
     *
     * @param event data
     */
    public void onChannelSelected(SelectUserChannelEvent event) {
        ChannelType channelType = event.channel.channelType();
        String actionAddress = event.channel.actionAddress();
        switch (channelType) {
            case Phone:
                launchPhoneIntent(this, actionAddress);
                break;
            case SMS:
                launchSMSIntent(this, actionAddress);
                break;
            case Email:
                launchEmailIntent(this, actionAddress);
                break;
            case Web:
            case URL:
                launchWebIntent(this, actionAddress);
                break;
            case Facebook:
                launchFacebookIntent(this, actionAddress);
                break;
            case Twitter:
                launchTwitterIntent(this, actionAddress);
                break;
            case Meerkat:
                launchMeerkatIntent(this, actionAddress);
                break;
            case Snapchat:
                launchSnapChatIntent(this, actionAddress);
                break;
            case Spotify:
                launchSpotifyIntent(this, actionAddress);
                break;
            case Reddit:
                launchRedditIntent(this, actionAddress);
                break;
            case Linkedin:
                launchLinkedInIntent(this, actionAddress);
                break;
            case FBMessenger:
                launchFBMessengerIntent(this, actionAddress);
                break;
            case Googleplus:
                launchGooglePlusIntent(this, actionAddress);
                break;
            case Github:
                launchGithubIntent(this, actionAddress);
                break;
            case Address:
                launchAddressIntent(this, actionAddress);
                break;
            case Youtube:
                launchYoutubeIntent(this, actionAddress);
                break;
            case Instagram:
                launchInstagramIntent(this, actionAddress);
                break;
            case Tumblr:
                launchTumblrIntent(this, actionAddress);
                break;
            case Ello:
                launchElloIntent(this, actionAddress);
                break;
            case Venmo:
                launchVenmoIntent(this, actionAddress);
                break;
            case Medium:
                launchMediumIntent(this, actionAddress);
                break;
            case Soundcloud:
                launchSoundCloudIntent(this, actionAddress);
                break;
            case Skype:
                launchSkypeIntent(this, actionAddress);
                break;
            case Yo:
                launchYoIntent(this, actionAddress);
                break;
            case Custom:
                break;
            case Slack:
                break;
            case Hangouts:
                launchHangoutsIntent(this, actionAddress);
                break;
            case Whatsapp:
                launchWhatsAppIntent(this, actionAddress);
                break;
            case Periscope:
                break;
            default:
                break;
        }
    }
}
