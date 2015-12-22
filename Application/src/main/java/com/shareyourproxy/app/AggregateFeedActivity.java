package com.shareyourproxy.app;

import android.os.Bundle;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxGoogleAnalytics;
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent;
import com.shareyourproxy.app.dialog.ShareLinkDialog;
import com.shareyourproxy.app.fragment.AggregateFeedFragment;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.IntentLauncher.launchEmailIntent;
import static com.shareyourproxy.IntentLauncher.launchIntroductionActivity;
import static com.shareyourproxy.IntentLauncher.launchInviteFriendIntent;
import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;


/**
 * The main landing point after loggin in. This is tabbed activity with {@link Contact}s and {@link Group}s.
 */
public class AggregateFeedActivity extends BaseActivity {
    private final RxGoogleAnalytics _analytics = new RxGoogleAnalytics(this);
    private CompositeSubscription _subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            AggregateFeedFragment aggregateFeedFragment = AggregateFeedFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, aggregateFeedFragment)
                .commit();
        }
    }

    /**
     * {@link SelectDrawerItemEvent}. When a drawer item is selected, call a proper event flow.
     *
     * @param event data
     */
    public void onDrawerItemSelected(SelectDrawerItemEvent event) {
        switch (event.drawerItem) {
            case PROFILE:
                User user = getLoggedInUser();
                _analytics.userProfileViewed(user);
                launchUserProfileActivity(this, user, user.id());
                break;
            case SHARE_PROFILE:
                ShareLinkDialog.newInstance(getLoggedInUser().groups())
                    .show(getSupportFragmentManager());
                break;
            case INVITE_FRIEND:
                launchInviteFriendIntent(this);
                break;
            case TOUR:
                launchIntroductionActivity(this);
                break;
            case REPORT_ISSUE:
                launchEmailIntent(this, getString(R.string.contact_proxy));
                break;
            case HEADER:
                //nada
                break;
            default:
                Timber.e("Invalid drawer item");
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(getBusObserver()));
    }

    public JustObserver<Object> getBusObserver() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof SelectDrawerItemEvent) {
                    onDrawerItemSelected((SelectDrawerItemEvent) event);
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
    }

}
