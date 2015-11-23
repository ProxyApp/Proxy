package com.shareyourproxy.app.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent;
import com.shareyourproxy.app.UserContactActivity;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.ViewChannelAdapter;
import com.shareyourproxy.app.dialog.ShareLinkDialog;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.OnClick;
import rx.functions.Action1;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.shareyourproxy.Constants.ARG_LOGGEDIN_USER_ID;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.IntentLauncher.launchChannelListActivity;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Display the logged in users profile and channels.
 */
public class MainUserProfileFragment extends UserProfileFragment {
    @Bind(R.id.fragment_user_profile_header_title)
    TextView titleTextView;
    @Bind(R.id.fragment_user_profile_fab_add_channel)
    FloatingActionButton floatingActionButtonAddChannel;
    @Bind(R.id.fragment_user_profile_fab_share)
    FloatingActionButton floatingActionButtonShare;
    @BindColor(android.R.color.white)
    int colorWhite;
    @BindDimen(R.dimen.fragment_userprofile_header_user_background_size)
    int marginUserHeight;

    /**
     * Empty Fragment Constructor.
     */
    public MainUserProfileFragment() {
    }

    /**
     * Return new instance for parent {@link UserContactActivity}.
     *
     * @return layouts.fragment
     */
    public static MainUserProfileFragment newInstance(User contact, String loggedInUserId) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_USER_SELECTED_PROFILE, contact);
        bundle.putString(ARG_LOGGEDIN_USER_ID, loggedInUserId);
        MainUserProfileFragment fragment = new MainUserProfileFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fragment_user_profile_fab_add_channel)
    public void onClickAdd() {
        launchChannelListActivity(getActivity());
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fragment_user_profile_fab_share)
    public void onClickShare() {
        ShareLinkDialog.newInstance(getLoggedInUser().groups())
            .show(getActivity().getSupportFragmentManager());
    }

    @Override
    void onCreateView(View rootView) {
        super.onCreateView(rootView);
        initialize();
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        setHeaderHeight();
        initializeFabPlusIcon();
        setToolbarTitle();
        initializeHeader();
    }

    private void setHeaderHeight() {
        ViewGroup.LayoutParams lp = collapsingToolbarLayout.getLayoutParams();
        lp.height = marginUserHeight;
        collapsingToolbarLayout.setLayoutParams(lp);
    }

    /**
     * Set the content image of this {@link FloatingActionButton}
     */
    private void initializeFabPlusIcon() {
        Drawable plus = svgToBitmapDrawable(
            getActivity(), R.raw.ic_add, svgLarge, colorWhite);
        floatingActionButtonAddChannel.setImageDrawable(plus);

        Drawable share = svgToBitmapDrawable(
            getActivity(), R.raw.ic_share, svgLarge, colorBlue);
        floatingActionButtonShare.setImageDrawable(share);
        floatingActionButtonAddChannel.setVisibility(VISIBLE);
        floatingActionButtonShare.setVisibility(VISIBLE);
    }

    private void setToolbarTitle() {
        String title = getLoggedInUser().fullName();
        titleTextView.setVisibility(VISIBLE);
        titleTextView.setText(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setVisibility(GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getRxBus().toObservable().subscribe(onNextEvent());
    }


    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof RecyclerViewDatasetChangedEvent) {
                    toggleFabVisibility((RecyclerViewDatasetChangedEvent) event);
                }
            }
        };
    }

    public void toggleFabVisibility(RecyclerViewDatasetChangedEvent event) {
        if (event.adapter instanceof ViewChannelAdapter) {
            if (event.viewState.equals
                (BaseRecyclerView.ViewState.EMPTY)) {
                floatingActionButtonAddChannel.setVisibility(GONE);
                floatingActionButtonShare.setVisibility(GONE);
            } else {
                floatingActionButtonAddChannel.setVisibility(VISIBLE);
                floatingActionButtonShare.setVisibility(VISIBLE);
            }
        }
    }

}
