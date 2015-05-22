package com.proxy.app.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.api.RestClient;
import com.proxy.api.domain.model.User;
import com.proxy.api.rx.EndObserver;
import com.proxy.api.rx.RxHelper;
import com.proxy.api.rx.event.UserSelectedEvent;
import com.proxy.api.service.UserService;
import com.proxy.app.adapter.UserRecyclerAdapter;
import com.proxy.widget.BaseRecyclerView;

import java.util.ArrayList;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.realm.Realm;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.proxy.IntentLauncher.launchUserProfileActivity;
import static com.proxy.api.domain.factory.RealmUserFactory.createRealmUser;
import static com.proxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.proxy.util.DebugUtils.showBroToast;
import static rx.android.app.AppObservable.bindFragment;

/**
 * A recyclerView of Favorite {@link User}s.
 */
public class FavoriteUserFragment extends BaseFragment implements ItemClickListener {
    @InjectView(R.id.common_recyclerview)
    protected BaseRecyclerView recyclerView;
    @InjectView(R.id.common_recyclerview_swipe_refresh)
    protected SwipeRefreshLayout swipeRefreshLayout;
    private UserRecyclerAdapter _adapter;
    private CompositeSubscription _subscriptions;
    private Realm _realm;
    private UserService _userService;
    private Subscription _downloadUsersSubscription;
    OnRefreshListener _refreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    _downloadUsersSubscription = bindFragment(FavoriteUserFragment.this,
                        initializeUserData(getActivity())).subscribe(getEndObserver());
                    _subscriptions.add(_downloadUsersSubscription);
                    swipeRefreshLayout.setRefreshing(false);
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
        _realm = Realm.getInstance(getActivity());
        initializeRecyclerView();
        initializeSwipeRefresh();
        return rootView;
    }

    /**
     * Get the {@link User} data.
     */
    private Observable<Map<String, User>> initializeUserData(Activity activity) {
        return getUserService(activity).listUsers().compose(RxHelper.<Map<String,
            User>>applySchedulers());
    }

    private EndObserver<Map<String, User>> getEndObserver() {
        return new EndObserver<Map<String, User>>() {
            @Override
            public void onError() {
                _subscriptions.remove(_downloadUsersSubscription);
            }

            @Override
            public void onCompleted() {
                _subscriptions.remove(_downloadUsersSubscription);
            }

            @Override
            public void onNext(Map<String, User> userMap) {
                _adapter.setUsers(new ArrayList<>(userMap.values()));
                for (Map.Entry<String, User> entry : userMap.entrySet()) {
                    transactRealmObject(_realm, createRealmUser(entry.getValue()));
                }
            }
        };
    }

    private UserService getUserService(Activity activity) {
        if (_userService == null) {
            _userService = RestClient.getUserService(activity);
        }
        return _userService;
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
        _adapter = UserRecyclerAdapter.newInstance(this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (_subscriptions == null) {
            _subscriptions = new CompositeSubscription();
        }
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserSelectedEvent) {
                        onUserSelected((UserSelectedEvent) event);
                    }
                }
            }));
        _downloadUsersSubscription = bindFragment(this, initializeUserData(getActivity()))
            .subscribe(getEndObserver());
        _subscriptions.add(_downloadUsersSubscription);
    }

    @Override
    public void onItemClick(View view, int position) {
        getRxBus().post(new UserSelectedEvent(_adapter.getItemData(position)));
    }

    @Override
    public void onItemLongClick(View view, int position) {
        showBroToast(getActivity(), _adapter.getItemData(position).last());
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    /**
     * User selected is this Fragments underlying recyclerView.Adapter.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.RESULT_OK) {
            if (_subscriptions == null) {
                _subscriptions = new CompositeSubscription();
            }
            _downloadUsersSubscription = bindFragment(this,
                initializeUserData(getActivity())).subscribe(getEndObserver());
            _subscriptions.add(_downloadUsersSubscription);
        }
    }
}
