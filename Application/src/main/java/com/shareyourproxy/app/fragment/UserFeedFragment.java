package com.shareyourproxy.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxActivityFeedSync;
import com.shareyourproxy.api.rx.command.eventcallback.ActivityFeedDownloadedEvent;
import com.shareyourproxy.app.adapter.ActivityFeedAdapter;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.app.dialog.ErrorDialog;
import com.shareyourproxy.widget.ContentDescriptionDrawable;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Response;
import retrofit.Retrofit;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.api.rx.RxHelper.checkCompositeButton;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Created by Evan on 10/10/15.
 */
public class UserFeedFragment extends BaseFragment implements ItemClickListener {
    @Bind(R.id.fragment_user_feed_recyclerview)
    BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_user_feed_empty_textview)
    TextView emptyTextView;
    @BindString(R.string.fragment_userfeed_empty_title)
    String loggedInNullTitle;
    @BindString(R.string.fragment_userfeed_empty_message)
    String stringNullMessage;
    @BindString(R.string.fragment_userprofile_contact_empty_title)
    String contactNullTitle;
    @BindDimen(R.dimen.common_svg_null_screen_small)
    int marginNullScreen;
    @BindString(R.string.twitter_login_error)
    String twitterLoginError;
    @BindString(R.string.twitter_login_error_message)
    String twitterLoginErrorMessage;
    private boolean _isLoggedInUser;
    private User _userContact;
    private CompositeSubscription _subscriptions;
    private ActivityFeedAdapter _adapter;
    private TwitterLoginButton twitterLoginButton;
    private int _lastClickedAuthItem;

    /**
     * Constructor.
     */
    public UserFeedFragment() {
    }

    /**
     * Create a new user activity feed fragment.
     *
     * @return user activity feed fragment.
     */
    public static UserFeedFragment newInstance() {
        return new UserFeedFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_user_feed, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    public void getUserFeed(TwitterSession session) {
        _subscriptions = checkCompositeButton(_subscriptions);
        _subscriptions.add(RxActivityFeedSync.getInstance()
            .getChannelFeed(getActivity(), session, _userContact.channels())
            .subscribe(ActivityFeedObserver()));
    }

    public JustObserver<ActivityFeedDownloadedEvent> ActivityFeedObserver() {
        return new JustObserver<ActivityFeedDownloadedEvent>() {
            @Override
            public void next(ActivityFeedDownloadedEvent event) {
                activityFeedDownloaded(event);
            }
        };
    }

    /**
     * Initialize a twitter login button with a callback to handle errors.
     */
    private void initializeTwitterLogin() {
        twitterLoginButton = new TwitterLoginButton(getActivity());
        twitterLoginButton.setVisibility(View.GONE);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void onResponse(Response<TwitterSession> response, Retrofit retrofit) {

            }

            @Override
            public void onFailure(Throwable t) {

            }

            @Override
            public void success(Result<TwitterSession> result) {
                Twitter.getSessionManager().setActiveSession(result.data);
                _adapter.removeItem(_lastClickedAuthItem);
                getUserFeed(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Timber.e(Log.getStackTraceString(exception));
                ErrorDialog.newInstance(twitterLoginError,
                    twitterLoginErrorMessage).show(getActivity().getSupportFragmentManager());
            }
        });
    }

    private void activityFeedDownloaded(ActivityFeedDownloadedEvent event) {
        _adapter.refreshFeedData(event.feedItems);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = checkCompositeButton(_subscriptions);
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeTwitterLogin();
        initializeRecyclerView();
        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        getUserFeed(session);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        initializeEmptyView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = ActivityFeedAdapter.newInstance(recyclerView, _userContact, this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void initializeEmptyView() {
        Context context = getContext();
        if (_isLoggedInUser) {
            SpannableStringBuilder sb = new SpannableStringBuilder(loggedInNullTitle).append("\n")
                .append(stringNullMessage);

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
                0, loggedInNullTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
                loggedInNullTitle.length() + 1, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            emptyTextView.setText(sb);
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
    public void onItemClick(View view, int position) {
        switch (_adapter.getItemViewType(position)) {
            case ActivityFeedAdapter.VIEWTYPE_HEADER:
                _lastClickedAuthItem = position;
                switch (_adapter.getItemData(position).channelType()) {
                    case Twitter:
                        twitterLoginButton.performClick();
                        break;
                }
                break;
            case ActivityFeedAdapter.VIEWTYPE_CONTENT:
                String url = _adapter.getItemData(position).actionAddress();
                IntentLauncher.launchWebIntent(getActivity(), url);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }
}
