package com.shareyourproxy.app;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.rx.command.GetAllUsersCommand;
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent;
import com.shareyourproxy.app.adapter.DrawerAdapter;
import com.shareyourproxy.app.fragment.DrawerFragment;
import com.shareyourproxy.app.fragment.MainFragment;

import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.util.ViewUtils.getMenuIcon;
import static rx.android.app.AppObservable.bindActivity;


/**
 * The {@link MainActivity} filled with {@link Contact}s.
 */
public class MainActivity extends BaseActivity implements ConnectionCallbacks,
    OnConnectionFailedListener {
    private GoogleApiClient _googleApiClient;
    private CompositeSubscription _subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        _googleApiClient = buildGoogleApiClient();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_fragment_container, new MainFragment())
                .replace(R.id.activity_main_drawer_fragment_container, new DrawerFragment())
                .commit();
        }

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
     * {@link SelectDrawerItemEvent}.
     *
     * @param event data
     */
    public void onDrawerItemSelected(SelectDrawerItemEvent event) {
        //if the user presses logout
        if (DrawerAdapter.isHeader(event.position)) {
            IntentLauncher.launchUserProfileActivity(this, getLoggedInUser());
        } else if (getString(R.string.settings_logout)
            .equals(event.message)) {
            // and the google api is connected
            if (_googleApiClient.isConnected()) {
                setLoggedInUser(null);
                Plus.AccountApi.clearDefaultAccount(_googleApiClient);
                IntentLauncher.launchLoginActivity(this, true);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Not Connected To Google Service, Try Again"
                    , Toast.LENGTH_SHORT).show();
                _googleApiClient.connect();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        _googleApiClient.connect();
        getRxBus().post(new GetAllUsersCommand());
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindActivity(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof SelectDrawerItemEvent) {
                        onDrawerItemSelected((SelectDrawerItemEvent) event);
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
    protected void onStop() {
        super.onStop();
        if (_googleApiClient.isConnected()) {
            _googleApiClient.disconnect();
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
        MenuItem search = menu.findItem(R.id.menu_main_search);
        // Add Icons to the menu items before they are displayed
        search.setIcon(getMenuIcon(this, R.raw.ic_search));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        _googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        _googleApiClient.connect();
    }
}


