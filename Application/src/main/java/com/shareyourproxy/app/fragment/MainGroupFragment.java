package com.shareyourproxy.app.fragment;

import android.app.Activity;
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
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static com.shareyourproxy.api.domain.model.Group.PUBLIC;
import static com.shareyourproxy.api.domain.model.Group.createBlank;
import static com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.ADD_GROUP;
import static com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.PUBLIC_GROUP;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Displaying a list of {@link User} {@link Group}s.
 */
public class MainGroupFragment
    extends BaseFragment implements ItemClickListener {
    @Bind(R.id.fragment_group_main_coordinator)
    CoordinatorLayout coordinatorLayout;
    @Bind(R.id.fragment_group_main_recyclerview)
    BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_group_main_fab)
    FloatingActionButton floatingActionButton;
    @Bind(R.id.fragment_group_main_swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.fragment_group_main_empty_textview)
    TextView emptyTextView;
    @BindColor(R.color.common_blue)
    int colorBlue;
    @BindColor(android.R.color.white)
    int colorWhite;
    @BindDimen(R.dimen.common_svg_large)
    int marginSVGLarge;
    SwipeRefreshLayout.OnRefreshListener _refreshListener = new SwipeRefreshLayout
        .OnRefreshListener() {
        @Override
        public void onRefresh() {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    User user = getLoggedInUser();
                    if (user != null) {
                        getRxBus().post(new SyncAllUsersCommand(getRxBus(), user.id()));
                    }
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
     * Prompt user with a {@link EditGroupChannelsFragment} to add a new {@link Group}.
     */
    @OnClick(R.id.fragment_group_main_fab)
    public void onClick() {
        IntentLauncher.launchEditGroupChannelsActivity(
            getActivity(), createBlank(), ADD_GROUP);
    }

    /**
     * Check if there was a group deleted from the {@link EditGroupChannelsFragment#onItemClick
     * (View, int)})}
     *
     * @param activity to get intent data from
     */
    public void checkGroupDeleted(Activity activity) {
        Boolean groupDeleted = activity.getIntent().getExtras().getBoolean(Constants
            .ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED, false);
        if (groupDeleted) {
            showUndoDeleteSnackBar((Group) activity.getIntent().getExtras().getParcelable(
                Constants.ARG_MAINGROUPFRAGMENT_DELETED_GROUP));
        }
    }

    /**
     * Build a {@link Snackbar} and show it. This {@link Snackbar} reverses the action of deleting a
     * {@link User} {@link Group}.
     *
     * @param group deleted
     */
    private void showUndoDeleteSnackBar(final Group group) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.undo_delete),
            Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getString(R.string.undo), onClickUndoDelete(group));
        snackbar.setActionTextColor(colorBlue);
        snackbar.show();
    }

    /**
     * Snackbar action button event logic.
     *
     * @param group to add
     * @return click listener
     */
    private View.OnClickListener onClickUndoDelete(final Group group) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRxBus().post(new AddUserGroupCommand(getRxBus(), getLoggedInUser(), group));
            }
        };
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_group, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize this fragments UI.
     */
    public void initialize() {
        initializeSVG();
        initializeRecyclerView();
        initializeSwipeRefresh();
        checkGroupDeleted(getActivity());
    }

    /**
     * Set the content image of this fragment's {@link FloatingActionButton}
     */
    private void initializeSVG() {
        Drawable drawable =
            svgToBitmapDrawable(getActivity(), R.raw.ic_add, marginSVGLarge, colorWhite);
        floatingActionButton.setImageDrawable(drawable);
        ViewCompat.setElevation(floatingActionButton, 10f);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserGroupAddedEventCallback) {
                        addGroup(((UserGroupAddedEventCallback) event).group);
                    } else if (event instanceof GroupChannelsUpdatedEventCallback) {
                        updateGroups(((GroupChannelsUpdatedEventCallback) event));
                    } else if (event instanceof LoggedInUserUpdatedEventCallback) {
                        updateGroups(((LoggedInUserUpdatedEventCallback) event).user.groups());
                    } else if (event instanceof SyncAllUsersCommand) {
                        swipeRefreshLayout.setRefreshing(true);
                    } else if (event instanceof SyncAllUsersSuccessEvent) {
                        swipeRefreshLayout.setRefreshing(false);
                    } else if (event instanceof SyncAllUsersErrorEvent) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }));
        _adapter.refreshGroupData(getLoggedInUser().groups());
    }

    /**
     * Add a new group.
     *
     * @param group to add
     */
    public void addGroup(Group group) {
        _adapter.addGroupData(group);
        showAddedGroupSnackBar();
    }

    /**
     * Add a new group.
     *
     * @param event group to add
     */
    public void updateGroups(GroupChannelsUpdatedEventCallback event) {
        _adapter.addGroupData(event.group);
        if (event.groupEditType == ADD_GROUP) {
            showAddedGroupSnackBar();
        } else {
            showChangesSavedSnackBar(coordinatorLayout);
        }
    }

    private void showAddedGroupSnackBar() {
        Snackbar.make(
            coordinatorLayout, getString(R.string.group_added), LENGTH_LONG).show();
    }

    /**
     * update all groups.
     *
     * @param groups to add
     */
    public void updateGroups(HashMap<String, Group> groups) {
        _adapter.refreshGroupData(groups);
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
        //if we're refreshing data, get rid of the UI
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Initialize this fragments {@link Group} data and {@link BaseRecyclerView}.
     */
    private void initializeRecyclerView() {
        recyclerView.hideRecyclerView(false);
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

    @Override
    public void onItemClick(View view, int position) {
        Group group = _adapter.getGroupData(position);
        if (group.id().equals(PUBLIC)) {
            IntentLauncher.launchEditGroupChannelsActivity(getActivity(), group, PUBLIC_GROUP);
        } else {
            IntentLauncher.launchEditGroupContactsActivity(getActivity(), group);
        }
    }

}
