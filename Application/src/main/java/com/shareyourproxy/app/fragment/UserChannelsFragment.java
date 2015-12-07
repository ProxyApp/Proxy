package com.shareyourproxy.app.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.RxQuery;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback;
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent;
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener;
import com.shareyourproxy.app.adapter.ViewChannelAdapter;
import com.shareyourproxy.app.dialog.EditChannelDialog;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.GONE;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.IntentLauncher.launchChannelListActivity;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;
import static com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.SHARE_PROFILE;

/**
 * Created by Evan on 10/10/15.
 */
public class UserChannelsFragment extends BaseFragment implements ItemLongClickListener {
    @Bind(R.id.fragment_user_channel_recyclerview)
    BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_user_channel_empty_view_container)
    LinearLayout emptyViewContainer;
    @Bind(R.id.fragment_user_channel_empty_button)
    Button addChannelButton;
    @Bind(R.id.fragment_user_channel_empty_textview)
    TextView emptyTextView;
    @Bind(R.id.fragment_user_channel_coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindString(R.string.fragment_userchannels_empty_title)
    String loggedInNullTitle;
    @BindString(R.string.fragment_userchannels_empty_message)
    String loggedInNullMessage;
    @BindString(R.string.fragment_userprofile_contact_empty_title)
    String contactNullTitle;
    @BindDimen(R.dimen.common_svg_null_screen_mini)
    int marginNullScreen;
    @BindColor(R.color.common_blue)
    int colorBlue;
    private boolean _isLoggedInUser;
    private User _userContact;
    private ViewChannelAdapter _adapter;
    private CompositeSubscription _subscriptions;
    private RxQuery _rxQuery = RxQuery.INSTANCE;
    private RxHelper _rxHelper = RxHelper.INSTANCE;

    /**
     * Constructor.
     */
    public UserChannelsFragment() {
    }

    /**
     * Create a new user channel fragment.
     *
     * @return user channels fragment.
     */
    public static UserChannelsFragment newInstance(User contact) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_USER_SELECTED_PROFILE, contact);
        UserChannelsFragment fragment = new UserChannelsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fragment_user_channel_empty_button)
    public void onClickAddChannel() {
        launchChannelListActivity(getActivity());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        _userContact = getArguments()
            .getParcelable(ARG_USER_SELECTED_PROFILE);
        _isLoggedInUser = isLoggedInUser(_userContact);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_channels, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        if (_isLoggedInUser) {
            initializeRecyclerView(getLoggedInUser().channels());
        } else {
            addChannelButton.setVisibility(GONE);
            initializeRecyclerView(null);
        }
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView(HashMap<String, Channel> channels) {
        _adapter = ViewChannelAdapter.newInstance(
            recyclerView, getSharedPreferences(), isShowHeader(channels), this);
        initializeEmptyView();

        recyclerView.setEmptyView(emptyViewContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(_adapter);
    }

    private boolean isShowHeader(HashMap<String, Channel> channels) {
        return channels != null && _isLoggedInUser && channels.size() > 0 &&
            !getSharedPreferences().getBoolean(SHARE_PROFILE.getKey(), false);
    }

    private void initializeEmptyView() {
        Context context = getContext();
        if (_isLoggedInUser) {
            SpannableStringBuilder sb = new SpannableStringBuilder(loggedInNullTitle).append("\n")
                .append(loggedInNullMessage);

            sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body2),
                0, loggedInNullTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
                loggedInNullTitle.length() + 1, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            emptyTextView.setText(sb);
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
                null, getNullDrawable(R.raw.ic_ghost_doge), null, null);


        } else {
            String contactNullMessage = getString(
                R.string.fragment_userprofile_contact_empty_message, _userContact.first());
            SpannableStringBuilder sb = new SpannableStringBuilder(contactNullTitle).append("\n")
                .append(contactNullMessage);

            sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body2),
                0, contactNullTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
                contactNullTitle.length() + 1, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            emptyTextView.setText(sb);

            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
                null, getNullDrawable(R.raw.ic_ghost_sloth), null, null);
        }
    }

    /**
     * Parse a svg and return a null screen sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private Drawable getNullDrawable(int resId) {
        return svgToBitmapDrawable(getActivity(), resId, marginNullScreen);
    }

    public void getSharedChannels() {
        _subscriptions.add(_rxQuery.queryPermissionedChannels(_userContact, getLoggedInUser().id())
            .subscribe(permissionedObserver()));
    }

    private JustObserver<HashMap<String, Channel>> permissionedObserver() {
        return new JustObserver<HashMap<String, Channel>>() {
            @Override
            public void next(HashMap<String, Channel> channels) {
                _adapter.updateChannels(channels);
            }

        };
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = _rxHelper.checkCompositeButton(_subscriptions);
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(onNextEvent()));
        if (_isLoggedInUser) {
            syncUsersContacts();
        } else {
            getSharedChannels();
        }
        recyclerView.scrollToPosition(0);
    }

    private JustObserver<Object> onNextEvent() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof UserChannelAddedEventCallback) {
                    addUserChannel(((UserChannelAddedEventCallback) event));
                } else if (event instanceof UserChannelDeletedEventCallback) {
                    deleteUserChannel(((UserChannelDeletedEventCallback) event));
                } else if (event instanceof SyncAllContactsSuccessEvent) {
                    if (_isLoggedInUser) {
                        syncUsersContacts();
                    }
                }
            }
        };
    }

    public void syncUsersContacts() {
        HashMap<String, Channel> channels = getLoggedInUser().channels();
        if (channels != null && channels.size() > 0) {
            _adapter.updateChannels(channels);
        } else {
            recyclerView.updateViewState(new RecyclerViewDatasetChangedEvent(
                _adapter, BaseRecyclerView.ViewState.EMPTY));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    private void addUserChannel(UserChannelAddedEventCallback event) {
        if (event.oldChannel != null) {
            _adapter.updateItem(event.oldChannel, event.newChannel);
        } else {
            _adapter.addItem(event.newChannel);
        }
    }

    private void deleteUserChannel(UserChannelDeletedEventCallback event) {
        _adapter.removeItem(event.position);
    }

    @Override
    public final void onItemClick(View view, int position) {
        Channel channel = _adapter.getItemData(position);
        getRxBus().post(new SelectUserChannelEvent(channel));
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Channel channel = _adapter.getItemData(position);
        if (_isLoggedInUser) {
            EditChannelDialog.newInstance(channel, position).show(getFragmentManager());
        }
    }
}
