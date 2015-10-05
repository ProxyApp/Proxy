package com.shareyourproxy.app.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.Constants;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback;
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent;
import com.shareyourproxy.api.rx.event.SyncAllUsersSuccessEvent;
import com.shareyourproxy.app.UserProfileActivity;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener;
import com.shareyourproxy.app.adapter.ViewChannelAdapter;
import com.shareyourproxy.app.dialog.EditChannelDialog;
import com.shareyourproxy.app.dialog.SaveGroupChannelDialog;
import com.shareyourproxy.app.dialog.UserGroupsDialog;
import com.shareyourproxy.widget.ContentDescriptionDrawable;
import com.shareyourproxy.widget.transform.AlphaTransform;
import com.shareyourproxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;
import static android.text.Html.fromHtml;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.IntentLauncher.launchChannelListActivity;
import static com.shareyourproxy.api.RestClient.getUserService;
import static com.shareyourproxy.api.rx.RxQuery.getUserContactScore;
import static com.shareyourproxy.api.rx.RxQuery.queryContactGroups;
import static com.shareyourproxy.api.rx.RxQuery.queryPermissionedChannels;
import static com.shareyourproxy.util.ObjectUtils.joinWithSpace;
import static com.shareyourproxy.util.ViewUtils.getMenuIcon;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Display a User or a User Contact's Channels. Allow Users to edit their channels. Allow User
 * Contact's to be added to be observed and added to groups logged in user groups.
 */
public class UserProfileFragment extends BaseFragment implements ItemLongClickListener {

    @BindDimen(R.dimen.common_svg_large)
    int marginSVGLarge;
    @BindDimen(R.dimen.common_svg_null_screen)
    int marginNullScreen;
    @Bind(R.id.fragment_user_profile_toolbar)
    Toolbar toolbar;
    @Bind(R.id.fragment_user_profile_recyclerview)
    BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_user_profile_header_image)
    ImageView userImage;
    @Bind(R.id.fragment_user_profile_header_followers)
    TextView followersTextView;
    @Bind(R.id.fragment_user_profile_header_button)
    Button groupButton;
    @Bind(R.id.fragment_user_profile_empty_textview)
    TextView emptyTextView;
    @Bind(R.id.fragment_user_profile_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.fragment_user_profile_fab)
    FloatingActionButton floatingActionButton;
    @Bind(R.id.fragment_user_profile_coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindColor(R.color.common_blue)
    int colorBlue;
    @BindString(R.string.fragment_userprofile_user_empty_text)
    String stringNullMessage;
    @BindString(R.string.calculating)
    String stringCalculating;
    @BindString(R.string.error_calculating)
    String stringErrorCalculating;
    private ViewChannelAdapter _adapter;
    private Target _target;
    private Target _backgroundTarget;
    private PaletteAsyncListener _paletteListener;
    private User _userContact;
    private CompositeSubscription _subscriptions;
    private boolean _isLoggedInUser;
    private ArrayList<GroupToggle> _contactGroups = new ArrayList<>();
    private Channel _deletedChannel;

    /**
     * Empty Fragment Constructor.
     */
    public UserProfileFragment() {
    }

    /**
     * Return new instance for parent {@link UserProfileActivity}.
     *
     * @return layouts.fragment
     */
    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @OnClick(R.id.fragment_user_profile_fab)
    public void onClick() {
        launchChannelListActivity(getActivity());
    }

    @OnClick(R.id.fragment_user_profile_header_button)
    void onClickGroup() {
        UserGroupsDialog.newInstance(_contactGroups, _userContact).show(getFragmentManager());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        _userContact = getActivity().getIntent().getExtras().getParcelable
            (ARG_USER_SELECTED_PROFILE);
        String loggedInUserId = getActivity().getIntent().getExtras()
            .getString(Constants.ARG_LOGGEDIN_USER_ID);
        _isLoggedInUser = isLoggedInUser(_userContact);
        if (getLoggedInUser() == null) {
            User user = null;
            try {
                user = getSharedPrefJsonUser();
            } catch (Exception e) {
                Timber.e(Log.getStackTraceString(e));
            }
            //set the shared preferences user if it matches the logged in user id
            if (user != null && user.id().equals(loggedInUserId)) {
                setLoggedInUser(user);
            } else {
                setLoggedInUser(getUserService(getActivity(), getRxBus())
                    .getUser(loggedInUserId).toBlocking().single());
            }
        }
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        checkCompositeButton();
        setToolbarTitle();
        initializeSVG();
        initializeHeader();
        _subscriptions.add(getUserContactScore(getActivity(), _userContact.id())
            .subscribe(getContactScoreObserver()));
        if (!_isLoggedInUser) {
            floatingActionButton.setVisibility(View.GONE);
            getGroupEditContacts();
            initializeRecyclerView(null);
            getSharedChannels();
        } else {
            initializeRecyclerView(getLoggedInUser().channels());
        }
    }

    /**
     * Set the content image of this {@link FloatingActionButton}
     */
    private void initializeSVG() {
        Drawable drawable = svgToBitmapDrawable(getActivity(), R.raw.ic_add,
            marginSVGLarge, Color.WHITE);
        floatingActionButton.setImageDrawable(drawable);
        ViewCompat.setElevation(floatingActionButton, 10f);
    }

    private void getGroupEditContacts() {
        _contactGroups.clear();
        //creates group edit contacts array
        List<GroupToggle> list = queryContactGroups(
            getLoggedInUser(), _userContact);
        _contactGroups.addAll(list);
        ArrayList<Group> selectedGroupsList = new ArrayList<>(list.size());
        for (GroupToggle groupToggle : list) {
            if (groupToggle.isChecked()) {
                selectedGroupsList.add(groupToggle.getGroup());
            }
        }
        updateGroupButtonText(selectedGroupsList);
    }

    private void setToolbarTitle() {
        String title = joinWithSpace(new String[]{ _userContact.first(), _userContact.last() });
        buildToolbar(toolbar, title, null);
        followersTextView.setText(getString(R.string.user_profile_followers,
            stringCalculating));
    }

    private void initializeHeader() {
        Picasso.with(getActivity()).load(_userContact.profileURL())
            .placeholder(R.mipmap.ic_proxy)
            .transform(new CircleTransform())
            .into(getBitmapTargetView());

        if (_userContact.coverURL() != null && !_userContact.coverURL().isEmpty()) {
            Picasso.with(getActivity()).load(_userContact.coverURL())
                .transform(AlphaTransform.create())
                .into(getBackgroundTarget());
        }
        if (_isLoggedInUser) {
            groupButton.setVisibility(View.GONE);
        } else {
            groupButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                getMenuIcon(getActivity(), R.raw.ic_groups), null, null, null);
        }
    }

    /**
     * Handle setting the User profile background cover bitmap.
     *
     * @return Target callback
     */
    private Target getBackgroundTarget() {
        if (_backgroundTarget == null) {
            _backgroundTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    collapsingToolbarLayout.setBackground(
                        new BitmapDrawable(getResources(), bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
        }
        return _backgroundTarget;
    }

    /**
     * Strong Reference Bitmap Target.
     *
     * @return target
     */
    private Target getBitmapTargetView() {
        if (_target == null) {
            _target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    userImage.setImageBitmap(bitmap);
                    new Palette.Builder(bitmap).generate(getPaletteAsyncListener());
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(errorDrawable.getIntrinsicWidth(),
                        errorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    userImage.setImageBitmap(bitmap);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(placeHolderDrawable.getIntrinsicWidth(),
                        placeHolderDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    userImage.setImageBitmap(bitmap);
                }
            };
        }
        return _target;
    }

    /**
     * Async returns when palette has been loaded.
     *
     * @return palette listener
     */
    private PaletteAsyncListener getPaletteAsyncListener() {
        if (_paletteListener == null) {
            _paletteListener = new PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    Integer offColor = palette.getMutedColor(colorBlue);
                    Integer color = palette.getVibrantColor(offColor);

                    collapsingToolbarLayout.setContentScrimColor(color);
                    collapsingToolbarLayout.setStatusBarScrimColor(color);
                    if (_userContact.coverURL() == null || "".equals(_userContact.coverURL())) {
                        collapsingToolbarLayout.setBackgroundColor(color);
                    }
                }
            };
        }
        return _paletteListener;
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

    @Override
    public void onResume() {
        super.onResume();
        checkCompositeButton();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(onNextEvent()));
    }

    public JustObserver<Integer> getContactScoreObserver() {
        return new JustObserver<Integer>() {


            @Override
            public void success(Integer integer) {
                followersTextView.setText(getString(R.string.user_profile_followers,
                    integer));
            }

            @Override
            public void error(Throwable e) {
                followersTextView.setText(getString(R.string.user_profile_followers,
                    stringErrorCalculating));
            }
        };
    }

    private void checkCompositeButton() {
        if (_subscriptions == null) {
            _subscriptions = new CompositeSubscription();
        }
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof UserChannelAddedEventCallback) {
                    addUserChannel(((UserChannelAddedEventCallback) event));
                } else if (event instanceof UserChannelDeletedEventCallback) {
                    deleteUserChannel(((UserChannelDeletedEventCallback) event));
                } else if (event instanceof GroupContactsUpdatedEventCallback) {
                    groupContactsUpdatedEvent((GroupContactsUpdatedEventCallback) event);
                } else if (event instanceof SyncAllUsersSuccessEvent) {
                    _adapter.notifyDataSetChanged();
                }
            }
        };
    }

    private void groupContactsUpdatedEvent(GroupContactsUpdatedEventCallback event) {
        updateGroupButtonText(event.contactGroups);
        _subscriptions.add(getUserContactScore(getActivity(), _userContact.id())
            .subscribe(getContactScoreObserver()));
    }

    @SuppressWarnings("unchecked")
    private void updateGroupButtonText(List<Group> list) {
        if (list != null) {
            int groupSize = list.size();
            if (groupSize == 0) {
                groupButton.setText(R.string.add_to_group);
                groupButton.setBackgroundResource(R.drawable.selector_button_zoidberg);
            } else if (groupSize == 1) {
                groupButton.setText(list.get(0).label());
                groupButton.setBackgroundResource(R.drawable.selector_button_grey);
            } else if (groupSize > 1) {
                groupButton.setText(getString(R.string.in_blank_groups, groupSize));
                groupButton.setBackgroundResource(R.drawable.selector_button_grey);
            }
        } else {
            groupButton.setText(R.string.add_to_group);
            groupButton.setBackgroundResource(R.drawable.selector_button_zoidberg);
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
        Snackbar snackbar = make(coordinatorLayout, getString(R.string.undo_delete), LENGTH_LONG);
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
    private OnClickListener getAddChannelClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                getRxBus().post(new AddUserChannelCommand(getRxBus(), getLoggedInUser(),
                    _deletedChannel));
            }
        };
    }

    public void getSharedChannels() {
        _subscriptions.add(queryPermissionedChannels(
            getActivity(), getLoggedInUser().id(), _userContact.id())
            .subscribe(permissionedObserver()));
    }

    private JustObserver<HashMap<String, Channel>> permissionedObserver() {
        return new JustObserver<HashMap<String, Channel>>() {
            @Override
            public void success(HashMap<String, Channel> channels) {
                _adapter.updateChannels(channels);
            }

            @Override
            public void error(Throwable e) {
                Timber.e("Error downloading permissioned channels");
            }
        };
    }
}
