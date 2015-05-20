package com.proxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.api.domain.model.User;
import com.proxy.app.BaseActivity;
import com.proxy.app.adapter.BaseViewHolder;
import com.proxy.app.adapter.DrawerRecyclerAdapter;
import com.proxy.api.rx.event.DrawerItemSelectedEvent;
import com.proxy.widget.BaseRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Drawer Fragment to handle displaying a user profile with options.
 */
public class DrawerFragment extends BaseFragment implements BaseViewHolder.ItemClickListener {

    @InjectView(R.id.fragment_drawer_recyclerview)
    BaseRecyclerView mDrawerRecyclerView;
    private DrawerRecyclerAdapter mAdapter;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawer, container, false);
        ButterKnife.inject(this, view);
        initializeRecyclerView();
        return view;
    }

    /**
     * Initialize a RecyclerView with User data.
     */
    private void initializeRecyclerView() {
        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = DrawerRecyclerAdapter.newInstance(
            getCurrentUser(), getResources().getStringArray(R.array.drawer_settings), this);
        mDrawerRecyclerView.setAdapter(mAdapter);
        mDrawerRecyclerView.setHasFixedSize(true);
        mDrawerRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onItemClick(View view, int position) {
        getRxBus().post(new DrawerItemSelectedEvent(view, position, mAdapter.getSettingValue
            (position)));
    }

    /**
     * Get the current user saved in {@link com.proxy.ProxyApplication}.
     *
     * @return current {@link User}
     */
    private User getCurrentUser() {
        return ((BaseActivity) getActivity()).getLoggedInUser();
    }

}
