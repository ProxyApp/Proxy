package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent;
import com.shareyourproxy.app.BaseActivity;
import com.shareyourproxy.app.adapter.DrawerAdapter;
import com.shareyourproxy.app.adapter.BaseRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;


/**
 * Drawer Fragment to handle displaying a user profile with options.
 */
public class DrawerFragment extends BaseFragment implements ItemClickListener {

    @InjectView(R.id.fragment_drawer_recyclerview)
    BaseRecyclerView drawerRecyclerView;
    private DrawerAdapter _adapter;

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
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        drawerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = DrawerAdapter.newInstance(
            getCurrentUser(), getResources().getStringArray(R.array.drawer_settings), this);
        drawerRecyclerView.setAdapter(_adapter);
        drawerRecyclerView.setHasFixedSize(true);
        drawerRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onItemClick(View view, int position) {
        getRxBus().post(new SelectDrawerItemEvent(view, position, _adapter.getSettingValue
            (position)));
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Toast.makeText(getActivity(), _adapter.getSettingValue(position), Toast.LENGTH_SHORT)
            .show();
    }

    /**
     * Get the current user saved in {@link com.shareyourproxy.ProxyApplication}.
     *
     * @return current {@link User}
     */
    private User getCurrentUser() {
        return ((BaseActivity) getActivity()).getLoggedInUser();
    }

}
