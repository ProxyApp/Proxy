package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shareyourproxy.R;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.factory.ChannelFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.SpotifyAuthResponse;
import com.shareyourproxy.api.domain.model.SpotifyUser;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.util.DebugUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.util.Log.getStackTraceString;
import static com.shareyourproxy.BuildConfig.SPOTIFY_CLIENT_ID;
import static com.shareyourproxy.BuildConfig.SPOTIFY_CLIENT_SECRET;
import static com.shareyourproxy.BuildConfig.WEBVIEW_REDIRECT;

/**
 * Created by Evan on 8/14/15.
 */
public class SpotifyAuthDialog extends BaseDialogFragment {

    /**
     * Created by Evan on 8/12/15.
     */
    private static final String TAG = DebugUtils.getSimpleName(SpotifyAuthDialog.class);
    @Bind(R.id.dialog_webview_container)
    protected WebView webView;

    public static SpotifyAuthDialog newInstance() {
        Bundle args = new Bundle();
        SpotifyAuthDialog fragment = new SpotifyAuthDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_webview, null, false);
        ButterKnife.bind(this, view);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                return false;
            }

            @Override
            public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
                final Uri uri = Uri.parse(url);
                final String uriValue = uri.toString();
                final String fragment = uri.getEncodedQuery();
                // if the uri contains the access token, acquire it
                if (uriValue.startsWith("https://www.shareyourproxy.com")) {
                    if (fragment != null && fragment.startsWith("code=")) {
                        Timber.i(fragment);
                        final String[] parts1 = fragment.split("code=");
                        final String accessToken = parts1[1];
                        String auth = SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET;
                        String header = new String(Base64.encode(auth.getBytes(), Base64.DEFAULT));

                        RestClient.getSpotifyAuthService()
                            .getAuth(SPOTIFY_CLIENT_ID,SPOTIFY_CLIENT_SECRET, "authorization_code", accessToken, WEBVIEW_REDIRECT)
                            .subscribe(authObserver());
                    }
                }
            }

        });

        try {
            String url = SpotifyUser.requestOAuthUrl(
                SPOTIFY_CLIENT_ID, WEBVIEW_REDIRECT);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setSaveFormData(false);
            webView.loadUrl(url);
//            IntentLauncher.launchWebIntent(getActivity(), url);
        } catch (final Exception e) {
            Timber.e(getStackTraceString(e));
        }
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        //Override the dialog wrapping content and cancel dismiss on click outside
        // of the dialog window
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().getAttributes().height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public JustObserver<SpotifyAuthResponse> authObserver() {
        return new JustObserver<SpotifyAuthResponse>() {
            @Override
            public void onError() {

            }

            @Override
            public void onNext(SpotifyAuthResponse event) {
                Timber.i(event.toString());
                RestClient.getSpotifyUserService().getUser(event.access_token())
                    .subscribe(getUserObserver());
            }
        };
    }

    public JustObserver<SpotifyUser> getUserObserver() {
        return new JustObserver<SpotifyUser>() {
            @Override
            public void onError() {

            }

            @Override
            public void onNext(SpotifyUser event) {
                Timber.i(event.toString());
                Channel channel = ChannelFactory.createModelInstance(event.id(), ChannelType
                        .Spotify.toString(),
                    ChannelType.Spotify, event.id());
                getRxBus().post(new AddUserChannelCommand(getLoggedInUser(), channel));
                getDialog().dismiss();
            }
        };
    }

    /**
     * Use the private string TAG from this class as an identifier.
     *
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    public SpotifyAuthDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }
}

