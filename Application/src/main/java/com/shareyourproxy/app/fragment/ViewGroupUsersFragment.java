package com.shareyourproxy.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.EndObserver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.app.adapter.UserRecyclerAdapter;
import com.shareyourproxy.widget.BaseRecyclerView;

import java.util.ArrayList;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.api.RestClient.getUserService;
import static com.shareyourproxy.util.DebugUtils.showBroToast;
import static com.shareyourproxy.util.ObjectUtils.capitalize;
import static rx.android.app.AppObservable.bindFragment;

/**
 * Fragment to display a groups contacts.
 */
public class ViewGroupUsersFragment extends BaseFragment implements ItemClickListener {
    @InjectView(R.id.fragment_view_group_users_toolbar)
    protected Toolbar toolbar;
    @InjectView(R.id.fragment_view_group_users_recyclerview)
    protected BaseRecyclerView recyclerView;
    @InjectView(R.id.fragment_view_group_users_swipe_refresh)
    protected SwipeRefreshLayout swipeRefreshLayout;
    private UserRecyclerAdapter _adapter;
    private Subscription _downloadUsersSubscription;
    private CompositeSubscription _subscriptions;
    SwipeRefreshLayout.OnRefreshListener _refreshListener = new SwipeRefreshLayout
        .OnRefreshListener() {
        @Override
        public void onRefresh() {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    _downloadUsersSubscription = bindFragment(ViewGroupUsersFragment.this,
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
    public ViewGroupUsersFragment() {
    }

    /**
     * Return new Fragment instance.
     *
     * @return fragment
     */
    public static ViewGroupUsersFragment newInstance() {
        return new ViewGroupUsersFragment();
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
            }
        };
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_group_users, container, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
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

    /**
     * User selected is this Fragments underlying recyclerView.Adapter.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user);
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    private Group getGroupArg() {
        return (Group) getActivity().getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeRecyclerView();
        initializeSwipeRefresh();
        initializeToolbar();
    }

    private void initializeToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(capitalize(getGroupArg().label()));
    }


    /**
     * Initialize the color sequence of the swipe refresh view.
     */
    private void initializeSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(_refreshListener);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.black, android.R.color
            .holo_orange_dark, R.color.common_green);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = UserRecyclerAdapter.newInstance(this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
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
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
