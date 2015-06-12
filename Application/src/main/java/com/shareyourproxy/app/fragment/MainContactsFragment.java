package com.shareyourproxy.app.fragment;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.api.rx.command.callback.UsersDownloadedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.ContactAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.DebugUtils.showBroToast;
import static rx.android.app.AppObservable.bindFragment;

/**
 * A recyclerView of Favorite {@link User}s.
 */
public class MainContactsFragment extends BaseFragment implements ItemClickListener {
    @InjectView(R.id.fragment_contact_main_recyclerview)
    protected BaseRecyclerView recyclerView;
    @InjectView(R.id.fragment_contact_main_swipe_refresh)
    protected SwipeRefreshLayout swipeRefreshLayout;
    private ContactAdapter _adapter;
    OnRefreshListener _refreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    _adapter.refreshContactList(getLoggedInUser().contacts());
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };
    private CompositeSubscription _subscriptions;

    /**
     * Constructor.
     */
    public MainContactsFragment() {
    }

    /**
     * Create a new fragment with favorite contacts.
     *
     * @return user fragment
     */
    public static MainContactsFragment newInstance() {
        return new MainContactsFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_recyclerview, container, false);
        ButterKnife.inject(this, rootView);
        initializeRecyclerView();
        initializeSwipeRefresh();
        return rootView;
    }

    /**
     * Initialize the color sequence of the swipe refresh view.
     */
    private void initializeSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(_refreshListener);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.black, android.R.color
            .holo_orange_dark, R.color.common_green);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = ContactAdapter.newInstance(getLoggedInUser().contacts(), this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserSelectedEvent) {
                        onUserSelected((UserSelectedEvent) event);
                    }
                    else if (event instanceof UsersDownloadedEvent) {
                        usersDownloaded((UsersDownloadedEvent) event);
                    }
                }
            }));
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    private void usersDownloaded(UsersDownloadedEvent event) {
        _adapter.refreshContactList(getLoggedInUser().contacts());
    }

    @Override
    public void onItemClick(View view, int position) {
        getRxBus().post(new UserSelectedEvent(
            UserFactory.createModelUser(_adapter.getItemData(position))));
    }

    @Override
    public void onItemLongClick(View view, int position) {
        showBroToast(getActivity(), _adapter.getItemData(position).last());
    }

    /**
     * User selected is this Fragments underlying recyclerView.Adapter.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user);
    }
}
