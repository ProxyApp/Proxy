package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.command.CheckUserContactsCommand;
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupDeletedEventCallback;
import com.shareyourproxy.api.rx.event.SaveChannelsClicked;
import com.shareyourproxy.app.fragment.GroupEditChannelFragment;
import com.shareyourproxy.app.fragment.MainFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.util.ViewUtils.getMenuIconSecondary;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;
import static rx.android.app.AppObservable.bindActivity;

/**
 * Add and remove newChannel permissions from a group.
 */
public class GroupEditChannelActivity extends BaseActivity {
    // View
    @Bind(R.id.activity_toolbar)
    protected Toolbar toolbar;
    // Transient
    private CompositeSubscription _subscriptions;

    @Override
    public void onBackPressed() {
        hideSoftwareKeyboard(toolbar);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_activity_fragment_container);
        ButterKnife.bind(this);
        initialize();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_fragment_container,
                    GroupEditChannelFragment.newInstance()).commit();
        }
    }

    private void initialize() {
        buildToolbar(toolbar, getString(R.string.edit_group),
            getMenuIconSecondary(this, R.raw.ic_clear));
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindActivity(this, getRxBus().toObserverable())
            .subscribe(onNextEvent()));
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof UserGroupDeletedEventCallback) {
                    userGroupDeleted((UserGroupDeletedEventCallback) event);
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_edit_group_channel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem search = menu.findItem(R.id.menu_edit_group_channel_save);
        search.setIcon(getMenuIconSecondary(this, R.raw.ic_done));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_edit_group_channel_save:
                getRxBus().post(new SaveChannelsClicked());
                break;
            default:
                Timber.e("Option item selected is unknown");
        }
        return super.onOptionsItemSelected(item);
    }

    private void userGroupDeleted(UserGroupDeletedEventCallback event) {
        getRxBus().post(new CheckUserContactsCommand(
            getLoggedInUser(), event.group.contacts(), getLoggedInUser().groups()));
        IntentLauncher.launchMainActivity(this, MainFragment.ARG_SELECT_GROUP_TAB, true, event.group);
        onBackPressed();
    }

}
