package com.shareyourproxy.app.fragment;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxGoogleAnalytics;
import com.shareyourproxy.api.rx.RxQuery;
import com.shareyourproxy.api.rx.command.SyncContactsCommand;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.event.NotificationCardActionEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.UserContactsAdapter;
import com.shareyourproxy.app.adapter.UserContactsAdapter.UserViewHolder;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import java.util.HashSet;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.IntentLauncher.launchInviteFriendIntent;
import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;
import static com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.INVITE_FRIENDS;

/**
 * A recyclerView of Favorite {@link User}s.
 */
public class MainContactsFragment extends BaseFragment implements ItemClickListener {
    @Bind(R.id.fragment_contact_main_recyclerview)
    BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_contact_main_swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.fragment_contact_main_empty_textview)
    TextView emptyTextView;
    @Bind(R.id.fragment_contact_main_empty_view)
    ScrollView emptyView;
    @BindDimen(R.dimen.common_margin_medium)
    int catPadding;
    @BindDimen(R.dimen.common_svg_null_screen_small)
    int marginNullScreen;
    @BindString(R.string.fragment_contact_main_empty_title)
    String nullTitle;
    @BindString(R.string.fragment_contact_main_empty_message)
    String nullMessage;
    OnRefreshListener _refreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            User user = getLoggedInUser();
            if (user != null) {
                getRxBus().post(new SyncContactsCommand(getLoggedInUser()));
            }
        }
    };
    private UserContactsAdapter _adapter;
    private CompositeSubscription _subscriptions;
    private RxQuery _rxQuery = RxQuery.INSTANCE;

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

    @OnClick(R.id.fragment_contact_main_empty_button)
    public void onClickInviteFriend() {
        launchInviteFriendIntent(getActivity());
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts_main, container, false);
        ButterKnife.bind(this, rootView);
        initializeRecyclerView();
        initializeSwipeRefresh(swipeRefreshLayout, _refreshListener);
        return rootView;
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        boolean showHeader = !getSharedPreferences().getBoolean(INVITE_FRIENDS.getKey(), false);
        _adapter = UserContactsAdapter.newInstance(recyclerView, getSharedPreferences(),
            showHeader, this);
        initializeEmptyView();

        recyclerView.setEmptyView(emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(_adapter);
    }

    private void initializeEmptyView() {
        Context context = getContext();
        Drawable draw = getCatDrawable();
        draw.setBounds(-catPadding, 0, draw.getIntrinsicWidth(), draw.getIntrinsicHeight());
        emptyTextView.setPadding(catPadding, 0, catPadding, 0);
        emptyTextView.setCompoundDrawables(null, draw, null, null);

        SpannableStringBuilder sb = new SpannableStringBuilder(nullTitle).append("\n")
            .append(nullMessage);

        sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body2),
            0, nullTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
            nullTitle.length() + 1, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        emptyTextView.setText(sb);
    }

    /**
     * Parse a svg and return a null screen sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private Drawable getCatDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_gato, marginNullScreen);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(getBusObserver()));
        checkRefresh(getLoggedInUser());
    }

    public JustObserver<Object> getBusObserver() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof UserSelectedEvent) {
                    onUserSelected((UserSelectedEvent) event);
                } else if (event instanceof LoggedInUserUpdatedEventCallback) {
                    userUpdated((LoggedInUserUpdatedEventCallback) event);
                } else if (event instanceof SyncContactsCommand) {
                    swipeRefreshLayout.setRefreshing(true);
                } else if (event instanceof SyncAllContactsSuccessEvent) {
                    swipeRefreshLayout.setRefreshing(false);
                } else if (event instanceof SyncAllContactsErrorEvent) {
                    swipeRefreshLayout.setRefreshing(false);
                } else if (event instanceof NotificationCardActionEvent) {
                    launchInviteFriendIntent(getActivity());
                }
            }
        };
    }

    /**
     * Refresh user data.
     *
     * @param user contacts to refresh
     */
    public void checkRefresh(User user) {
        HashSet<String> contacts = user.contacts();
        if (contacts != null) {
            _adapter.refreshUserList(_rxQuery.queryUserContacts(getActivity(), contacts));
        }
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
        checkRefresh(event.user);
    }

    @Override
    public void onItemClick(View view, int position) {
        UserViewHolder holder = (UserViewHolder) recyclerView.getChildViewHolder(view);
        User user = _adapter.getItemData(position);
        new RxGoogleAnalytics(getActivity()).contactProfileViewed(user);
        getRxBus().post(new UserSelectedEvent(holder.userImage, holder.userName, user));
    }

    /**
     * User selected, launch that contacts profile.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user, getLoggedInUser().id(),
            event.imageView, event.textView);
    }
}
