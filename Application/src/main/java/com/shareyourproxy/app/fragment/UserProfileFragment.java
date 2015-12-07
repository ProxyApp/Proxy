package com.shareyourproxy.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.shareyourproxy.Constants;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxGoogleAnalytics;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.api.rx.command.SyncContactsCommand;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback;
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.support.design.widget.Snackbar.LENGTH_INDEFINITE;
import static android.support.design.widget.Snackbar.make;
import static com.facebook.drawee.backends.pipeline.Fresco.newDraweeControllerBuilder;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.IntentLauncher.launchAddressIntent;
import static com.shareyourproxy.IntentLauncher.launchElloIntent;
import static com.shareyourproxy.IntentLauncher.launchEmailIntent;
import static com.shareyourproxy.IntentLauncher.launchFBMessengerIntent;
import static com.shareyourproxy.IntentLauncher.launchFacebookIntent;
import static com.shareyourproxy.IntentLauncher.launchGithubIntent;
import static com.shareyourproxy.IntentLauncher.launchGooglePlusIntent;
import static com.shareyourproxy.IntentLauncher.launchHangoutsIntent;
import static com.shareyourproxy.IntentLauncher.launchInstagramIntent;
import static com.shareyourproxy.IntentLauncher.launchLinkedInIntent;
import static com.shareyourproxy.IntentLauncher.launchMediumIntent;
import static com.shareyourproxy.IntentLauncher.launchMeerkatIntent;
import static com.shareyourproxy.IntentLauncher.launchNintendoNetworkIntent;
import static com.shareyourproxy.IntentLauncher.launchPhoneIntent;
import static com.shareyourproxy.IntentLauncher.launchPlaystationNetworkIntent;
import static com.shareyourproxy.IntentLauncher.launchRedditIntent;
import static com.shareyourproxy.IntentLauncher.launchSMSIntent;
import static com.shareyourproxy.IntentLauncher.launchSkypeIntent;
import static com.shareyourproxy.IntentLauncher.launchSnapChatIntent;
import static com.shareyourproxy.IntentLauncher.launchSoundCloudIntent;
import static com.shareyourproxy.IntentLauncher.launchSpotifyIntent;
import static com.shareyourproxy.IntentLauncher.launchSteamIntent;
import static com.shareyourproxy.IntentLauncher.launchTumblrIntent;
import static com.shareyourproxy.IntentLauncher.launchTwitchIntent;
import static com.shareyourproxy.IntentLauncher.launchTwitterIntent;
import static com.shareyourproxy.IntentLauncher.launchVenmoIntent;
import static com.shareyourproxy.IntentLauncher.launchWebIntent;
import static com.shareyourproxy.IntentLauncher.launchWhatsAppIntent;
import static com.shareyourproxy.IntentLauncher.launchXboxLiveIntent;
import static com.shareyourproxy.IntentLauncher.launchYoIntent;
import static com.shareyourproxy.IntentLauncher.launchYoutubeIntent;
import static com.shareyourproxy.api.RestClient.getUserService;
import static com.shareyourproxy.api.rx.RxHelper.checkCompositeButton;
import static com.shareyourproxy.api.rx.RxQuery.getUserContactScore;
import static com.shareyourproxy.util.ViewUtils.getAlphaOverlayHierarchy;
import static com.shareyourproxy.util.ViewUtils.getUserImageHierarchy;
import static com.shareyourproxy.util.ViewUtils.getUserImageHierarchyNoFade;

/**
 * Display a User or a User Contact's Channels. Allow Users to edit their channels. Allow User
 * Contact's to be added to be observed and added to groups logged in user groups.
 */
public abstract class UserProfileFragment extends BaseFragment {
    @Bind(R.id.fragment_user_profile_toolbar)
    Toolbar toolbar;
    @Bind(R.id.fragment_user_profile_appbar)
    AppBarLayout appBarLayout;
    @Bind(R.id.fragment_user_profile_swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.fragment_user_profile_coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @Bind(R.id.fragment_user_profile_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.fragment_user_profile_header_image)
    SimpleDraweeView userImage;
    @Bind(R.id.fragment_user_profile_header_background)
    SimpleDraweeView userBackground;
    @Bind(R.id.fragment_user_profile_header_followers)
    TextView followersTextView;
    @BindColor(R.color.common_blue)
    int colorBlue;
    @BindDimen(R.dimen.common_svg_large)
    int svgLarge;
    @BindDimen(R.dimen.common_margin_huge)
    int svgHuge;
    @BindString(R.string.calculating)
    String stringCalculating;
    @BindString(R.string.error_calculating)
    String stringErrorCalculating;
    private CompositeSubscription _subscriptions;
    private RxGoogleAnalytics _analytics;
    private Channel _deletedChannel;
    private Palette.PaletteAsyncListener _paletteListener;
    private User _userContact;
    SwipeRefreshLayout.OnRefreshListener _refreshListener = new SwipeRefreshLayout
        .OnRefreshListener() {
        @Override
        public void onRefresh() {
            User user = getLoggedInUser();
            if (user != null) {
                getRxBus().post(new SyncContactsCommand(user));
            }
            getUserContactScore(getActivity(), _userContact.id())
                .subscribe(getContactScoreObserver());
        }
    };
    private UserChannelsFragment userChannelsFragment;
    private BasePostprocessor _postProcessor;


    /**
     * Get a click listener to add a deleted channel.
     *
     * @return click listener
     */
    private View.OnClickListener getAddChannelClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRxBus().post(new AddUserChannelCommand(getLoggedInUser(), _deletedChannel));
            }
        };
    }

    private AppBarLayout.OnOffsetChangedListener getOffsetListener() {
        return new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
                swipeRefreshLayout.setEnabled(offset == 0);
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        _userContact = getArguments().getParcelable(ARG_USER_SELECTED_PROFILE);
        String loggedInUserId = getArguments().getString(Constants.ARG_LOGGEDIN_USER_ID);
        checkLoggedInUserValue(loggedInUserId);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        ButterKnife.bind(this, rootView);
        onCreateView(rootView);
        return rootView;
    }

    void onCreateView(View rootView) {
        _analytics = RxGoogleAnalytics.getInstance(getActivity());
        followersTextView.setText(getString(R.string.user_profile_followers,
            stringCalculating));
        appBarLayout.addOnOffsetChangedListener(getOffsetListener());
        initializeSwipeRefresh(swipeRefreshLayout, _refreshListener);
        initializeUserChannels();
        //followers score
        getUserContactScore(getActivity(), _userContact.id())
            .subscribe(getContactScoreObserver());
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
        //if we're refreshing data, get rid of the UI
        swipeRefreshLayout.setRefreshing(false);
    }

    private void initializeUserChannels() {
        userChannelsFragment = UserChannelsFragment.newInstance(_userContact);
        getChildFragmentManager().beginTransaction()
            .replace(R.id.fragment_user_profile_user_channels,
                userChannelsFragment).commit();
    }

    /**
     * If we entered this activity fragment through a notification, make sure the logged in user has
     * a value.
     *
     * @param loggedInUserId logged in user id.
     */
    private void checkLoggedInUserValue(String loggedInUserId) {
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
                setLoggedInUser(getUserService(getActivity())
                    .getUser(loggedInUserId).toBlocking().single());
            }
        }
    }

    private void showDeletedChannelSnackBar(CoordinatorLayout coordinatorLayout) {
        Snackbar snackbar = make(coordinatorLayout, getString(R.string.undo_delete),
            LENGTH_INDEFINITE);
        snackbar.setAction(getString(R.string.undo), getAddChannelClickListener());
        snackbar.setActionTextColor(colorBlue);
        snackbar.show();
    }


    private JustObserver<Object> onNextEvent() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof GroupContactsUpdatedEventCallback) {
                    groupContactsUpdatedEvent((GroupContactsUpdatedEventCallback) event);
                } else if (event instanceof UserChannelAddedEventCallback) {
                    addUserChannel(((UserChannelAddedEventCallback) event));
                } else if (event instanceof UserChannelDeletedEventCallback) {
                    deleteUserChannel(((UserChannelDeletedEventCallback) event));
                } else if (event instanceof SyncContactsCommand) {
                    swipeRefreshLayout.setRefreshing(true);
                } else if (event instanceof SyncAllContactsSuccessEvent) {
                    swipeRefreshLayout.setRefreshing(false);
                } else if (event instanceof SyncAllContactsErrorEvent) {
                    swipeRefreshLayout.setRefreshing(false);
                } else if (event instanceof SelectUserChannelEvent) {
                    onChannelSelected((SelectUserChannelEvent) event);
                }
            }
        };
    }

    private void groupContactsUpdatedEvent(GroupContactsUpdatedEventCallback event) {
        if (event.contactGroups.size() > 0) {
            _analytics.userContactAdded(event.user);
        } else {
            _analytics.userContactRemoved(event.user);
        }
        getUserContactScore(getActivity(), _userContact.id())
            .subscribe(getContactScoreObserver());
    }

    private void deleteUserChannel(UserChannelDeletedEventCallback event) {
        _deletedChannel = event.channel;
        showDeletedChannelSnackBar(coordinatorLayout);
    }

    private void addUserChannel(UserChannelAddedEventCallback event) {
        if (event.oldChannel != null) {
            showChangesSavedSnackBar(coordinatorLayout);
        }
    }

    /**
     * Handle channel selected events to launch the correct android process.
     *
     * @param event data
     */
    void onChannelSelected(SelectUserChannelEvent event) {
        ChannelType channelType = event.channel.channelType();
        String actionAddress = event.channel.actionAddress();
        switch (channelType) {
            case Phone:
                launchPhoneIntent(getActivity(), actionAddress);
                break;
            case SMS:
                launchSMSIntent(getActivity(), actionAddress);
                break;
            case Email:
                launchEmailIntent(getActivity(), actionAddress);
                break;
            case Web:
            case URL:
                launchWebIntent(getActivity(), actionAddress);
                break;
            case Facebook:
                launchFacebookIntent(getActivity(), actionAddress);
                break;
            case Twitter:
                launchTwitterIntent(getActivity(), actionAddress);
                break;
            case Meerkat:
                launchMeerkatIntent(getActivity(), actionAddress);
                break;
            case Snapchat:
                launchSnapChatIntent(getActivity(), actionAddress);
                break;
            case Spotify:
                launchSpotifyIntent(getActivity(), actionAddress);
                break;
            case Reddit:
                launchRedditIntent(getActivity(), actionAddress);
                break;
            case Linkedin:
                launchLinkedInIntent(getActivity(), actionAddress);
                break;
            case FBMessenger:
                launchFBMessengerIntent(getActivity(), actionAddress);
                break;
            case Googleplus:
                launchGooglePlusIntent(getActivity(), actionAddress);
                break;
            case Github:
                launchGithubIntent(getActivity(), actionAddress);
                break;
            case Address:
                launchAddressIntent(getActivity(), actionAddress);
                break;
            case Youtube:
                launchYoutubeIntent(getActivity(), actionAddress);
                break;
            case Instagram:
                launchInstagramIntent(getActivity(), actionAddress);
                break;
            case Tumblr:
                launchTumblrIntent(getActivity(), actionAddress);
                break;
            case Ello:
                launchElloIntent(getActivity(), actionAddress);
                break;
            case Venmo:
                launchVenmoIntent(getActivity(), actionAddress);
                break;
            case Medium:
                launchMediumIntent(getActivity(), actionAddress);
                break;
            case Soundcloud:
                launchSoundCloudIntent(getActivity(), actionAddress);
                break;
            case Skype:
                launchSkypeIntent(getActivity(), actionAddress);
                break;
            case Yo:
                launchYoIntent(getActivity(), actionAddress);
                break;
            case Custom:
                break;
            case Slack:
                break;
            case Hangouts:
                launchHangoutsIntent(getActivity(), actionAddress);
                break;
            case Whatsapp:
                launchWhatsAppIntent(getActivity(), actionAddress);
                break;
            case Periscope:
                break;
            case PlaystationNetwork:
                launchPlaystationNetworkIntent(getActivity(), actionAddress);
                break;
            case NintendoNetwork:
                launchNintendoNetworkIntent(getActivity(), actionAddress);
                break;
            case Steam:
                launchSteamIntent(getActivity(), actionAddress);
                break;
            case Twitch:
                launchTwitchIntent(getActivity(), actionAddress);
                break;
            case XboxLive:
                launchXboxLiveIntent(getActivity(), actionAddress);
                break;
            case LeagueOfLegends:
                // league is specifically a static profile and goes no where.
            default:
                break;
        }
    }

    /**
     * Async returns when palette has been loaded.
     *
     * @return palette listener
     */
    private Palette.PaletteAsyncListener getPaletteAsyncListener() {
        if (_paletteListener == null) {
            _paletteListener = new Palette.PaletteAsyncListener() {
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
     * Initialize the Header view data and state.
     */
    void initializeHeader() {
        //update profile user image
        String profileURL = _userContact.profileURL();
        if (this instanceof MainUserProfileFragment) {
            userImage.setHierarchy(getUserImageHierarchy(getActivity()));
        } else {
            userImage.setHierarchy(getUserImageHierarchyNoFade(getActivity()));
        }

        if (profileURL != null) {
            ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(profileURL))
                .setPostprocessor(getPaletteProcessor())
                .build();
            userImage.setController(newDraweeControllerBuilder()
                .setImageRequest(request)
                .build());
        }

        //update profile background
        String coverURL = _userContact.coverURL();
        userBackground.setHierarchy(getAlphaOverlayHierarchy(
            collapsingToolbarLayout, getResources()));
        userBackground.setController(newDraweeControllerBuilder()
            .setUri(coverURL == null ? null : Uri.parse(coverURL))
            .setAutoPlayAnimations(true)
            .build());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        userChannelsFragment.onActivityResult(requestCode, resultCode, data);
    }

    private BasePostprocessor getPaletteProcessor() {
        if (_postProcessor == null) {
            _postProcessor = new BasePostprocessor() {

                @Override
                public void process(Bitmap bitmap) {
                    new Palette.Builder(bitmap).generate(getPaletteAsyncListener());
                }

                @Override
                public String getName() {
                    return "datPostProcessor";
                }

            };
        }
        return _postProcessor;
    }

    User getContact() {
        return _userContact;
    }

    private JustObserver<Integer> getContactScoreObserver() {
        return new JustObserver<Integer>() {
            @Override
            public void next(Integer integer) {
                if (getActivity() != null) {
                    followersTextView.setText(getString(R.string.user_profile_followers,
                        integer));
                }
            }

            @Override
            public void error(Throwable e) {
                //TODO: sometimes this returns before the parent activity is attached
                if (getActivity() != null) {
                    followersTextView.setText(getString(R.string.user_profile_followers,
                        stringErrorCalculating));
                }
            }
        };
    }
}
