package com.shareyourproxy.app.fragment;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.SyncAllUsersCommand;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.event.SyncAllUsersErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllUsersSuccessEvent;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.UserAdapter;
import com.shareyourproxy.app.adapter.UserAdapter.UserViewHolder;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static android.text.Html.fromHtml;
import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.api.rx.RxQuery.queryUserContacts;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.ViewUtils.getNullScreenIconDimen;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * A recyclerView of Favorite {@link User}s.
 */
public class MainContactsFragment extends BaseFragment implements ItemClickListener {
    @Bind(R.id.fragment_contact_main_recyclerview)
    protected BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_contact_main_swipe_refresh)
    protected SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.fragment_contact_main_empty_textview)
    protected TextView emptyTextView;
    @BindDimen(R.dimen.common_margin_medium)
    protected int catPadding;
    @BindString(R.string.fragment_contact_main_empty_text)
    protected String nullMessage;

    OnRefreshListener _refreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    User user = getLoggedInUser();
                    if (user != null) {
                        getRxBus().post(new SyncAllUsersCommand(
                            getRxBus(), getLoggedInUser().id().value()));
                    }
                }
            });
        }
    };
    private UserAdapter _adapter;
    private CompositeSubscription _subscriptions;

    /**
     * Constructor.
     */
    public MainContactsFragment() {
    }

    /**
     * Create a new layouts.fragment with favorite contacts.
     *
     * @return user layouts.fragment
     */
    public static MainContactsFragment newInstance() {
        return new MainContactsFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_recyclerview, container, false);
        ButterKnife.bind(this, rootView);
        initializeRecyclerView();
        initializeSwipeRefresh();
        return rootView;
    }

    /**
     * Initialize the color sequence of the swipe refresh view.
     */
    private void initializeSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(_refreshListener);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.common_text, R.color.common_blue, R.color.common_green);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        initializeEmptyView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.hideRecyclerView(false);
        _adapter = UserAdapter.newInstance(this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void initializeEmptyView() {
        Drawable draw = getCatDrawable();
        draw.setBounds(-catPadding, 0, draw.getIntrinsicWidth(), draw.getIntrinsicHeight());
        emptyTextView.setPadding(catPadding, 0, catPadding, 0);
        emptyTextView.setCompoundDrawables(null, draw, null, null);
        emptyTextView.setText(fromHtml(nullMessage));
        recyclerView.setEmptyView(emptyTextView);
    }

    /**
     * Parse a svg and return a null screen sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private Drawable getCatDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_cat,
            getNullScreenIconDimen(getActivity()));
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
                    } else if (event instanceof LoggedInUserUpdatedEventCallback) {
                        userUpdated((LoggedInUserUpdatedEventCallback) event);
                    } else if (event instanceof SyncAllUsersCommand) {
                        swipeRefreshLayout.setRefreshing(true);
                    } else if (event instanceof SyncAllUsersSuccessEvent) {
                        swipeRefreshLayout.setRefreshing(false);
                    } else if (event instanceof SyncAllUsersErrorEvent) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }));
        refreshUserData(getLoggedInUser());
    }

    /**
     * Refresh user data.
     *
     * @param user contacts to refresh
     */
    public void refreshUserData(@NonNull User user) {
        _adapter.refreshUserList(
            queryUserContacts(getActivity(), user.contacts()));
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
        //if we're refreshing data, get rid of the UI
        swipeRefreshLayout.setRefreshing(false);
    }

    private void userUpdated(LoggedInUserUpdatedEventCallback event) {
        refreshUserData(event.user);
    }

    @Override
    public void onItemClick(View view, int position) {
        UserViewHolder holder = (UserViewHolder) recyclerView.getChildViewHolder(view);
        getRxBus().post(
            new UserSelectedEvent(
                holder.userImage, holder.userName, _adapter.getItemData(position)));
    }

    /**
     * User selected, launch that contacts profile.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user, getLoggedInUser().id().value(),
            event.imageView, event.textView);
    }
}
