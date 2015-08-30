package com.shareyourproxy.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.app.fragment.AddChannelListFragment;
import com.shareyourproxy.app.fragment.BaseFragment;
import com.shareyourproxy.app.fragment.UserProfileFragment;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.util.ViewUtils.getMenuIcon;

/**
 * Activity that displays a list of Channels for a user to add to their {@link
 * UserProfileFragment}.
 */
public class AddChannelListActivity extends BaseActivity {

    @Bind(R.id.activity_toolbar)
    Toolbar toolbar;
    private CompositeSubscription _subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_activity_fragment_container);
        ButterKnife.bind(this);
        initialize();
        if (savedInstanceState == null) {
            BaseFragment fragment = AddChannelListFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_fragment_container, fragment).commit();
        }
    }

    /**
     * Initialize this view.
     */
    private void initialize() {
        buildToolbar(toolbar, getString(R.string.add_channel), getMenuIcon(this, R.raw.ic_clear));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                Timber.e("Option item selected is unknown");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeSubscriptions();
    }

    /**
     * Create a composite subscription field to handle unsubscribing in onPause.
     */
    public void initializeSubscriptions() {
        if (_subscriptions == null) {
            _subscriptions = new CompositeSubscription();
        }
        _subscriptions.add(getRxBus().toObserverable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof AddUserChannelCommand) {
                        finishActivity();
                    }
                }
            }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    /**
     * Finish activity and animate.
     */
    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initializeSubscriptions();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }
}
