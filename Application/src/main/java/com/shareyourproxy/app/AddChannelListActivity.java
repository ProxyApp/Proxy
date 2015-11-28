package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxGoogleAnalytics;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback;
import com.shareyourproxy.api.rx.event.AddChannelDialogSuccess;
import com.shareyourproxy.api.rx.event.ChannelAddedEvent;
import com.shareyourproxy.app.dialog.SaveGroupChannelDialog;
import com.shareyourproxy.app.fragment.AddChannelListFragment;
import com.shareyourproxy.app.fragment.BaseFragment;
import com.shareyourproxy.app.fragment.UserProfileFragment;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;
import static com.shareyourproxy.util.ViewUtils.getMenuIcon;

/**
 * Activity that displays a list of Channels for a user to add to their {@link
 * UserProfileFragment}.
 */
public class AddChannelListActivity extends BaseActivity {

    private final RxGoogleAnalytics _analytics = RxGoogleAnalytics.getInstance(this);
    @Bind(R.id.activity_toolbar)
    Toolbar toolbar;
    @BindString(R.string.add_channel)
    String addChannel;
    @BindString(R.string.add_another_channel)
    String addAnotherChannel;
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
        buildToolbar(toolbar, addChannel, getMenuIcon(this, R.raw.ic_clear));
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
        //Finish activity and animate.
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
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
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(new JustObserver<Object>() {
                @Override
                public void next(Object event) {
                    if (event instanceof UserChannelAddedEventCallback) {
                        channelAdded((UserChannelAddedEventCallback) event);
                    } else if (event instanceof AddChannelDialogSuccess) {
                        showAddGroupChannelDialog((AddChannelDialogSuccess) event);
                    } else if (event instanceof ChannelAddedEvent) {
                        ChannelAddedEvent((ChannelAddedEvent) event);
                    }
                }
            }));
    }

    public void showAddGroupChannelDialog(AddChannelDialogSuccess event) {
        SaveGroupChannelDialog.newInstance(event.channel,
            event.user).show(getSupportFragmentManager());
    }

    public void ChannelAddedEvent(ChannelAddedEvent event) {
        showSnackBar(event);
    }

    private void showSnackBar(ChannelAddedEvent event) {
        make(toolbar, getString(
            R.string.blank_added, event.channel.channelType()), LENGTH_LONG).show();
    }

    public void channelAdded(UserChannelAddedEventCallback event) {
        if (event.oldChannel == null) {
            _analytics.channelAdded(event.newChannel.channelType());
        } else {
            _analytics.channelEdited(event.oldChannel.channelType());
        }
        toolbar.setTitle(addAnotherChannel);
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }
}
