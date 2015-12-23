package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.command.UpdateUserContactsCommand;
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupDeletedEventCallback;
import com.shareyourproxy.api.rx.event.ViewGroupContactsEvent;
import com.shareyourproxy.app.fragment.AggregateFeedFragment;
import com.shareyourproxy.app.fragment.EditGroupChannelsFragment;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.Constants.ARG_EDIT_GROUP_TYPE;
import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.IntentLauncher.launchEditGroupContactsActivity;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.ADD_GROUP;
import static com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.EDIT_GROUP;
import static com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.PUBLIC_GROUP;
import static com.shareyourproxy.util.ViewUtils.getMenuIconSecondary;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Add and remove newChannel permissions from a group.
 */
public class EditGroupChannelsActivity extends BaseActivity {
    @Bind(R.id.activity_toolbar)
    Toolbar toolbar;
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
                    EditGroupChannelsFragment.newInstance()).commit();
        }
    }

    private GroupEditType getAddOrEdit() {
        return (GroupEditType) getIntent().getExtras().getSerializable(ARG_EDIT_GROUP_TYPE);
    }

    private void initialize() {
        GroupEditType editType = getAddOrEdit();
        if (ADD_GROUP.equals(editType)) {
            buildToolbar(toolbar, getString(R.string.add_group),
                getMenuIconSecondary(this, R.raw.ic_clear));
        } else if (EDIT_GROUP.equals(editType)) {
            buildToolbar(toolbar, getString(R.string.edit_group),
                getMenuIconSecondary(this, R.raw.ic_clear));
        } else if (PUBLIC_GROUP.equals(editType)) {
            buildToolbar(toolbar, getString(R.string.public_group),
                getMenuIconSecondary(this, R.raw.ic_clear));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(onNextEvent(this)));
    }

    private JustObserver<Object> onNextEvent(final EditGroupChannelsActivity activity) {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof UserGroupDeletedEventCallback) {
                    userGroupDeleted((UserGroupDeletedEventCallback) event);
                } else if (event instanceof ViewGroupContactsEvent) {
                    launchEditGroupContactsActivity(activity, getSelectedGroup());
                }
            }
        };
    }

    private Group getSelectedGroup() {
        return getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
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
        updateUserContacts(event);
        launchMainActivity(this, AggregateFeedFragment.ARG_SELECT_GROUP_TAB, true, event.getGroup());
        onBackPressed();
    }

    private void updateUserContacts(UserGroupDeletedEventCallback event) {
        ArrayList<String> contacts = new ArrayList<>();
        if (event.getGroup().contacts() != null) {
            for (String contactId : event.getGroup().contacts()) {
                contacts.add(contactId);
            }
        }

        getRxBus().post(new UpdateUserContactsCommand(
            getLoggedInUser(), contacts, getLoggedInUser().groups()));
    }

    public enum GroupEditType {
        ADD_GROUP, EDIT_GROUP, PUBLIC_GROUP
    }
}
