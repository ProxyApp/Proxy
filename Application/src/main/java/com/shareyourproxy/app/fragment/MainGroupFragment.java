package com.shareyourproxy.app.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.event.GroupAddedEvent;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.app.adapter.GroupRecyclerAdapter;
import com.shareyourproxy.app.dialog.AddGroupDialog;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

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
    @InjectView(R.id.fragment_group_display_recyclerview)
    protected RecyclerView recyclerView;
    @InjectView(R.id.fragment_group_display_add_item)
    protected FloatingActionButton floatingActionButton;
    private GroupRecyclerAdapter _adapter;
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
    @OnClick(R.id.fragment_group_display_add_item)
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

    /**
     * Initialize this fragments {@link Group} data and {@link RecyclerView}.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = GroupRecyclerAdapter.newInstance(getGroupData(), this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Get Group Data from logged in user.
     *
     * @return Group List
     */
    private ArrayList<Group> getGroupData() {
        if (getLoggedInUser() == null) {
            return new ArrayList<>();
        }
        ArrayList<Group> serverGroups = getLoggedInUser().groups();
        if (serverGroups == null || serverGroups.size() == 0) {
            return new ArrayList<>();
        } else {
            return serverGroups;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof GroupAddedEvent) {
                        groupAdded((GroupAddedEvent) event);
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
     * {@link GroupAddedEvent}.
     *
     * @param event group
     */
    public void groupAdded(GroupAddedEvent event) {
        _adapter.addGroupData(event.group);
        _adapter.notifyItemInserted(_adapter.getItemCount());
        recyclerView.smoothScrollToPosition(_adapter.getItemCount());
        //update groups in firebase
        User loggedInUser = UserFactory.addUserGroups(getLoggedInUser(),
            _adapter.getDataArray());
        setLoggedInUser(loggedInUser);
        RestClient.getGroupService(getActivity())
            .addUserGroup(loggedInUser.id().value(), event.group.id().value(), event.group)
            .subscribe(new JustObserver<Group>() {
                @Override
                public void onError() {

                }

                @Override
                public void onNext(Group group) {
                    Timber.i("added group: " + group.toString());

                }
            });
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
