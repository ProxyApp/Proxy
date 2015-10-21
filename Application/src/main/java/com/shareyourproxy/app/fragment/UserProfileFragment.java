package com.shareyourproxy.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.shareyourproxy.app.UserProfileActivity;
import com.shareyourproxy.app.dialog.SaveGroupChannelDialog;
import com.shareyourproxy.app.dialog.UserGroupsDialog;
import com.shareyourproxy.widget.transform.AlphaTransform;
import com.shareyourproxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
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

import static android.support.design.widget.Snackbar.LENGTH_INDEFINITE;
import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.IntentLauncher.launchChannelListActivity;
import static com.shareyourproxy.api.RestClient.getUserService;
import static com.shareyourproxy.api.rx.RxQuery.getUserContactScore;
import static com.shareyourproxy.api.rx.RxQuery.queryContactGroups;
import static com.shareyourproxy.util.ObjectUtils.joinWithSpace;
import static com.shareyourproxy.util.ViewUtils.getMenuIcon;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;
import static java.util.Collections.singletonList;

/**
 * Display a User or a User Contact's Channels. Allow Users to edit their channels. Allow User
 * Contact's to be added to be observed and added to groups logged in user groups.
 */
public class UserProfileFragment extends BaseFragment {

    @Bind(R.id.fragment_user_profile_toolbar)
    Toolbar toolbar;
    @Bind(R.id.fragment_user_profile_header_image)
    ImageView userImage;
    @Bind(R.id.fragment_user_profile_header_followers)
    TextView followersTextView;
    @Bind(R.id.fragment_user_profile_header_button)
    Button groupButton;
    @Bind(R.id.fragment_user_profile_viewpager)
    ViewPager viewPager;
    @Bind(R.id.fragment_user_profile_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.fragment_user_profile_coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @Bind(R.id.fragment_user_profile_sliding_tabs)
    TabLayout slidingTabLayout;
    @Bind(R.id.fragment_user_profile_fab)
    FloatingActionButton floatingActionButton;
    @BindColor(R.color.common_blue)
    int colorBlue;
    @BindColor(android.R.color.white)
    int colorWhite;
    @BindColor(R.color.common_proxy_dark_selected)
    int _selectedColor;
    @BindColor(R.color.common_proxy_dark_disabled)
    int _unselectedColor;
    @BindString(R.string.calculating)
    String stringCalculating;
    @BindString(R.string.error_calculating)
    String stringErrorCalculating;
    @BindDimen(R.dimen.common_svg_large)
    int marginSVGLarge;
    private Target _target;
    private Target _backgroundTarget;
    private PaletteAsyncListener _paletteListener;
    private User _userContact;
    private CompositeSubscription _subscriptions;
    private boolean _isLoggedInUser;
    private ArrayList<GroupToggle> _contactGroups = new ArrayList<>();
    private List<? extends BaseFragment> fragmentArray;
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

    @OnClick(R.id.fragment_user_profile_header_button)
    void onClickGroup() {
        UserGroupsDialog.newInstance(_contactGroups, _userContact).show(getFragmentManager());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        _userContact = getActivity().getIntent().getExtras().getParcelable
            (ARG_USER_SELECTED_PROFILE);
        _isLoggedInUser = isLoggedInUser(_userContact);

        String loggedInUserId = getActivity().getIntent().getExtras()
            .getString(Constants.ARG_LOGGEDIN_USER_ID);
        checkLoggedInUserValue(loggedInUserId);
    }

    /**
     * If we entered this activity fragment through a notification, make sure the logged in user has
     * a value.
     *
     * @param loggedInUserId logged in user id.
     */
    public void checkLoggedInUserValue(String loggedInUserId) {
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
        _subscriptions = checkCompositeButton(_subscriptions);
        initializeFab();
        setToolbarTitle();
        initializeHeader();
        initializeViewPager();
    }

    private void initializeFab() {
        if (_isLoggedInUser) {
            initializeFabPlusIcon();
        } else {
            floatingActionButton.setVisibility(View.GONE);
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

    private void setToolbarTitle() {
        String title = joinWithSpace(new String[]{ _userContact.first(), _userContact.last() });
        buildToolbar(toolbar, title, null);
        followersTextView.setText(getString(R.string.user_profile_followers,
            stringCalculating));
    }

    /**
     * Initialize the Header view data and state.
     */
    private void initializeHeader() {
        String profileURL = _userContact.profileURL();
        String coverURL = _userContact.coverURL();
        //profile pic
        if (profileURL != null && !profileURL.trim().isEmpty() && !profileURL.contains(".gif")) {
            Picasso.with(getActivity()).load(profileURL)
                .placeholder(R.mipmap.ic_proxy)
                .transform(new CircleTransform())
                .into(getBitmapTargetView());
        } else {
            Picasso.with(getActivity()).load(R.mipmap.ic_proxy)
                .transform(new CircleTransform())
                .into(getBitmapTargetView());
        }
        //Background cover photo
        if (coverURL != null && !coverURL.isEmpty() && !coverURL.contains(".gif")) {
            Picasso.with(getActivity()).load(coverURL)
                .transform(AlphaTransform.create())
                .into(getBackgroundTarget());
        } else {
            Picasso.with(getActivity()).load(R.mipmap.ic_proxy)
                .transform(AlphaTransform.create())
                .into(getBackgroundTarget());
        }
        //update group button
        if (_isLoggedInUser) {
            groupButton.setVisibility(View.GONE);
        } else {
            groupButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                getMenuIcon(getActivity(), R.raw.ic_groups), null, null, null);
            getGroupEditContacts();
        }
        //followers score
        _subscriptions.add(getUserContactScore(getActivity(), _userContact.id())
            .subscribe(getContactScoreObserver()));
    }

    private void initializeViewPager() {
        initializeFragments();
        initializeTabs();
    }

    /**
     * Initialize this fragments tabs and their icons. Select the default tab based input intent
     * data from user action.
     */
    private void initializeTabs() {
//        slidingTabLayout.addTab(
//            slidingTabLayout.newTab()
//                .setText(R.string.activity));
        //TODO:REMOVE ME
        slidingTabLayout.setVisibility(View.GONE);
        slidingTabLayout.addTab(
            slidingTabLayout.newTab()
                .setText(R.string.channels));

        slidingTabLayout.setOnTabSelectedListener(getOnTabSelectedListener());
        viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(slidingTabLayout));
        //set the defualt selected tab
        slidingTabLayout.getTabAt(getActivity().getIntent().getExtras()
            .getInt(Constants.ARG_MAINFRAGMENT_SELECTED_TAB)).select();
    }

    /**
     * Get a tab selection listener that tints tab drawables correctly.
     *
     * @return OnTabSelectedListener
     */
    private TabLayout.OnTabSelectedListener getOnTabSelectedListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
    }

    /**
     * Add fragments to the List backing the {@link MainFragment#slidingTabLayout}.
     */
    private void initializeFragments() {
//        fragmentArray = asList(
//            UserFeedFragment.newInstance(), UserChannelsFragment.newInstance());
        fragmentArray = singletonList(UserChannelsFragment.newInstance());
        viewPager.setAdapter(
            BasePagerAdapter.newInstance(fragmentArray, getChildFragmentManager()));
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
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
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

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = checkCompositeButton(_subscriptions);
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(onNextEvent()));
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    public JustObserver<Integer> getContactScoreObserver() {
        return new JustObserver<Integer>() {
            @Override
            public void next(Integer integer) {
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

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof GroupContactsUpdatedEventCallback) {
                    groupContactsUpdatedEvent((GroupContactsUpdatedEventCallback) event);
                } else if (event instanceof UserChannelAddedEventCallback) {
                    addUserChannel(((UserChannelAddedEventCallback) event));
                }else if (event instanceof UserChannelDeletedEventCallback) {
                    deleteUserChannel(((UserChannelDeletedEventCallback) event));
                }
            }
        };
    }

    private void deleteUserChannel(UserChannelDeletedEventCallback event) {
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

    private void addUserChannel(UserChannelAddedEventCallback event) {
        if (event.oldChannel != null) {
            showChangesSavedSnackBar(coordinatorLayout);
        } else {
            showAddedChannelSnackBar();
            SaveGroupChannelDialog.newInstance(event.newChannel, event.user)
                .show(getFragmentManager());
        }
    }

    private void showAddedChannelSnackBar() {
        make(coordinatorLayout, getString(R.string.channel_added), LENGTH_LONG).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (BaseFragment frag : fragmentArray) {
            frag.onActivityResult(requestCode, resultCode, data);
        }
    }

}
