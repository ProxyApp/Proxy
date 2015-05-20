package com.proxy.app.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.IntentLauncher;
import com.proxy.R;
import com.proxy.api.RestClient;
import com.proxy.api.domain.model.User;
import com.proxy.api.rx.RxModelUpload;
import com.proxy.api.rx.event.UserSelectedEvent;
import com.proxy.api.service.UserService;
import com.proxy.app.adapter.BaseViewHolder;
import com.proxy.app.adapter.UserRecyclerAdapter;
import com.proxy.widget.BaseRecyclerView;

import java.util.ArrayList;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.realm.Realm;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.proxy.api.domain.factory.UserFactory.createRealmUser;
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
    private CompositeSubscription mSubscriptions;
    private Realm mRealm;
    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout
        .OnRefreshListener() {
        @Override
        public void onRefresh() {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    initializeUserData(getActivity());
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };

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
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_recyclerview, container, false);
        ButterKnife.inject(this, rootView);
        mRealm = Realm.getInstance(getActivity());
        initializeRecyclerView();
        initializeUserData(getActivity());
        initializeSwipeRefresh();
        return rootView;
    }

    /**
     * Get the {@link User} data.
     */
    private void initializeUserData(Activity activity) {
        UserService userService = RestClient.getUserService(activity);
        userService.listUsers().compose(RxModelUpload.<Map<String, User>>applySchedulers())
            .subscribe(new Action1<Map<String, User>>() {
                @Override
                public void call(Map<String, User> userMap) {
                    mAdapter.setUsers(new ArrayList<>(userMap.values()));
                    for (Map.Entry<String, User> entry : userMap.entrySet()) {
                        transactRealmObject(mRealm, createRealmUser(entry.getValue()));
                    }
                }
            });
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
        mAdapter = UserRecyclerAdapter.newInstance(this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.RESULT_OK) {
            //TODO: update users
        }
    }
}
