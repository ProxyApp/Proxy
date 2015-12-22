package com.shareyourproxy.app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Status;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxGoogleAnalytics;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.command.AddUserCommand;
import com.shareyourproxy.api.rx.command.SyncContactsCommand;
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent;
import com.shareyourproxy.app.fragment.AggregateFeedFragment;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.BuildConfig.VERSION_CODE;
import static com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION;
import static com.shareyourproxy.IntentLauncher.launchIntroductionActivity;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.api.RestClient.getHerokuUserService;
import static com.shareyourproxy.api.RestClient.getUserService;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;


/**
 * Log in with a google plus account.
 */
public class LoginActivity extends GoogleApiActivity {

    private final RxGoogleAnalytics _analytics = new RxGoogleAnalytics(this);
    private final RxHelper _rxHelper = RxHelper.INSTANCE;
    @Bind(R.id.activity_login_title)
    TextView proxyLogo;
    @Bind(R.id.activity_login_sign_in_button)
    SignInButton signInButton;
    @BindDimen(R.dimen.common_rect_tiny)
    int margin;
    @BindDimen(R.dimen.common_svg_ultra_minor)
    int svgUltraMinor;
    private CompositeSubscription _subscriptions;

    /**
     * Sign in click listener.
     */
    @OnClick(R.id.activity_login_sign_in_button)
    protected void onClickSignIn() {
        signInToGoogle();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize() {
        initializeValues();
        drawLogo();
    }

    private void initializeValues() {
        signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);
        signInButton.setEnabled(true);
    }

    /**
     * Set the Logo image.drawable on this activities {@link ImageView}.
     */
    private void drawLogo() {
        Drawable draw = svgToBitmapDrawable(this, R.raw.ic_proxy_logo_typed, svgUltraMinor);
        proxyLogo.setCompoundDrawablesWithIntrinsicBounds(null, draw, null, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable().subscribe(getRxBusObserver()));
    }

    public JustObserver<Object> getRxBusObserver() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof SyncAllContactsSuccessEvent ||
                    event instanceof SyncAllContactsErrorEvent) {
                    login();
                }
            }

            @Override
            public void error(Throwable e) {
                showErrorDialog(LoginActivity.this, getString(R.string.rx_eventbus_error));
                signInButton.setEnabled(true);
            }
        };
    }

    public void login() {
        if (getSharedPreferences().getBoolean(KEY_PLAY_INTRODUCTION, true)) {
            launchIntroductionActivity(this);
        } else {
            launchMainActivity(this, AggregateFeedFragment.ARG_SELECT_PROFILE_TAB, false, null);
        }
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
    }

    /**
     * Get Database {@link User}..\
     *
     * @param account user account
     */
    private void getUserFromFirebase(@NonNull GoogleSignInAccount account) {
        String userId = new StringBuilder(GOOGLE_UID_PREFIX).append(account.getId()).toString();
        getUserService().getUser(userId)
            .compose(_rxHelper.<User>observeMain())
            .subscribe(getUserObserver(this, account));
    }

    /**
     * This Observer eventually calls SyncAllContactsCommand which calls login.
     *
     * @param activity context
     * @param acct user account
     * @return current user
     */
    private JustObserver<User> getUserObserver(final BaseActivity activity, final GoogleSignInAccount acct) {
        return new JustObserver<User>() {
            @Override
            public void next(User user) {
                if (user == null) {
                    addUserToDatabase(createUserFromGoogle(acct));
                } else {
                    _rxHelper.updateRealmUser(activity, user);
                    setLoggedInUser(user);
                    getUserService().updateUserVersion(user.id(), VERSION_CODE)
                        .compose(_rxHelper.<String>observeMain()).subscribe();
                    getRxBus().post(new SyncContactsCommand(user));
                }
            }

            @Override
            public void error(Throwable e) {
                showErrorDialog(activity, getString(R.string.retrofit_general_error));
                signInButton.setEnabled(true);
            }
        };
    }

    /**
     * Add a {@link User} to FireBase.
     *
     * @param newUser the {@link User} to log in
     */
    private void addUserToDatabase(@NonNull User newUser) {
        setLoggedInUser(newUser);
        HashMap<String, Group> userGroups = newUser.groups();
        if (userGroups != null) {
            ArrayList<String> groupIds = new ArrayList<>(userGroups.size());
            for (Group group : userGroups.values()) {
                groupIds.add(group.id());
            }
            getHerokuUserService().putSharedLinks(groupIds, newUser.id())
                .compose(_rxHelper.observeMain()).subscribe();
        }
        getRxBus().post(new AddUserCommand(newUser));
        getRxBus().post(new SyncContactsCommand(newUser));
        _analytics.userAdded(newUser);
    }

    @Override
    public void onGooglePlusSignIn(GoogleSignInAccount acct) {
        if (acct != null) {
            getUserFromFirebase(acct);
        } else {
            showErrorDialog(this, getString(R.string.login_error_retrieving_user));
            signInButton.setEnabled(true);
        }
    }

    @Override
    public void onGooglePlusError(Status status) {
        showErrorDialog(this, status.getStatusMessage());
        signInButton.setEnabled(true);
    }
}
