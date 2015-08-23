package com.shareyourproxy.app.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.Constants;
import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.AddUserGroupCommand;
import com.shareyourproxy.api.rx.command.SyncAllUsersCommand;
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupAddedEventCallback;
import com.shareyourproxy.api.rx.event.SyncAllUsersErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllUsersSuccessEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.app.adapter.GroupAdapter;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.util.ViewUtils.getLargeIconDimen;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * {@link Fragment} that handles displaying a list of {@link Group}s in a {@link RecyclerView}.
 */
public class MainGroupFragment
    extends BaseFragment implements ItemClickListener {
    @Bind(R.id.fragment_group_main_coordinator)
    protected CoordinatorLayout coordinatorLayout;
    @Bind(R.id.fragment_group_main_recyclerview)
    protected BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_group_main_fab_group)
    protected FloatingActionButton floatingActionButton;
    @Bind(R.id.fragment_group_main_swipe_refresh)
    protected SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.fragment_group_main_empty_textview)
    protected TextView emptyTextView;
    @BindColor(R.color.common_blue)
    protected int _blue;
    SwipeRefreshLayout.OnRefreshListener _refreshListener = new SwipeRefreshLayout
        .OnRefreshListener() {
        @Override
        public void onRefresh() {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    getRxBus().post(new SyncAllUsersCommand(getLoggedInUser().id().value()));
                }
            });
        }
    };
    private GroupAdapter _adapter;
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
     * Prompt user with a {@link GroupEditChannelFragment} to add a new {@link Group}.
     */
    @OnClick(R.id.fragment_group_main_fab_group)
    public void onClick() {
        IntentLauncher.launchGroupEditChannelActivity(getActivity(), Group.createBlank());
    }

    public void checkGroupDeleted(Activity activity) {
        Boolean groupDeleted = activity.getIntent().getExtras().getBoolean(Constants
            .ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED, false);
        if (groupDeleted) {
            showSnackBar((Group) activity.getIntent().getExtras().getParcelable(
                Constants.ARG_MAINGROUPFRAGMENT_DELETED_GROUP));
        }
    }

    private void showSnackBar(final Group group) {
        Snackbar snackbar = Snackbar.make(floatingActionButton, getString(R.string.undo_delete),
            Snackbar.LENGTH_LONG);
        snackbar.setAction(getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRxBus().post(new AddUserGroupCommand(getLoggedInUser(), group));
            }
        });
        snackbar.setActionTextColor(_blue);
        snackbar.show();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_display_group, container, false);
        ButterKnife.bind(this, rootView);
        initializeSVG();
        initializeRecyclerView();
        initializeSwipeRefresh();
        checkGroupDeleted(getActivity());
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
        _subscriptions.add(getRxBus().toObserverable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserGroupAddedEventCallback) {
                        groupAdded((UserGroupAddedEventCallback) event);
                    } else if (event instanceof LoggedInUserUpdatedEventCallback) {
                        updateGroups(((LoggedInUserUpdatedEventCallback) event).user.groups());
                    } else if (event instanceof GroupChannelsUpdatedEventCallback) {
                        groupAdded((GroupChannelsUpdatedEventCallback) event);
                    } else if (event instanceof SyncAllUsersCommand) {
                        swipeRefreshLayout.setRefreshing(true);
                    } else if (event instanceof SyncAllUsersSuccessEvent) {
                        swipeRefreshLayout.setRefreshing(false);
                    } else if (event instanceof SyncAllUsersErrorEvent) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }));
        User user = getLoggedInUser();
        if (user != null && user.groups().size() > 0) {
            _adapter.updateGroupData(user.groups());
        }
    }

    public void updateGroups(HashMap<String, Group> groups) {
        _adapter.refreshGroupData(groups);
    }

    private void groupAdded(GroupChannelsUpdatedEventCallback event) {
        _adapter.addGroupData(event.group);
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        //if we're refreshing data, get rid of the UI
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Initialize this fragments {@link Group} data and {@link RecyclerView}.
     */
    private void initializeRecyclerView() {
        recyclerView.setEmptyView(emptyTextView);
        recyclerView.setSwipeRefreshLayout(swipeRefreshLayout);
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
    private HashMap<String, Group> getGroupData() {
        if (getLoggedInUser() != null) {
            return getLoggedInUser().groups();
        } else {
            return null;
        }
    }

    /**
     * {@link UserGroupAddedEventCallback}
     *
     * @param event group
     */
    public void groupAdded(UserGroupAddedEventCallback event) {
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
