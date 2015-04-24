package com.proxy.app.fragment;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.IntentLauncher;
import com.proxy.R;
import com.proxy.api.domain.model.User;
import com.proxy.app.adapter.BaseViewHolder;
import com.proxy.app.adapter.UserRecyclerAdapter;
import com.proxy.event.UserSelectedEvent;
import com.proxy.widget.BaseRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static rx.android.app.AppObservable.bindFragment;

/**
 * A RecyclerView of Favorite {@link User}s.
 */
public class FavoriteUserFragment extends BaseFragment implements BaseViewHolder.ItemClickListener {
    @InjectView(R.id.common_recyclerview)
    BaseRecyclerView mRecyclerView;
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
    private CompositeSubscription mSubscriptions;

    /**
     * Constructor.
     */
    public FavoriteUserFragment() {
    }

    /**
     * Create a new fragment with favorite contacts.
     *
     * @return user fragment
     */
    public static FavoriteUserFragment newInstance() {
        return new FavoriteUserFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
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
     * Initialize a RecyclerView with User data.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = UserRecyclerAdapter.newInstance(getDefaultRealm(), this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscriptions = new CompositeSubscription();
        mSubscriptions.add(bindFragment(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserSelectedEvent) {
                        onUserSelected((UserSelectedEvent) event);
                    }
                }
            }));
    }

    @Override
    public void onItemClick(View view, int position) {
        getRxBus().post(new UserSelectedEvent(mAdapter.getItemData(position)));
    }

    @Override
    public void onPause() {
        super.onPause();
        mSubscriptions.unsubscribe();
    }

    /**
     * User selected is this Fragments underlying RecyclerView.Adapter.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        IntentLauncher.launchUserProfileActivity(getActivity(), event.user);
    }
}
