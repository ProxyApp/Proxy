package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.app.fragment.AddChannelListFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.util.ViewUtils.getMenuIcon;
import static rx.android.app.AppObservable.bindActivity;

/**
 * Activity that displays a list of ChannelTypes to choose from.
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
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_fragment_container,
                    AddChannelListFragment.newInstance()).commit();
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
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindActivity(this, getRxBus().toObserverable())
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
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
    }
}
