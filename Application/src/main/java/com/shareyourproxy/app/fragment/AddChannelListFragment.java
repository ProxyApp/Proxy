package com.shareyourproxy.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.app.adapter.AddChannelAdapter;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.dialog.AddAuthChannelDialog;
import com.shareyourproxy.app.dialog.AddChannelDialog;
import com.shareyourproxy.app.dialog.AddRedditChannelDialog;
import com.shareyourproxy.app.dialog.ErrorDialog;
import com.shareyourproxy.app.dialog.InstagramAuthDialog;
import com.shareyourproxy.app.dialog.SpotifyAuthDialog;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.UUID;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

/**
 * Display a list of channel types for the user to add new channel information to their profile.
 */
public class AddChannelListFragment extends BaseFragment implements ItemClickListener {

    @Bind(R.id.fragment_channel_list_recyclerview)
    BaseRecyclerView recyclerView;
    @BindString(R.string.twitter_login_error)
    String twitterLoginError;
    @BindString(R.string.twitter_login_error_message)
    String twitterLoginErrorMessage;
    private TwitterLoginButton twitterLoginButton;
    private AddChannelAdapter _adapter;
    private CallbackManager _callbackManager;
    private LoginManager _loginManager;
    private Channel _clickedChannel;
    private FacebookCallback<LoginResult> _fbLoginCallback = getFBCallback();

    /**
     * Constructor.
     */
    public AddChannelListFragment() {
    }

    /**
     * Return new fragment instance.
     *
     * @return AddChannelListFragment
     */
    public static AddChannelListFragment newInstance() {
        return new AddChannelListFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_channellist, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize the facebook {@link ChannelType} callback manager.
     */
    public void initializeFaceBookLogin() {
        _callbackManager = CallbackManager.Factory.create();
        _loginManager = LoginManager.getInstance();
        _loginManager.registerCallback(_callbackManager, _fbLoginCallback);
    }

    /**
     * Callback manager to handle next or error when OAuthing a facebook user.
     *
     * @return callback manager
     */
    private FacebookCallback<LoginResult> getFBCallback() {
        return new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                            JSONObject object, GraphResponse response) {
                            try {
                                String id = object.getString("id");
                                Channel channel = createModelInstance(
                                    id, "", _clickedChannel.channelType(), "");

                                AddAuthChannelDialog
                                    .newInstance(channel).show(getFragmentManager());
                            } catch (JSONException e) {
                                Timber.e(Log.getStackTraceString(e));
                            }
                        }
                    });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Timber.i("Facebook Log In Cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                Timber.e("Facebook LogIn Failed: %1$s", exception.getMessage());
            }
        };
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeFaceBookLogin();
        initializeTwitterLogin();
        initializeRecyclerView();
    }

    /**
     * Initialize a twitter login button with a callback to handle errors.
     */
    private void initializeTwitterLogin() {
        twitterLoginButton = new TwitterLoginButton(getActivity());
        twitterLoginButton.setVisibility(View.GONE);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Twitter.getSessionManager().setActiveSession(result.data);
                String id = String.valueOf(result.data.getUserId());
                String handle = result.data.getUserName();

                Channel channel = createModelInstance(
                    id, "", _clickedChannel.channelType(), handle);
                getRxBus().post(new AddUserChannelCommand(getLoggedInUser(), channel));
            }

            @Override
            public void failure(TwitterException exception) {
                Timber.e(Log.getStackTraceString(exception));
                ErrorDialog.newInstance(twitterLoginError,
                    twitterLoginErrorMessage).show(getActivity().getSupportFragmentManager());
            }

            @Override
            public void onResponse(Response<TwitterSession> response, Retrofit retrofit) {
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    /**
     * Initialize a recyclerView with {@link Channel} data.
     */
    private void initializeRecyclerView() {
        _adapter = AddChannelAdapter.newInstance(recyclerView, getSharedPreferences(), this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(_adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        _clickedChannel = _adapter.getItemData(position);
        ChannelType channelType = _clickedChannel.channelType();
        switch (channelType) {
            case Custom:
            case Phone:
            case SMS:
            case Email:
            case Web:
            case URL:
            case Meerkat:
            case Snapchat:
            case Linkedin:
            case FBMessenger:
            case Hangouts:
            case Whatsapp:
            case Yo:
            case Googleplus:
            case Github:
            case Address:
            case Slack:
            case Youtube:
            case PlaystationNetwork:
            case NintendoNetwork:
            case Steam:
            case Twitch:
            case LeagueOfLegends:
            case XboxLive:
            case Tumblr:
            case Ello:
            case Venmo:
            case Periscope:
            case Medium:
            case Soundcloud:
            case Skype:
                AddChannelDialog.newInstance(
                    _clickedChannel.channelType())
                    .show(getActivity().getSupportFragmentManager());
                break;
            case Facebook:
                _loginManager.logInWithReadPermissions(
                    this, Arrays.asList("public_profile", "user_friends"));
                break;
            case Twitter:
                try {
                    getActivity().getPackageManager().getPackageInfo("com.twitter.android", 0);
                    twitterLoginButton.performClick();
                } catch (Exception e) {
                    Channel channel = createModelInstance(UUID.randomUUID().toString(),
                        "", _clickedChannel.channelType(), "");
                    AddAuthChannelDialog.newInstance(channel)
                        .show(getActivity().getSupportFragmentManager());
                }
                break;
            case Reddit:
                AddRedditChannelDialog.newInstance(_clickedChannel.channelType())
                    .show(getActivity().getSupportFragmentManager());
                break;
            case Instagram:
                InstagramAuthDialog.newInstance().show(getFragmentManager());
                break;
            case Spotify:
                SpotifyAuthDialog.newInstance().show(getFragmentManager());
//                AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder
// (CLIENT_ID,
//                    AuthenticationResponse.Type.TOKEN,
//                    REDIRECT_URI);
//                builder.setScopes(new String[]{"user-read-private", "streaming"})
//                AuthenticationRequest request = builder.build();
//
//                AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

}
