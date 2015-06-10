package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.command.callback.UserGroupDeletedEvent;
import com.shareyourproxy.api.rx.event.GroupChannelToggledEvent;
import com.shareyourproxy.api.rx.event.SaveChannelsClicked;
import com.shareyourproxy.app.fragment.GroupEditChannelFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.util.ViewUtils.getMenuIconSecondary;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;
import static rx.android.app.AppObservable.bindActivity;

public class GroupEditChannelActivity extends BaseActivity {

    @InjectView(R.id.activity_toolbar)
    protected Toolbar toolbar;
    private CompositeSubscription _subscriptions;

    //note this may make sense to factor out into the base activity
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
        ButterKnife.inject(this);
        initialize();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_fragment_container,
                    GroupEditChannelFragment.newInstance()).commit();
        }
    }

    private void initialize() {
        //we'll need a set of user channels
        //and a group
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
                if (event instanceof UserGroupDeletedEvent) {
                    userGroupDeleted((UserGroupDeletedEvent) event);
                } else if (event instanceof GroupChannelToggledEvent) {
                    toggleChannelInGroup(((GroupChannelToggledEvent) event));
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
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_edit_group_channel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem search = menu.findItem(R.id.menu_edit_group_channel_save);
        // Add Icons to the menu items before they are displayed
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

    private void userGroupDeleted(UserGroupDeletedEvent event) {
        IntentLauncher.launchMainActivity(this);
        onBackPressed();
    }

    private void toggleChannelInGroup(GroupChannelToggledEvent toggleChannel) {
    }

}
