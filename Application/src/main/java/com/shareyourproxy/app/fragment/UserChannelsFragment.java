package com.shareyourproxy.app.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback;
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent;
import com.shareyourproxy.api.rx.event.SyncAllUsersSuccessEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener;
import com.shareyourproxy.app.adapter.ViewChannelAdapter;
import com.shareyourproxy.app.dialog.EditChannelDialog;
import com.shareyourproxy.app.dialog.SaveGroupChannelDialog;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.support.design.widget.Snackbar.LENGTH_INDEFINITE;
import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;
import static android.text.Html.fromHtml;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.IntentLauncher.launchChannelListActivity;
import static com.shareyourproxy.api.rx.RxQuery.queryPermissionedChannels;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Created by Evan on 10/10/15.
 */
public class UserChannelsFragment extends BaseFragment implements ItemLongClickListener {
    @Bind(R.id.fragment_user_channel_recyclerview)
    BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_user_channel_empty_textview)
    TextView emptyTextView;
    @Bind(R.id.fragment_user_channel_coordinator)
    CoordinatorLayout coordinatorLayout;
    @Bind(R.id.fragment_user_channel_fab)
    FloatingActionButton floatingActionButton;
    @BindString(R.string.fragment_userchannels_empty_text)
    String stringNullMessage;
    @BindDimen(R.dimen.common_svg_large)
    int marginSVGLarge;
    @BindDimen(R.dimen.common_svg_null_screen)
    int marginNullScreen;
    @BindColor(R.color.common_blue)
    int colorBlue;
    @BindColor(android.R.color.white)
    int colorWhite;
    private Channel _deletedChannel;
    private boolean _isLoggedInUser;
    private User _userContact;
    private ViewChannelAdapter _adapter;
    private CompositeSubscription _subscriptions;

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
    public static UserChannelsFragment newInstance() {
        return new UserChannelsFragment();
    }

    @OnClick(R.id.fragment_user_channel_fab)
    public void onClick() {
        launchChannelListActivity(getActivity());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        _userContact = getActivity().getIntent().getExtras().getParcelable
            (ARG_USER_SELECTED_PROFILE);
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
        _subscriptions = checkCompositeButton(_subscriptions);
        if (_isLoggedInUser) {
            initializeFabPlusIcon();
            initializeRecyclerView(getLoggedInUser().channels());
        } else {
            floatingActionButton.setVisibility(View.GONE);
            initializeRecyclerView(null);
            getSharedChannels();
        }
    }

    /**
     * Set the content image of this {@link FloatingActionButton}
     */
    private void initializeFabPlusIcon() {
        Drawable drawable = svgToBitmapDrawable(
            getActivity(), R.raw.ic_add, marginSVGLarge, colorWhite);
        floatingActionButton.setImageDrawable(drawable);
        ViewCompat.setElevation(floatingActionButton, 10f);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView(HashMap<String, Channel> channels) {
        initializeEmptyView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = ViewChannelAdapter.newInstance(channels, this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void initializeEmptyView() {
        if (_isLoggedInUser) {
            emptyTextView.setText(fromHtml(stringNullMessage));
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
                null, getNullDrawable(R.raw.ic_ghost_doge), null, null);
        } else {
            emptyTextView.setText(
                fromHtml(getString(R.string.fragment_userprofile_contact_empty_text,
                    _userContact.first())));
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
                null, getNullDrawable(R.raw.ic_ghost_sloth), null, null);
        }
        recyclerView.setEmptyView(emptyTextView);
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
        _subscriptions.add(queryPermissionedChannels(
            getActivity(), getLoggedInUser().id(), _userContact.id())
            .subscribe(permissionedObserver()));
    }

    private JustObserver<HashMap<String, Channel>> permissionedObserver() {
        return new JustObserver<HashMap<String, Channel>>() {
            @Override
            public void next(HashMap<String, Channel> channels) {
                _adapter.updateChannels(channels);
            }

            @Override
            public void error(Throwable e) {
                Timber.e("Error downloading permissioned channels");
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = checkCompositeButton(_subscriptions);
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(onNextEvent()));
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof UserChannelAddedEventCallback) {
                    addUserChannel(((UserChannelAddedEventCallback) event));
                } else if (event instanceof UserChannelDeletedEventCallback) {
                    deleteUserChannel(((UserChannelDeletedEventCallback) event));
                } else if (event instanceof SyncAllUsersSuccessEvent) {
                    _adapter.notifyDataSetChanged();
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

    private void addUserChannel(UserChannelAddedEventCallback event) {
        if (event.oldChannel != null) {
            _adapter.updateChannel(event.oldChannel, event.newChannel);
            showChangesSavedSnackBar(coordinatorLayout);
        } else {
            _adapter.addChannel(event.newChannel);
            showAddedChannelSnackBar();
            SaveGroupChannelDialog.newInstance(event.newChannel, event.user)
                .show(getFragmentManager());
        }
    }

    private void deleteUserChannel(UserChannelDeletedEventCallback event) {
        _adapter.removeChannel(event.channel);
        _deletedChannel = event.channel;
        showDeletedChannelSnackBar();
    }

    private void showDeletedChannelSnackBar() {
        Snackbar snackbar = make(coordinatorLayout, getString(R.string.undo_delete),
            LENGTH_INDEFINITE);
        snackbar.setAction(getString(R.string.undo), getAddChannelClickListener());
        snackbar.setActionTextColor(colorBlue);
        snackbar.show();
    }

    private void showAddedChannelSnackBar() {
        make(coordinatorLayout, getString(R.string.channel_added), LENGTH_LONG).show();
    }

    /**
     * Get a click listener to add a deleted channel.
     *
     * @return click listener
     */
    private View.OnClickListener getAddChannelClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRxBus().post(new AddUserChannelCommand(getRxBus(), getLoggedInUser(),
                    _deletedChannel));
            }
        };
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
            EditChannelDialog.newInstance(channel).show(getFragmentManager());
        }
    }
}
