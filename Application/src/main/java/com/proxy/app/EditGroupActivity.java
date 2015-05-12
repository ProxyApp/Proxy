package com.proxy.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.proxy.R;
import com.proxy.api.RestClient;
import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.Group;
import com.proxy.api.domain.model.User;
import com.proxy.app.fragment.ChannelListFragment;
import com.proxy.app.fragment.EditGroupFragment;
import com.proxy.event.GroupChannelToggled;
import com.proxy.event.GroupDeleted;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static rx.android.app.AppObservable.bindActivity;
import static com.proxy.Constants.ARG_SELECTED_GROUP;
import static com.proxy.Constants.ARG_USER_LOGGED_IN;
import static com.proxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.proxy.util.ViewUtils.getMenuIcon;

public class EditGroupActivity extends BaseActivity {

    private CompositeSubscription subscriptions;
    @InjectView(R.id.common_toolbar)
    protected Toolbar toolbar;

    //note this may make sense to factor out into the base activity
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(ARG_USER_SELECTED_PROFILE, getLoggedInUser());
        intent.putExtra(ARG_USER_LOGGED_IN, true);
        setResult(Activity.RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.inject(this);
        initialize();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_group_list,
                            EditGroupFragment.newInstance()).commit();
        }
    }

    private void initialize() {
        //we'll need a set of user channels
        //and a group
        buildToolbar(toolbar, getString(R.string.edit_group), getMenuIcon(this, R.raw.clear));
    }

    @Override
    public void onResume(){
        super.onResume();
        subscriptions = new CompositeSubscription();
        subscriptions.add(bindActivity(this, getRxBus().toObserverable())
            .subscribe( new Action1<Object>() {
                @Override
                public void call(Object o) {
                    if(o instanceof GroupDeleted) {
                        removeGroupFromUser(getLoggedInUser(), selectedGroup());
                       // todo do something with the new user
                    }
                    else if(o instanceof GroupChannelToggled){
                        Channel c = null;
                        for (Channel chan: getLoggedInUser().channels()){
                            if(chan.channelId().equals(((GroupChannelToggled) o).channelid)) {
                                c = chan;
                            }
                        }
                        if(c == null) {
                            return;
                        }
                        toggleChannelInGroup(c);
                    }
                }
            }
        ));
    }

    private void removeGroupFromUser(User user, Group group) {
       if(user.groups() != null) {
           for (Group g : user.groups()) {
               if (group.groupId().equals(g.groupId())) {
                   user.groups().remove(g);
                   break;
               }
           }
       }
    }

    private Group selectedGroup() {
        return getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
    }

    private void toggleChannelInGroup(Channel channel) {
        for(Channel c: selectedGroup().channels()) {
            if(channel.channelId().equals(c.channelId())) {
                selectedGroup().channels().remove(c);
                return;
            }
        }
        addChannelToGroup(selectedGroup(), channel);
    }

    private void addChannelToGroup(Group grp, Channel channel) {
        grp.channels().add(channel);
    }
}
