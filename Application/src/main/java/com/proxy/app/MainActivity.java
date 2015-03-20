package com.proxy.app;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.proxy.R;
import com.proxy.app.adapter.DrawerRecyclerAdapter;
import com.proxy.app.fragment.MainFragment;
import com.proxy.event.OttoBusDriver;
import com.proxy.util.IntentLauncher;
import com.proxy.widget.BaseRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.proxy.util.DebugUtils.getSimpleName;


/**
 * The main activity this application launches.
 */
public class MainActivity extends BaseActivity implements BaseRecyclerView.OnItemClickListener {

    //Static Fields
    private static final String TAG = getSimpleName(MainActivity.class);
    //Views
    @InjectView(R.id.common_toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.activity_main_drawer_layout)
    DrawerLayout mDrawer;
    @InjectView(R.id.activity_main_drawer_content)
    BaseRecyclerView mRecyclerView;
    private DrawerRecyclerAdapter mAdapter;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        initializeDrawer();
        initializeRecyclerView();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_container, new MainFragment()).commit();
        }
    }

    /**
     * Initialize a RecyclerView with User data.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = DrawerRecyclerAdapter.newInstance(
            getResources().getStringArray(R.array.drawer_settings));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnItemTouchListener(BaseRecyclerView.getItemClickListener(this, this));
    }

    /**
     * Initialize this activities drawer view.
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
            mDrawer.setElevation(getResources().getDimension(R.dimen.common_drawer_elevation));
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

    private String getSetting(int resID) {
        return getString(resID);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (getSetting(R.string.settings_logout)
            .equals(mAdapter.getSettingValue(position))) {
            IntentLauncher.launchLoginActivity(MainActivity.this, true);
            finish();
        }
    }
}
