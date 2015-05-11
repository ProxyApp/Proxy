package com.proxy.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import com.proxy.R;
import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.Group;
import com.proxy.app.fragment.ChannelListFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import static com.proxy.util.ViewUtils.getMenuIcon;
import static com.proxy.Constants.ARG_SELECTED_GROUP;
import static com.proxy.Constants.ARG_USER_LOGGED_IN;
import static com.proxy.Constants.ARG_USER_SELECTED_PROFILE;

public class EditGroupActivity extends BaseActivity {


    @InjectView(R.id.common_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.inject(this);
        initialize();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_group_list,
                            ChannelListFragment.newInstance()).commit();
        }
    }

    private void initialize() {
        //we'll need a set of user channels
        //and a group
        buildToolbar(mToolbar, getString(R.string.edit_group), getMenuIcon(this, R.raw.clear));

    }

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

//    private void addContactToGroup(Contact contact) {
//        User user =  getLoggedInUser();
//
//    }

    private Group selectedGroup() {
        return getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
    }

    private void toggleChannelInGroup(Channel channel) {
        for(Channel c: selectedGroup().channels()) {
            if(channel.channelId().equals(c.channelId())) {
              // remove the channel
                return;
            }
        }
        // add the channel
    }
}
