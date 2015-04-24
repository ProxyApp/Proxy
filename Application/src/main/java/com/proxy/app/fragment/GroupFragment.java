package com.proxy.app.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.proxy.R;
import com.proxy.api.RestClient;
import com.proxy.api.model.Group;
import com.proxy.api.model.User;
import com.proxy.app.BaseActivity;
import com.proxy.app.adapter.GroupRecyclerAdapter;
import com.proxy.app.dialog.AddGroupDialog;
import com.proxy.event.GroupAddedEvent;
import com.proxy.event.OttoBusDriver;
import com.proxy.widget.FloatingActionButton;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import io.realm.RealmList;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.proxy.util.DebugUtils.getSimpleName;
import static com.proxy.util.ViewUtils.floatingActionButtonElevation;
import static com.proxy.util.ViewUtils.getLargeIconDimen;
import static com.proxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * {@link Fragment} that handles displaying a list of {@link Group}s in a {@link RecyclerView}.
 */
public class GroupFragment extends BaseFragment {
    private static final String TAG = getSimpleName(GroupFragment.class);
    @InjectView(R.id.fragment_group_recyclerview)
    protected RecyclerView mRecyclerView;
    @InjectView(R.id.fragment_group_add_item)
    protected FloatingActionButton mActionButton;
    @InjectView(R.id.fragment_group_add_item_image)
    protected ImageView mActionButtonImage;
    Callback<User> userCallBack = new Callback<User>() {
        @Override
        public void success(User user, Response response) {
            ((BaseActivity) getActivity()).setCurrentUser(user);
        }

        @Override
        public void failure(RetrofitError error) {

        }
    };
    private GroupRecyclerAdapter mAdapter;

    /**
     * {@link Fragment} Constructor.
     */
    public GroupFragment() {
    }

    /**
     * Get a new Instance of this {@link GroupFragment}.
     *
     * @return {@link GroupFragment}
     */
    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    /**
     * Prompt user with a {@link AddGroupDialog} to add a new {@link Group}.
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
        ViewCompat.setElevation(mActionButton, floatingActionButtonElevation(getActivity()));

        Drawable drawable = svgToBitmapDrawable(getActivity(), R.raw.add,
            getLargeIconDimen(getActivity()), Color.WHITE);
        mActionButtonImage.setImageDrawable(drawable);
    }

    /**
     * Initialize this fragments {@link Group} data and {@link RecyclerView}.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = GroupRecyclerAdapter.newInstance(getGroupData());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Get Group Data from logged in user.
     *
     * @return Group List
     */
    private RealmList<Group> getGroupData() {
        RealmList<Group> serverGroups = getLoggedInUser().getGroups();
        if (serverGroups == null) {
            return new RealmList<>();
        } else {
            return serverGroups;
        }
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
        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
        //update groups in firebase
        User loggedInUser = getLoggedInUser();
        loggedInUser.setGroups(mAdapter.getDataArray());
        RestClient.newInstance(getActivity()).getUserService().updateUser(loggedInUser.getUserId(),
            loggedInUser, userCallBack);
    }

    /**
     * Get the logged in user.
     *
     * @return Logged in user
     */
    private User getLoggedInUser() {
        return ((BaseActivity) getActivity()).getCurrentUser();
    }

}
