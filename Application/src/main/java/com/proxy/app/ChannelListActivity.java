package com.proxy.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.proxy.R;
import com.proxy.api.domain.factory.UserFactory;
import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.ChannelSection;
import com.proxy.api.domain.model.ChannelType;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmChannelSection;
import com.proxy.api.domain.realm.RealmChannelType;
import com.proxy.api.rx.event.ChannelAddedEvent;
import com.proxy.api.rx.event.ChannelDialogRequestEvent;
import com.proxy.app.dialog.AddChannelDialog;
import com.proxy.app.fragment.ChannelListFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.proxy.Constants.ARG_USER_CREATED_CHANNEL;
import static com.proxy.Constants.ARG_USER_LOGGED_IN;
import static com.proxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.proxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.proxy.api.domain.factory.ChannelFactory.getModelChannelSection;
import static com.proxy.api.domain.factory.ChannelFactory.getModelChannelType;
import static com.proxy.util.ViewUtils.getMenuIcon;
import static rx.android.app.AppObservable.bindActivity;

/**
 * Created by Evan on 5/5/15.
 */
public class ChannelListActivity extends BaseActivity {

    @InjectView(R.id.common_toolbar)
    Toolbar mToolbar;

    Callback<User> userCallBack = new Callback<User>() {
        @Override
        public void success(User user, Response response) {
            Timber.i("Channel updated Successfully");
        }

        @Override
        public void failure(RetrofitError error) {
            Timber.i("Channel failed to update");
        }
    };
    private CompositeSubscription mSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);
        ButterKnife.inject(this);
        initialize();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_channel_list_container,
                    ChannelListFragment.newInstance()).commit();
        }
    }

    /**
     * Initialize this view.
     */
    private void initialize() {
        buildToolbar(mToolbar, getString(R.string.add_channel), getMenuIcon(this, R.raw.clear));
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

    //note this may make sense to factor out into the base activity
    @Override
    public void onBackPressed() {
        finishActivity(Activity.RESULT_CANCELED, null);
    }

    private void finishActivity(int activityResult, Channel channel) {
        Intent intent = new Intent();
        intent.putExtra(ARG_USER_CREATED_CHANNEL, channel);
        intent.putExtra(ARG_USER_SELECTED_PROFILE, getLoggedInUser());
        intent.putExtra(ARG_USER_LOGGED_IN, true);
        setResult(activityResult, intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
    }


    @Override
    public void onResume() {
        super.onResume();
        mSubscriptions = new CompositeSubscription();
        mSubscriptions.add(bindActivity(this, getRxBus().toObserverable())
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof ChannelDialogRequestEvent) {
                        dialogRequestEvent((ChannelDialogRequestEvent) event);
                    } else if (event instanceof ChannelAddedEvent) {
                        addChannelEvent((ChannelAddedEvent) event);
                    }
                }
            }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscriptions.unsubscribe();
    }

    public void dialogRequestEvent(ChannelDialogRequestEvent event) {
        RealmChannelType realmChannelType = event.channel.getChannelType();
        RealmChannelSection realmChannelSection = event.channel.getSection();
        ChannelType channelType = getModelChannelType(realmChannelType);
        ChannelSection channelSection = getModelChannelSection(realmChannelSection);
        AddChannelDialog.newInstance(channelType, channelSection).show(getSupportFragmentManager());
    }

    public void addChannelEvent(ChannelAddedEvent event) {
        Channel channel = createModelInstance(event.channel);
        User loggedInUser = UserFactory.updateUserChannel(getLoggedInUser(),
            channel);
        setLoggedInUser(loggedInUser);
        finishActivity(Activity.RESULT_OK, channel);
    }
}
