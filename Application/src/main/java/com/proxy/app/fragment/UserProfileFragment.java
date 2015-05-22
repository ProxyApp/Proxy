package com.proxy.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.User;
import com.proxy.api.rx.JustObserver;
import com.proxy.api.rx.event.ChannelAddedEvent;
import com.proxy.api.rx.event.ChannelSelectedEvent;
import com.proxy.api.rx.event.DeleteChannelEvent;
import com.proxy.app.adapter.ChannelGridRecyclerAdapter;
import com.proxy.app.dialog.EditChannelDialog;
import com.proxy.app.dialog.ErrorDialog;
import com.proxy.widget.BaseRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.proxy.Constants.ARG_USER_CREATED_CHANNEL;
import static com.proxy.Constants.ARG_USER_LOGGED_IN;
import static com.proxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.proxy.api.rx.RxChannelSync.addChannel;
import static com.proxy.api.rx.RxChannelSync.deleteChannel;
import static com.proxy.app.adapter.BaseViewHolder.ItemClickListener;
import static rx.android.app.AppObservable.bindFragment;

/**
 * Display a User or Contacts Profile.
 */
public class UserProfileFragment extends BaseFragment implements ItemClickListener {

    public static final int SPAN_COUNT = 4;
    @InjectView(R.id.common_recyclerview)
    protected BaseRecyclerView recyclerView;
    private ChannelGridRecyclerAdapter _adapter;
    private User _user;
    private CompositeSubscription _subscriptions;
    private Subscription _channelSyncSubscription;

    /**
     * Constructor.
     */
    public UserProfileFragment() {
    }

    /**
     * Return new {@link UserProfileFragment} instance.
     *
     * @return fragment
     */
    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _user = activity.getIntent().getExtras().getParcelable(ARG_USER_SELECTED_PROFILE);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_base_recyclerview, container, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeRecyclerView();
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        final GridLayoutManager manager = new GridLayoutManager(getActivity(), SPAN_COUNT);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return _adapter.isHeaderOrSection(position) ? manager.getSpanCount() : 1;
            }
        });
        recyclerView.setLayoutManager(manager);
        _adapter = ChannelGridRecyclerAdapter.newInstance(_user, this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public final void onItemClick(View view, int position) {
        if (!_adapter.isHeaderOrSection(position)) {
            Channel channel = _adapter.getItemData(position);
            getRxBus().post(new ChannelSelectedEvent(channel));
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        if (isLoggedInUser()) {
            EditChannelDialog.newInstance(_adapter.getItemData(position))
                .show(getFragmentManager());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (_subscriptions == null) {
            _subscriptions = new CompositeSubscription();
        }
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())
            .subscribe(onNextEvent()));
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof ChannelAddedEvent) {
                    addUserChannel(((ChannelAddedEvent) event).channel);
                } else if (event instanceof DeleteChannelEvent) {
                    deleteUserChannel(((DeleteChannelEvent) event).channel);
                }
            }
        };
    }

    private boolean isLoggedInUser() {
        return getActivity().getIntent().getExtras().getBoolean(ARG_USER_LOGGED_IN);
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Channel channel = data.getExtras().getParcelable(ARG_USER_CREATED_CHANNEL);
            if (channel != null && _subscriptions == null) {
                _subscriptions = new CompositeSubscription();
                addUserChannel(channel);
            }
        }
    }

    private void addUserChannel(Channel channel) {
        _channelSyncSubscription = bindFragment(this,
            addChannel(getActivity(), getLoggedInUser(), channel))
            .subscribe(addChannelObserver());
        _subscriptions.add(_channelSyncSubscription);
    }

    private void deleteUserChannel(Channel channel) {
        _channelSyncSubscription = bindFragment(this,
            deleteChannel(getActivity(), getLoggedInUser(), channel))
            .subscribe(deleteChannelObserver());
        _subscriptions.add(_channelSyncSubscription);
    }

    public Observer<Pair<User, Channel>> addChannelObserver() {
        return new JustObserver<Pair<User, Channel>>() {
            @Override
            public void onNext(Pair<User, Channel> userInfo) {
                setLoggedInUser(userInfo.first);
                _adapter.addChannel(userInfo.second);
                _adapter.notifyDataSetChanged();
                _subscriptions.remove(_channelSyncSubscription);
            }

            @Override
            public void onError() {
                ErrorDialog.newInstance("Data Sync Error", "Error saving the channel")
                    .show(getFragmentManager());
                _subscriptions.remove(_channelSyncSubscription);
            }
        };
    }

    public Observer<Pair<User, Channel>> deleteChannelObserver() {
        return new JustObserver<Pair<User, Channel>>() {
            @Override
            public void onNext(Pair<User, Channel> userInfo) {
                setLoggedInUser(userInfo.first);
                _adapter.removeChannel(userInfo.second);
                _adapter.notifyDataSetChanged();
                _subscriptions.remove(_channelSyncSubscription);
            }

            @Override
            public void onError() {
                ErrorDialog.newInstance("Data Sync Error", "Error saving the channel")
                    .show(getFragmentManager());
                _subscriptions.remove(_channelSyncSubscription);
            }
        };
    }
}
