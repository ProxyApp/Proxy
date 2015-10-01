package com.shareyourproxy.app.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent;
import com.shareyourproxy.app.MainActivity;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener;
import com.shareyourproxy.app.adapter.DrawerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;


/**
 * Drawer Fragment to handle displaying a user profile with options.
 */
public class DrawerFragment extends BaseFragment implements ItemLongClickListener {

    @Bind(R.id.fragment_drawer_recyclerview)
    BaseRecyclerView drawerRecyclerView;
    private DrawerAdapter _adapter;
    private CompositeSubscription _subscriptions;

    /**
     * Constructor.
     */
    public DrawerFragment() {
    }

    /**
     * Create a new instance of this fragment for parent {@link MainActivity}.
     *
     * @return drawer fragment
     */
    public static DrawerFragment newInstance() {
        return new DrawerFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawer, container, false);
        ButterKnife.bind(this, view);
        initializeRecyclerView(getResources());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof LoggedInUserUpdatedEventCallback) {
                        _adapter.updateUser(((LoggedInUserUpdatedEventCallback) event).user);
                    }
                }
            }));
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
    private void initializeRecyclerView(Resources res) {
        int icons[] =
            new int[]{ R.raw.ic_account_circle, R.raw.ic_email, R.raw.ic_info, R.raw.ic_explore,
                R.raw.ic_eject };
        String[] strings = res.getStringArray(R.array.drawer_settings);

        _adapter = DrawerAdapter.newInstance(getLoggedInUser(), strings, icons, this);
        drawerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
        String value = _adapter.getSettingValue(position);
        if (!value.equals(DrawerAdapter.HEADER)) {
            Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT)
                .show();
        }
    }
}
