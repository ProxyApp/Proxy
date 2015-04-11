package com.proxy.app.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.api.RestClient;
import com.proxy.api.model.User;
import com.proxy.api.service.UserService;
import com.proxy.app.adapter.UserRecyclerAdapter;
import com.proxy.event.OttoBusDriver;
import com.proxy.event.UserAddedEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;
import rx.functions.Action1;


/**
 * A RecyclerView of {@link User}s.
 */
public class UserFragment extends BaseFragment {

    @InjectView(R.id.common_recyclerview)
    RecyclerView mRecyclerView;
    @InjectView(R.id.common_recyclerview_swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private UserRecyclerAdapter mAdapter;
    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout
        .OnRefreshListener() {

        @Override
        public void onRefresh() {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };

    /**
     * Constructor.
     */
    public UserFragment() {
    }

    /**
     * Create a new user fragment.
     *
     * @return user fragment
     */
    public static UserFragment newInstance() {
        return new UserFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        OttoBusDriver.register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        OttoBusDriver.unregister(this);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_recyclerview, container, false);
        ButterKnife.inject(this, rootView);
        initializeRecyclerView();
        initializeUserData();
        initializeSwipeRefresh();
        return rootView;
    }

    /**
     * Initialize the color sequence of the swipe refresh view.
     */
    private void initializeSwipeRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(refreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.black, android.R.color
            .holo_orange_dark, R.color.common_green);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * Get the {@link User} data.
     */
    private void initializeUserData() {
        UserService userService = RestClient.newInstance(getActivity()).getUserService();
        userService.listUsers().subscribe(new Action1<Map<String, User>>() {
            @Override
            public void call(Map<String, User> userMap) {
                for (Map.Entry<String, User> entry : userMap.entrySet()) {
                    addUserToAdapter(entry.getValue());
                }
            }
        });
    }

    /**
     * Initialize a RecyclerView with User data.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = UserRecyclerAdapter.newInstance();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * User Added Event
     *
     * @param event user data
     */
    @Subscribe
    @DebugLog
    @SuppressWarnings("unused")
    public void userAdded(UserAddedEvent event) {
        addUserToAdapter(event.user);
    }

    /**
     * Add a user to the {@link ArrayList} persisted in the {@link UserRecyclerAdapter}.
     *
     * @param user to add
     */
    private void addUserToAdapter(User user) {
        mAdapter.addUserData(user);
        mAdapter.notifyItemInserted(mAdapter.getItemCount());
    }

}
