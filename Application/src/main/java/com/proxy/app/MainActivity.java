package com.proxy.app;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.proxy.IntentLauncher;
import com.proxy.R;
import com.proxy.api.RestClient;
import com.proxy.api.domain.model.Contact;
import com.proxy.api.domain.model.User;
import com.proxy.api.service.UserService;
import com.proxy.app.adapter.DrawerRecyclerAdapter;
import com.proxy.app.fragment.DrawerFragment;
import com.proxy.app.fragment.MainFragment;
import com.proxy.event.DrawerItemSelectedEvent;
import com.proxy.event.RxBusDriver;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.realm.Realm;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.proxy.api.domain.factory.UserFactory.createRealmUser;
import static com.proxy.util.ViewUtils.getMenuIcon;
import static rx.android.app.AppObservable.bindActivity;


/**
 * The {@link MainActivity} filled with {@link Contact}s.
 */
public class MainActivity extends BaseActivity implements ConnectionCallbacks,
    OnConnectionFailedListener {
    //Views
    @InjectView(R.id.common_toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.activity_main_drawer_layout)
    DrawerLayout mDrawer;
    private GoogleApiClient mGoogleApiClient;
    private RestClient mRestClient;
    private Realm mRealm;
    private RxBusDriver rxBus;
    private CompositeSubscription mSubscriptions;


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        mRealm = getDefaultRealm();
        mGoogleApiClient = buildGoogleApiClient();
        mRestClient = RestClient.newInstance(this);
        initializeDrawer();
        initializeUserData();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_fragment_container, new MainFragment())
                .replace(R.id.activity_main_drawer_fragment_container, new DrawerFragment())
                .commit();
        }

    }

    /**
     * Get the {@link User} data.
     */
    private void initializeUserData() {
        UserService userService = mRestClient.getUserService();
        userService.listUsers().subscribeOn(Schedulers.io()).
            observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Map<String, User>>() {
            @Override
            public void call(Map<String, User> userMap) {
                for (Map.Entry<String, User> entry : userMap.entrySet()) {
                    transactRealmObject(mRealm, createRealmUser(entry.getValue()));
                }
            }
        });
    }

    /**
     * When we build the GoogleApiClient we specify where connected and connection failed callbacks
     * should be returned, which Google APIs our app uses and which OAuth 2.0 scopes our app
     * requests.
     *
     * @return Api Client
     */
    private GoogleApiClient buildGoogleApiClient() {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Plus.API, Plus.PlusOptions.builder().build());
        return builder.build();
    }

    /**
     * Initialize this activity's drawer view.
     */
    private void initializeDrawer() {
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawer,
            mToolbar, R.string.common_open, R.string.common_closed) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawer.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        ViewCompat.setElevation(mDrawer, getResources().getDimension(R.dimen
            .common_drawer_elevation));
    }

    /**
     * {@link DrawerItemSelectedEvent}.
     *
     * @param event data
     */
    public void onDrawerItemSelected(DrawerItemSelectedEvent event) {
        //if the user presses logout
        if (DrawerRecyclerAdapter.isHeader(event.position)) {
            IntentLauncher.launchUserProfileActivity(this, getLoggedInUser());
        } else if (getString(R.string.settings_logout)
            .equals(event.message)) {
            // and the google api is connected
            if (mGoogleApiClient.isConnected()) {
                setLoggedInUser(null);
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                IntentLauncher.launchLoginActivity(this, true);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Not Connected To Google Service, Try Again"
                    , Toast.LENGTH_SHORT).show();
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscriptions = new CompositeSubscription();
        mSubscriptions.add(bindActivity(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof DrawerItemSelectedEvent) {
                        onDrawerItemSelected((DrawerItemSelectedEvent) event);
                    }
                }
            }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscriptions.unsubscribe();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem notification = menu.findItem(R.id.menu_main_notification);
        MenuItem search = menu.findItem(R.id.menu_main_search);
        // Add Icons to the menu items before they are displayed
        notification.setIcon(getMenuIcon(this, R.raw.notifications));
        search.setIcon(getMenuIcon(this, R.raw.search));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_notification:
                break;
            case R.id.menu_main_search:
                IntentLauncher.launchSearchActivity(this);
                break;
            default:
                Timber.e("Menu Item ID unknown");
                break;
        }
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Timber.i("Connected to G+");
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mGoogleApiClient.connect();
    }
}


