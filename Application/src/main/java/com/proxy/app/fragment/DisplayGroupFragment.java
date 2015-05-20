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
import com.proxy.api.domain.factory.UserFactory;
import com.proxy.api.domain.model.Group;
import com.proxy.api.RestClient;
import com.proxy.api.domain.model.User;
import com.proxy.api.rx.JustObserver;
import com.proxy.api.rx.event.GroupAddedEvent;
import com.proxy.app.adapter.BaseViewHolder;
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
    extends BaseFragment
    implements BaseViewHolder.ItemClickListener {
    private static final String TAG = getSimpleName(DisplayGroupFragment.class);
    @InjectView(R.id.fragment_group_display_recyclerview)
    protected RecyclerView mRecyclerView;
    @InjectView(R.id.fragment_group_display_add_item)
    protected FloatingActionButton mFloatingActionButton;
    private GroupRecyclerAdapter mAdapter;
    private CompositeSubscription mSubscriptions;

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
//        ViewCompat.setLayerType(mFloatingActionButton, ViewCompat.LAYER_TYPE_SOFTWARE, null);
//        ViewCompat.setElevation(mFloatingActionButton, floatingActionButtonElevation(getActivity
//            ()));
        Drawable drawable = svgToBitmapDrawable(getActivity(), R.raw.add,
            getLargeIconDimen(getActivity()), Color.WHITE);
        mFloatingActionButton.setImageDrawable(drawable);
    }

    /**
     * Initialize this fragments {@link Group} data and {@link RecyclerView}.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = GroupRecyclerAdapter.newInstance(getGroupData(), this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Get Group Data from logged in user.
     *
     * @return Group List
     */
    private ArrayList<Group> getGroupData() {
        ArrayList<Group> serverGroups = getLoggedInUser().groups();
        if (serverGroups == null) {
            return new ArrayList<>();
        } else {
            return serverGroups;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscriptions = new CompositeSubscription();
        mSubscriptions.add(bindFragment(this, getRxBus().toObserverable())//
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
        mSubscriptions.unsubscribe();
    }

    /**
     * {@link GroupAddedEvent}.
     *
     * @param event group
     */
    public void groupAdded(GroupAddedEvent event) {
        mAdapter.addGroupData(event.group);
        mAdapter.notifyItemInserted(mAdapter.getItemCount());
        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
        //update groups in firebase
        User loggedInUser = UserFactory.updateUserGroups(getLoggedInUser(),
            mAdapter.getDataArray());
        setLoggedInUser(loggedInUser);
        RestClient.getGroupService(getActivity())
            .addUserGroup(loggedInUser.id().value(), event.group.groupId(), event.group)
            .subscribe(new JustObserver<Group>() {
                @Override
                public void error() {

                }

                @Override
                public void onNext(Group group) {
                    Timber.i("added group: " + group.toString());

                }
            });
    }

    @Override
    public void onItemClick(View view, int position) {
        Group group = mAdapter.getGroupData(position);
        IntentLauncher.launchEditGroupActivity(this.getActivity(), group);
    }
}
