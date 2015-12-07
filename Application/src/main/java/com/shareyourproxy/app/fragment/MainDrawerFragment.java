package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent;
import com.shareyourproxy.app.AggregateFeedActivity;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener;
import com.shareyourproxy.app.adapter.DrawerAdapter;
import com.shareyourproxy.app.adapter.DrawerAdapter.DrawerItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;


/**
 * Drawer Fragment to handle displaying a user profile with options.
 */
public class MainDrawerFragment extends BaseFragment implements ItemLongClickListener {

    @Bind(R.id.fragment_drawer_recyclerview)
    BaseRecyclerView drawerRecyclerView;
    private DrawerAdapter _adapter;
    private CompositeSubscription _subscriptions;

    /**
     * Constructor.
     */
    public MainDrawerFragment() {
    }

    /**
     * Create a new instance of this fragment for parent {@link AggregateFeedActivity}.
     *
     * @return drawer fragment
     */
    public static MainDrawerFragment newInstance() {
        return new MainDrawerFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawer, container, false);
        ButterKnife.bind(this, view);
        initializeRecyclerView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(getBusObserver()));
    }

    public JustObserver<Object> getBusObserver() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof LoggedInUserUpdatedEventCallback) {
                    _adapter.updateUser(((LoggedInUserUpdatedEventCallback) event).user);
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    /**
     * Initialize a recyclerView with User data and menu options.
     */
    private void initializeRecyclerView() {
        _adapter = DrawerAdapter.newInstance(getLoggedInUser(), this);

        drawerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        drawerRecyclerView.setHasFixedSize(true);
        drawerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        drawerRecyclerView.setAdapter(_adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        DrawerItem drawerItem = _adapter.getSettingValue(position);
        getRxBus().post(new SelectDrawerItemEvent(
            drawerItem, view, position, getString(drawerItem.getLabelRes())));
    }

    @Override
    public void onItemLongClick(View view, int position) {
        DrawerItem item = _adapter.getSettingValue(position);
        if (!item.equals(DrawerItem.HEADER)) {
            Toast.makeText(getActivity(), getString(item.getLabelRes()), Toast.LENGTH_SHORT)
                .show();
        }
    }
}
