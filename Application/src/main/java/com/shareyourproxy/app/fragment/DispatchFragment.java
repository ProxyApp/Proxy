package com.shareyourproxy.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.app.MainActivity;
import com.shareyourproxy.util.ViewUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.shareyourproxy.util.ViewUtils.dpToPx;

/**
 * Dispatch Fragment to handle dispatching a {@link com.shareyourproxy.app.LoginActivity} or a {@link
 * MainActivity} base off the current user.
 */
public class DispatchFragment extends BaseFragment {
    public static final int HOLD_ON_A_SECOND = 1000;
    @InjectView(R.id.fragment_dispatch_image)
    ImageView imageView;

    Runnable loginRunnable = new Runnable() {
        @Override
        public void run() {
            IntentLauncher.launchLoginActivity(getActivity(), false);
            getActivity().finish();
        }
    };

    /**
     * Constructor.
     */
    public DispatchFragment() {
    }

    /**
     * Return new Fragment instance.
     *
     * @return fragment
     */
    public static DispatchFragment newInstance() {
        return new DispatchFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dispatch, container, false);
        ButterKnife.inject(this, rootView);
        drawLogo();
        rootView.postDelayed(loginRunnable, HOLD_ON_A_SECOND);
        return rootView;
    }

    /**
     * Set the Logo drawable on this activities {@link ImageView}.
     */
    private void drawLogo() {
        ViewCompat.setLayerType(imageView, ViewCompat.LAYER_TYPE_SOFTWARE, null);
        ViewCompat.setElevation(imageView, getElevation());

        imageView.setImageDrawable(ViewUtils.svgToBitmapDrawable(getActivity(),
            R.raw.ic_proxy_logo, (int) getResourceDimension(getActivity())));
    }

    /**
     * Get a big icon dimension size.
     *
     * @param activity context
     * @return resource dimension
     */
    private float getResourceDimension(Activity activity) {
        return dpToPx(activity.getResources(), R.dimen.common_svg_ultra);
    }

    /**
     * Get the elevation resource for {@link com.melnykov.fab.FloatingActionButton}.
     *
     * @return diemnsion of elevation
     */
    private float getElevation() {
        return dpToPx(getActivity().getResources(), R.dimen.common_fab_elevation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
