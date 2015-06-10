package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.app.adapter.ContactAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.util.DebugUtils.showBroToast;
import static com.shareyourproxy.util.ObjectUtils.capitalize;
import static rx.android.app.AppObservable.bindFragment;

/**
 * Fragment to display a groups contacts.
 */
public class GroupContactsFragment extends BaseFragment implements ItemClickListener {
    @InjectView(R.id.fragment_view_group_users_toolbar)
    protected Toolbar toolbar;
    @InjectView(R.id.fragment_view_group_users_recyclerview)
    protected BaseRecyclerView recyclerView;
    private ContactAdapter _adapter;
    private CompositeSubscription _subscriptions;

    /**
     * Constructor.
     */
    public GroupContactsFragment() {
    }

    /**
     * Return new Fragment instance.
     *
     * @return fragment
     */
    public static GroupContactsFragment newInstance() {
        return new GroupContactsFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_group_users, container, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserSelectedEvent) {
                        onUserSelected((UserSelectedEvent) event);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * User selected is this Fragments underlying recyclerView.Adapter.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user);
    }

    private Group getGroupArg() {
        return (Group) getActivity().getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeRecyclerView();
        initializeToolbar();
    }

    private void initializeToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(capitalize(getGroupArg().label()));
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = ContactAdapter.newInstance(getGroupArg().contacts(), this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onItemClick(View view, int position) {
        getRxBus().post(new UserSelectedEvent(
            UserFactory.createModelUser(_adapter.getItemData(position))));
    }

    @Override
    public void onItemLongClick(View view, int position) {
        showBroToast(getActivity(), _adapter.getItemData(position).last());
    }

}
