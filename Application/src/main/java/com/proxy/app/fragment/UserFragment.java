package com.proxy.app.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.app.adapter.UserRecyclerAdapter;
import com.proxy.event.OttoBusDriver;
import com.proxy.event.UserAddedEvent;
import com.proxy.model.User;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;

import static com.proxy.Constants.ARG_USER_LIST;


/**
 * A RecyclerView of {@link User}s.
 */
public class UserFragment extends BaseFragment {

    @InjectView(R.id.common_recyclerview)
    RecyclerView mRecyclerView;
    private UserRecyclerAdapter mAdapter;

    public UserFragment() {
    }

    /**
     * Create a new user fragment.
     *
     * @return user fragment
     */
    public static Fragment newInstance() {
        return new UserFragment();
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

    /**
     * Build a user.
     *
     * @return Evan
     */
    public User getDefaultUser() {
        return User.builder().firstName("Evan").lastName("Denerley").email("evan@gmail.com")
            .userImageURL("http://i.imgur.com/DvpvklR.png").build();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_recyclerview, container, false);
        ButterKnife.inject(this, rootView);
        initializeRecyclerView();
        initializeMockUserData();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * Set some users
     */
    private void initializeMockUserData() {
        ArrayList<User> userArrayList = new ArrayList<>();
        userArrayList.add(getDefaultUser());
        userArrayList.add(getDefaultUser());
        userArrayList.add(getDefaultUser());
        userArrayList.add(getDefaultUser());
        userArrayList.add(getDefaultUser());
        mAdapter.setDataArray(userArrayList);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<User> arrayList = savedInstanceState.getParcelableArrayList(ARG_USER_LIST);
            mAdapter.setDataArray(arrayList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARG_USER_LIST, mAdapter.getDataArray());
    }

    /**
     * Initialize a RecyclerView with User data.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = UserRecyclerAdapter.newInstance();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * User Added Event
     *
     * @param event user data
     */
    @Subscribe
    @DebugLog
    @SuppressWarnings("unused")
    public void userAdded(UserAddedEvent event) {
        mAdapter.addUserData(event.user);
        mAdapter.notifyItemInserted(mAdapter.getItemCount());
    }

}
