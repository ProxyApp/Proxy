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
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.ChannelAdapter;
import com.shareyourproxy.app.dialog.AddChannelDialog;
import com.shareyourproxy.app.dialog.AddFacebookChannelDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

/**
 * Display a list of newChannel types for the user to add new newChannel information.
 */
public class AddChannelListFragment extends BaseFragment implements ItemClickListener {

    @Bind(R.id.fragment_channel_list_recyclerview)
    protected BaseRecyclerView recyclerView;
    private ChannelAdapter _adapter;
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
     * Return new Fragment instance.
     *
     * @return layouts.fragment
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

    public void initializeFaceBookLogin() {
        _callbackManager = CallbackManager.Factory.create();
        _loginManager = LoginManager.getInstance();
        _loginManager.registerCallback(_callbackManager, _fbLoginCallback);
    }

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
                                    id, getString(R.string.facebook), _clickedChannel.channelType(),
                                    _clickedChannel.channelSection(), "");

                                AddFacebookChannelDialog
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
                Timber.e("Facebook LogIn Failed: " + exception.getMessage());
            }
        };
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeFaceBookLogin();
        initializeRecyclerView();
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = ChannelAdapter.newInstance(this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onItemClick(View view, int position) {
        _clickedChannel = _adapter.getItemData(position);
        ChannelType channelType = _clickedChannel.channelType();

        if (channelType.equals(ChannelType.Facebook)) {
            _loginManager.logInWithReadPermissions(
                this, Arrays.asList("public_profile", "user_friends"));
        } else {
            AddChannelDialog.newInstance(
                _clickedChannel.channelType(), _clickedChannel.channelSection())
                .show(getActivity().getSupportFragmentManager());
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
