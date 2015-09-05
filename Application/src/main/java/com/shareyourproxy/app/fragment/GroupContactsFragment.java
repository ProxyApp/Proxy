package com.shareyourproxy.app.fragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.app.adapter.UserAdapter;
import com.shareyourproxy.app.adapter.UserAdapter.UserViewHolder;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static android.text.Html.fromHtml;
import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.api.rx.RxQuery.queryUserContacts;
import static com.shareyourproxy.util.ObjectUtils.capitalize;
import static com.shareyourproxy.util.ViewUtils.getNullScreenIconDimen;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Display the {@link User} contacts added to the selected {@link Group}.
 */
public class GroupContactsFragment extends BaseFragment implements ItemClickListener {
    @Bind(R.id.fragment_contact_group_toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.fragment_contact_group_recyclerview)
    protected BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_contact_group_empty_textview)
    protected TextView emptyTextView;
    @BindString(R.string.fragment_contact_group_empty_text)
    protected String emptyTextMessage;
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
     * @return GroupContactsFragment
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
        _subscriptions.add(getRxBus().toObservable()
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

    /**
     * A Group has been edited in {@link GroupEditChannelFragment}. Update this fragments intent
     * data and title.
     *
     * @param event group data
     */
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
     * User selected from this groups contacts. Open that Users profile.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user,
            getLoggedInUser().id().value(), event.imageView, event.textView);
    }

    /**
     * Get the group selected and bundled in this activities {@link
     * IntentLauncher#launchEditGroupContactsActivity(Activity,
     * Group)} call.
     *
     * @return selected group
     */
    private Group getGroupArg() {
        return (Group) getActivity().getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeRecyclerView();
        buildToolbar(toolbar, capitalize(getGroupArg().label()), null);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        initializeEmptyView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = UserAdapter.newInstance(this);
        _adapter.refreshUserList(queryUserContacts(
            getActivity(), getGroupArg().contacts()));
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
    }

    private void initializeEmptyView() {
        emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
            null, getFishDrawable(), null, null);
        emptyTextView.setText(fromHtml(emptyTextMessage));
        recyclerView.setEmptyView(emptyTextView);
    }

    /**
     * Parse a svg and return a null screen sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private Drawable getFishDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_fish,
            getNullScreenIconDimen(getActivity()));
    }

    @Override
    public void onItemClick(View view, int position) {
        UserViewHolder holder = (UserViewHolder) recyclerView.getChildViewHolder(view);
        getRxBus().post(
            new UserSelectedEvent(
                holder.userImage, holder.userName, _adapter.getItemData(position)));
    }

}
