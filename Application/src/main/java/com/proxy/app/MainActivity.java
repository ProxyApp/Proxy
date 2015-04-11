package com.proxy.app;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
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
import com.proxy.ProxyApplication;
import com.proxy.R;
import com.proxy.api.model.User;
import com.proxy.app.adapter.DrawerRecyclerAdapter;
import com.proxy.app.fragment.MainFragment;
import com.proxy.event.OttoBusDriver;
import com.proxy.widget.BaseRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

import static com.proxy.util.ViewUtils.getMenuIconDimen;
import static com.proxy.util.ViewUtils.svgToBitmapDrawable;


/**
 * The main activity filled with contacts.
 */
public class MainActivity extends BaseActivity implements BaseRecyclerView
    .OnItemClickListener, ConnectionCallbacks, OnConnectionFailedListener {
    //Views
    @InjectView(R.id.common_toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.activity_main_drawer_layout)
    DrawerLayout mDrawer;
    @InjectView(R.id.activity_main_drawer_content)
    BaseRecyclerView mDrawerRecyclerView;
    private DrawerRecyclerAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        initializeDrawer();
        initializeRecyclerView();
        mGoogleApiClient = buildGoogleApiClient();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_fragment_container, new MainFragment())
                .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    /**
     * Initialize a RecyclerView with User data.
     */
    private void initializeRecyclerView() {
        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = DrawerRecyclerAdapter.newInstance(
            getCurrentUser(), getResources().getStringArray(R.array.drawer_settings));
        mDrawerRecyclerView.setAdapter(mAdapter);
        mDrawerRecyclerView.setHasFixedSize(true);
        mDrawerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mDrawerRecyclerView.addOnItemTouchListener(
            BaseRecyclerView.getItemClickListener(this, this));
    }

    /**
     * Get the current user saved in {@link com.proxy.ProxyApplication}.
     *
     * @return current {@link User}
     */
    private User getCurrentUser() {
        return ((ProxyApplication) getApplication()).getCurrentUser();
    }

    /**
     * Initialize this activity's drawer view.
     */
    @SuppressLint("NewApi")
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setElevation(mDrawer, getResources().getDimension(R.dimen
                .common_drawer_elevation));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        OttoBusDriver.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OttoBusDriver.register(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        //if the user presses logout
        if (getString(R.string.settings_logout)
            .equals(mAdapter.getSettingValue(position))) {
            // and the google api is connected
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                IntentLauncher.launchLoginActivity(MainActivity.this, true);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Not Connected To Google Service, Try Again"
                    , Toast.LENGTH_SHORT).show();
                mGoogleApiClient.connect();
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_contacts, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem notification = menu.findItem(R.id.menu_notification);
        MenuItem search = menu.findItem(R.id.menu_search);
        // Add Icons to the menu items before they are displayed
        notification.setIcon(getMenuIcon(R.raw.notifications));
        search.setIcon(getMenuIcon(R.raw.search));
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Return a new Drawable of the entered resource icon.
     *
     * @param resId icon resource id
     * @return menu icon drawable
     */
    private Drawable getMenuIcon(int resId) {
        return svgToBitmapDrawable(this, resId, getMenuIconDimen(this), Color.WHITE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_notification:
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


