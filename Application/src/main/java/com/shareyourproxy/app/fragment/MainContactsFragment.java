package com.shareyourproxy.app.fragment;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.SyncAllUsersCommand;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.ContactAdapter;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
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
    @Bind(R.id.fragment_contact_main_recyclerview)
    protected BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_contact_main_swipe_refresh)
    protected SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.fragment_contact_main_empty_textview)
    protected TextView emptyTextView;
    OnRefreshListener _refreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    getRxBus().post(new SyncAllUsersCommand(getLoggedInUser().id().value()));
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };
    private ContactAdapter _adapter;
    private CompositeSubscription _subscriptions;

    /**
     * Constructor.
     */
    public MainContactsFragment() {
    }

    /**
     * Create a new layouts.fragment with favorite contacts.
     *
     * @return user layouts.fragment
     */
    public static MainContactsFragment newInstance() {
        return new MainContactsFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_recyclerview, container, false);
        ButterKnife.bind(this, rootView);
        initializeRecyclerView();
        initializeSwipeRefresh();
        return rootView;
    }

    /**
     * Initialize the color sequence of the swipe refresh view.
     */
    private void initializeSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(_refreshListener);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.common_text, R.color.common_blue, R.color.common_green);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        HashMap<String, Contact> contacts = null;
        if (getLoggedInUser() != null) {
            contacts = getLoggedInUser().contacts();
        }
        recyclerView.setEmptyView(emptyTextView);
        recyclerView.setSwipeRefreshLayout(swipeRefreshLayout);
        _adapter = ContactAdapter.newInstance(contacts, this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserSelectedEvent) {
                        onUserSelected((UserSelectedEvent) event);
                    } else if (event instanceof LoggedInUserUpdatedEventCallback) {
                        userUpdated((LoggedInUserUpdatedEventCallback) event);
                    }
                }
            }));
        User loggedInUser = getLoggedInUser();
        if (getLoggedInUser() != null) {
            _adapter.updateContactsList(loggedInUser.contacts());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
        swipeRefreshLayout.setRefreshing(false);
    }

    private void userUpdated(LoggedInUserUpdatedEventCallback event) {
        _adapter.updateContactsList(event.user.contacts());
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
        launchUserProfileActivity(getActivity(), event.user, getLoggedInUser().id().value());
    }
}
