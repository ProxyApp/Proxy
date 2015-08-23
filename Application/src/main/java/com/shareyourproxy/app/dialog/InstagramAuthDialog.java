package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
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
import com.shareyourproxy.api.domain.model.InstagramAuthResponse;
import com.shareyourproxy.api.domain.model.InstagramUser;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.util.DebugUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.util.Log.getStackTraceString;
import static com.shareyourproxy.BuildConfig.INSTAGRAM_APP_ID;
import static com.shareyourproxy.BuildConfig.INSTAGRAM_APP_SECRET;
import static com.shareyourproxy.BuildConfig.WEBVIEW_REDIRECT;

/**
 * Created by Evan on 8/12/15.
 */
public class InstagramAuthDialog extends BaseDialogFragment {
    private static final String TAG = DebugUtils.getSimpleName(InstagramAuthDialog.class);
    @Bind(R.id.dialog_webview_container)
    protected WebView webView;

    public static InstagramAuthDialog newInstance() {
        Bundle args = new Bundle();
        InstagramAuthDialog fragment = new InstagramAuthDialog();
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
                final String fragment = uri.getEncodedQuery();
                // if the uri contains the access token, acquire it
                if (fragment != null && fragment.startsWith("code=")) {
                    webView.stopLoading();
                    Timber.i(fragment);
                    final String[] parts1 = fragment.split("code=");
                    final String accessToken = parts1[1];

                    RestClient.getInstagramAuthService()
                        .getAuth(INSTAGRAM_APP_ID, INSTAGRAM_APP_SECRET,
                            "authorization_code", WEBVIEW_REDIRECT, accessToken)
                        .subscribe(authObserver());
                }
            }

        });

        try {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setSaveFormData(false);
            webView.loadUrl(InstagramUser.requestOAuthUrl(
                INSTAGRAM_APP_ID, WEBVIEW_REDIRECT));
        } catch (final Exception e) {
            Timber.e(getStackTraceString(e));
        }
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        //Override the dialog wrapping content and cancel dismiss on click outside
        // of the dialog window
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public JustObserver<InstagramAuthResponse> authObserver() {
        return new JustObserver<InstagramAuthResponse>() {

            @Override
            public void onError() {

            }

            @Override
            public void onNext(InstagramAuthResponse event) {
                Timber.i(event.toString());
                InstagramUser user = event.user();
                Channel channel = ChannelFactory.createModelInstance(user.id(), ChannelType
                        .Instagram.toString(),
                    ChannelType.Instagram, user.username());
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
    public InstagramAuthDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }
}
