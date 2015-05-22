package com.proxy.app.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.proxy.IntentLauncher;
import com.proxy.R;
import com.proxy.api.RestClient;
import com.proxy.api.domain.factory.UserFactory;
import com.proxy.api.domain.model.Group;
import com.proxy.api.domain.model.User;
import com.proxy.api.rx.JustObserver;
import com.proxy.api.rx.event.GroupAddedEvent;
import com.proxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.proxy.app.adapter.GroupRecyclerAdapter;
import com.proxy.app.dialog.AddGroupDialog;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.proxy.util.DebugUtils.getSimpleName;
import static com.proxy.util.ViewUtils.getLargeIconDimen;
import static com.proxy.util.ViewUtils.svgToBitmapDrawable;
import static rx.android.app.AppObservable.bindFragment;

/**
 * {@link Fragment} that handles displaying a list of {@link Group}s in a {@link RecyclerView}.
 */
public class DisplayGroupFragment
    extends BaseFragment implements ItemClickListener {
    private static final String TAG = getSimpleName(DisplayGroupFragment.class);
    @InjectView(R.id.fragment_group_display_recyclerview)
    protected RecyclerView recyclerView;
    @InjectView(R.id.fragment_group_display_add_item)
    protected FloatingActionButton floatingActionButton;
    private GroupRecyclerAdapter _adapter;
    private CompositeSubscription _subscriptions;

    /**
     * {@link Fragment} Constructor.
     */
    public DisplayGroupFragment() {
    }

    /**
     * Get a new Instance of this {@link DisplayGroupFragment}.
     *
     * @return {@link DisplayGroupFragment}
     */
    public static DisplayGroupFragment newInstance() {
        return new DisplayGroupFragment();
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
        Drawable drawable = svgToBitmapDrawable(getActivity(), R.raw.add,
            getLargeIconDimen(getActivity()), Color.WHITE);
        floatingActionButton.setImageDrawable(drawable);
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
        IntentLauncher.launchEditGroupActivity(this.getActivity(), group);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
