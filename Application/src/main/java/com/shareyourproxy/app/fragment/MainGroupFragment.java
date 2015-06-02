package com.shareyourproxy.app.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.rx.command.event.UserGroupAddedEvent;
import com.shareyourproxy.api.rx.command.event.UsersDownloadedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.app.adapter.GroupAdapter;
import com.shareyourproxy.app.dialog.AddGroupDialog;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.util.DebugUtils.getSimpleName;
import static com.shareyourproxy.util.ViewUtils.getLargeIconDimen;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;
import static rx.android.app.AppObservable.bindFragment;

/**
 * {@link Fragment} that handles displaying a list of {@link Group}s in a {@link RecyclerView}.
 */
public class MainGroupFragment
    extends BaseFragment implements ItemClickListener {
    private static final String TAG = getSimpleName(MainGroupFragment.class);
    @InjectView(R.id.fragment_group_main_recyclerview)
    protected BaseRecyclerView recyclerView;
    @InjectView(R.id.fragment_group_main_fab_group)
    protected FloatingActionButton floatingActionButton;
    @InjectView(R.id.fragment_group_main_swipe_refresh)
    protected SwipeRefreshLayout swipeRefreshLayout;
    private GroupAdapter _adapter;
    SwipeRefreshLayout.OnRefreshListener _refreshListener = new SwipeRefreshLayout
        .OnRefreshListener() {
        @Override
        public void onRefresh() {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    _adapter.refreshGroupData(getGroupData());
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };
    private CompositeSubscription _subscriptions;

    /**
     * {@link Fragment} Constructor.
     */
    public MainGroupFragment() {
    }

    /**
     * Get a new Instance of this {@link MainGroupFragment}.
     *
     * @return {@link MainGroupFragment}
     */
    public static MainGroupFragment newInstance() {
        return new MainGroupFragment();
    }

    /**
     * Prompt user with a {@link AddGroupDialog} to add a new {@link Group}.
     */
    @OnClick(R.id.fragment_group_main_fab_group)
    public void onClick() {
        AddGroupDialog.newInstance().show(getFragmentManager(), TAG);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_display_group, container, false);
        ButterKnife.inject(this, rootView);
        initializeSVG();
        initializeRecyclerView();
        initializeSwipeRefresh();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * Set the content image of this {@link FloatingActionButton}
     */
    @SuppressWarnings("NewApi")
    private void initializeSVG() {
        Drawable drawable = svgToBitmapDrawable(getActivity(), R.raw.ic_add,
            getLargeIconDimen(getActivity()), Color.WHITE);
        floatingActionButton.setImageDrawable(drawable);
        ViewCompat.setElevation(floatingActionButton, 10f);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserGroupAddedEvent) {
                        groupAdded((UserGroupAddedEvent) event);
                    }
                    if (event instanceof UsersDownloadedEvent) {
                        usersDownloaded((UsersDownloadedEvent) event);
                    }
                }
            }));
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
    }

    /**
     * Initialize this fragments {@link Group} data and {@link RecyclerView}.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = GroupAdapter.newInstance(recyclerView, getGroupData(), this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
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
     * Get Group Data from logged in user.
     *
     * @return Group List
     */
    private ArrayList<Group> getGroupData() {
        return getLoggedInUser().groups();
    }

    private void usersDownloaded(UsersDownloadedEvent event) {
        _adapter.refreshGroupData(getGroupData());
    }

    /**
     * {@link UserGroupAddedEvent}
     *
     * @param event group
     */
    public void groupAdded(UserGroupAddedEvent event) {
        _adapter.addGroupData(event.group);
    }

    @Override
    public void onItemClick(View view, int position) {
        Group group = _adapter.getGroupData(position);
        IntentLauncher.launchViewGroupUsersActivity(getActivity(), group);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
