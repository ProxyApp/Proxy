package com.shareyourproxy.app;

import android.os.Bundle;
import android.view.View;

import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent;
import com.shareyourproxy.app.fragment.DispatchFragment;

import rx.subscriptions.CompositeSubscription;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static com.shareyourproxy.IntentLauncher.launchLoginActivity;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.app.fragment.AggregateFeedFragment.ARG_SELECT_PROFILE_TAB;

/**
 * Activity to check if we have a cached user in SharedPreferences. Send the user to the {@link AggregateFeedActivity} if we have a cached user or send them to
 * {@link LoginActivity} if we need to login to google services and download a current user. Delete cached Realm data on startup. Fullscreen activity.
 */
public class DispatchActivity extends GoogleApiActivity {
    private final RxHelper _rxHelper = RxHelper.INSTANCE;
    private CompositeSubscription _subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content,
                    DispatchFragment.newInstance()).commit();
        }
        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = _rxHelper.checkCompositeButton(_subscriptions);
        _subscriptions.add(getRxBus().toObservable().subscribe(getRxBusObserver()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    /**
     * Delete any saved realm file and go full screen for the loading UI.
     */
    private void initialize() {
        deleteRealm();
        goFullScreen();
    }

    /**
     * Hide Navigation bar and go full screen.
     */
    private void goFullScreen() {
        View decorView = getWindow().getDecorView();
        int uiOptions = SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public JustObserver<Object> getRxBusObserver() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof SyncAllContactsSuccessEvent) {
                    goToUserFeedActivity();
                } else if (event instanceof SyncAllContactsErrorEvent) {
                    goToLoginActivity();
                }
            }
        };
    }

    /**
     * Go to the main user feed activity and finish this one.
     */
    private void goToUserFeedActivity() {
        launchMainActivity(this, ARG_SELECT_PROFILE_TAB, false, null);
        finish();
    }

    /**
     * Launch the login activity and finish this dispatch activity.
     */
    public void goToLoginActivity() {
        launchLoginActivity(this);
        finish();
    }
}
