package com.proxy.app.fragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
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

import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.proxy.R;
import com.proxy.app.adapter.GroupRecyclerAdapter;
import com.proxy.app.dialog.AddGroupDialog;
import com.proxy.event.GroupAddedEvent;
import com.proxy.event.OttoBusDriver;
import com.proxy.model.Group;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import timber.log.Timber;

import static com.proxy.Constants.ARG_GROUP_LIST;
import static com.proxy.util.DebugUtils.getDebugTAG;
import static com.proxy.util.ViewUtils.dpToPx;

/**
 * Fragment that handles displaying a group list.
 */
public class GroupFragment extends BaseFragment {
    private static final String TAG = getDebugTAG(GroupFragment.class);
    @InjectView(R.id.fragment_group_recyclerview)
    protected RecyclerView mRecyclerView;
    @InjectView(R.id.fragment_group_add_item_image)
    protected ImageView mAddItemImage;
    private float mImageWidth;
    private GroupRecyclerAdapter mAdapter;

    /**
     * Constructor.
     */
    public GroupFragment() {
    }

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
        mImageWidth = dpToPx(activity, getResourceDimension(activity));

    }

    private float getResourceDimension(Activity activity) {
        return activity.getResources().getDimension(R.dimen.common_svg_large);
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

    private void initializeSVG() {
        ViewCompat.setLayerType(mAddItemImage, ViewCompat.LAYER_TYPE_SOFTWARE, null);
        try {
            SVG svg = SVG.getFromResource(getActivity(), R.raw.add);
            svg.setDocumentWidth(mImageWidth);
            svg.setDocumentHeight(mImageWidth);
            svg.setDocumentPreserveAspectRatio(PreserveAspectRatio.END);
            Drawable drawable = new PictureDrawable(svg.renderToPicture());
            mAddItemImage.setImageDrawable(drawable);
        } catch (SVGParseException e) {
            Timber.e(e, TAG + "initializeSVG()");
        }
        ViewCompat.setElevation(mAddItemImage, getElevation());
    }

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
