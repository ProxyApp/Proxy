package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.app.adapter.UserAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.api.rx.RxQuery.queryUserContacts;
import static com.shareyourproxy.util.ObjectUtils.capitalize;

/**
 * Fragment to display a contactGroups contacts.
 */
public class GroupContactsFragment extends BaseFragment implements ItemClickListener {
    @Bind(R.id.fragment_contact_group_toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.fragment_contact_group_recyclerview)
    protected BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_contact_group_empty_textview)
    protected TextView emptyTextView;
    private UserAdapter _adapter;
    private CompositeSubscription _subscriptions;

    /**
     * Constructor.
     */
    public GroupContactsFragment() {
    }

    /**
     * Return new Fragment instance.
     *
     * @return layouts.fragment
     */
    public static GroupContactsFragment newInstance() {
        return new GroupContactsFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_group_users, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObserverable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserSelectedEvent) {
                        onUserSelected((UserSelectedEvent) event);
                    } else if (event instanceof GroupChannelsUpdatedEventCallback) {
                        channelsUpdated((GroupChannelsUpdatedEventCallback) event);
                    }
                }
            }));
    }

    private void channelsUpdated(GroupChannelsUpdatedEventCallback event) {
        getActivity().getIntent().putExtra(ARG_SELECTED_GROUP, event.group);
        getSupportActionBar().setTitle(capitalize(getGroupArg().label()));
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
        ButterKnife.unbind(this);
    }

    /**
     * User selected is this Fragments underlying recyclerView.Adapter.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user, getLoggedInUser().id().value());
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
        recyclerView.setEmptyView(emptyTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = UserAdapter.newInstance(this);
        _adapter.refreshUserList(queryUserContacts(
            getActivity(), getGroupArg().contacts()));
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onItemClick(View view, int position) {
        getRxBus().post(new UserSelectedEvent(_adapter.getItemData(position)));
    }

    @Override
    public void onItemLongClick(View view, int position) {
    }

}
