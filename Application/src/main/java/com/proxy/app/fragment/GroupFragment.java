package com.proxy.app.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.proxy.R;
import com.proxy.app.adapter.GroupRecyclerAdapter;
import com.proxy.app.dialog.AddGroupDialog;
import com.proxy.event.GroupAddedEvent;
import com.proxy.event.OttoBusDriver;
import com.proxy.model.Group;
import com.proxy.widget.FloatingActionButton;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hugo.weaving.DebugLog;

import static com.proxy.Constants.ARG_GROUP_LIST;
import static com.proxy.util.DebugUtils.getDebugTAG;
import static com.proxy.util.ViewUtils.dpToPx;
import static com.proxy.util.ViewUtils.getLargeIconDimen;
import static com.proxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Fragment that handles displaying a group list.
 */
public class GroupFragment extends BaseFragment {
    private static final String TAG = getDebugTAG(GroupFragment.class);
    @InjectView(R.id.fragment_group_recyclerview)
    protected RecyclerView mRecyclerView;
    @InjectView(R.id.fragment_group_add_item)
    protected FloatingActionButton mActionButton;
    @InjectView(R.id.fragment_group_add_item_image)
    protected ImageView mActionButtonImage;
    private GroupRecyclerAdapter mAdapter;

    /**
     * Constructor.
     */
    public GroupFragment() {
    }

    /**
     * Get a new Instance of this {@link GroupFragment}.
     * @return {@link GroupFragment}
     */
    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    /**
     * Add a new user on click.
     */
    @OnClick(R.id.fragment_group_add_item)
    public void onClick() {
        AddGroupDialog.newInstance().show(getFragmentManager(), TAG);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        OttoBusDriver.register(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        OttoBusDriver.unregister(this);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
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
     * Set the content image of this {@link GroupFragment#mActionButtonImage}
     */
    @SuppressWarnings("NewApi")
    private void initializeSVG() {
        ViewCompat.setLayerType(mActionButtonImage, ViewCompat.LAYER_TYPE_SOFTWARE, null);

        Drawable drawable = svgToBitmapDrawable(getActivity(), R.raw.add,
            getLargeIconDimen(getActivity()), Color.WHITE);
        mActionButtonImage.setImageDrawable(drawable);

        ViewCompat.setElevation(mActionButton, getElevation());
    }

    /**
     * Get a common {@link FloatingActionButton} elevation resource.
     * @return elevation dimension
     */
    private float getElevation() {
        return dpToPx(getActivity(), getResources().getDimension(R.dimen.common_fab_elevation));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<Group> arrayList = savedInstanceState.getParcelableArrayList(ARG_GROUP_LIST);
            mAdapter.setDataArray(arrayList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARG_GROUP_LIST, mAdapter.getDataArray());
    }

    /**
     * Initialize this RecyclerView.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = GroupRecyclerAdapter.newInstance();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * {@link GroupAddedEvent}.
     *
     * @param event group
     */
    @Subscribe
    @DebugLog
    @SuppressWarnings("unused")
    public void groupAdded(GroupAddedEvent event) {
        mAdapter.addGroupData(event.group);
        mAdapter.notifyItemInserted(mAdapter.getItemCount());
    }
}
