package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.rx.command.UpdateUserContactsCommand;
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupDeletedEventCallback;
import com.shareyourproxy.app.fragment.GroupEditChannelFragment;
import com.shareyourproxy.app.fragment.MainFragment;

import java.util.ArrayList;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.Constants.ARG_ADD_OR_EDIT;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.util.ViewUtils.getMenuIconSecondary;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Add and remove newChannel permissions from a group.
 */
public class GroupEditChannelActivity extends BaseActivity {
    public static final int ADD_GROUP = 0;
    public static final int EDIT_GROUP = 1;
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

    private int getAddOrEdit() {
        return getIntent().getExtras().getInt(ARG_ADD_OR_EDIT, 0);
    }

    private void initialize() {
        if (getAddOrEdit() == ADD_GROUP){
            buildToolbar(toolbar, getString(R.string.add_group),
                getMenuIconSecondary(this, R.raw.ic_clear));
        }
        if(getAddOrEdit() == EDIT_GROUP){
            buildToolbar(toolbar, getString(R.string.edit_group),
                getMenuIconSecondary(this, R.raw.ic_clear));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObserverable()
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

    private void userGroupDeleted(UserGroupDeletedEventCallback event) {
        ArrayList<String> contacts = new ArrayList<>();
        if (event.group.contacts() != null) {
            for (Map.Entry<String, Id> contactId : event.group.contacts().entrySet()) {
                contacts.add(contactId.getKey());
            }
        }

        getRxBus().post(new UpdateUserContactsCommand(
            getLoggedInUser(), contacts, getLoggedInUser().groups()));
        launchMainActivity(this, MainFragment.ARG_SELECT_GROUP_TAB, true, event.group);
        onBackPressed();
    }

}
