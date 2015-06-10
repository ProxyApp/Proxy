package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.rx.command.event.UserGroupDeletedEvent;
import com.shareyourproxy.api.rx.event.GroupChannelToggledEvent;
import com.shareyourproxy.app.fragment.EditGroupChannelFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.util.ViewUtils.getMenuIcon;
import static rx.android.app.AppObservable.bindActivity;

public class EditGroupChannelActivity extends BaseActivity {

    @InjectView(R.id.include_toolbar)
    protected Toolbar toolbar;
    private CompositeSubscription _subscriptions;

    //note this may make sense to factor out into the base activity
    @Override
    public void onBackPressed() {
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
                    EditGroupChannelFragment.newInstance()).commit();
        }
    }

    private void initialize() {
        //we'll need a set of user channels
        //and a group
        buildToolbar(toolbar, getString(R.string.edit_group), getMenuIcon(this, R.raw.ic_clear));
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
        search.setIcon(getMenuIcon(this, R.raw.ic_done));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_edit_group_channel_save:
                saveGroupData();
                break;
            default:
                Timber.e("Option item selected is unknown");
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveGroupData() {
        IntentLauncher.launchMainActivity(this);
        onBackPressed();
    }

    private void userGroupDeleted(UserGroupDeletedEvent event) {
        IntentLauncher.launchMainActivity(this);
        onBackPressed();
    }

    private Group getSelectedGroup() {
        return getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
    }

    private void toggleChannelInGroup(GroupChannelToggledEvent toggleChannel) {
    }

}
