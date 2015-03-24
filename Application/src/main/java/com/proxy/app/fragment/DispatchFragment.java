package com.proxy.app.fragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.proxy.IntentLauncher;
import com.proxy.R;
import com.proxy.event.OttoBusDriver;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

import static com.proxy.util.DebugUtils.getSimpleName;
import static com.proxy.util.ViewUtils.dpToPx;

/**
 * Dispatch Fragment to handle dispatching a {@link com.proxy.app.LoginActivity} or a {@link
 * com.proxy.app.ContactsActivity} base off the current user.
 */
public class DispatchFragment extends BaseFragment {
    public static final int HOLD_ON_2_SECONDS = 2000;
    @InjectView(R.id.fragment_dispatch_image)
    ImageView mImageView;
    private float mImageWidth;
    private static final String TAG = getSimpleName(DispatchFragment.class);

    /**
     * Return new Fragment instance.
     *
     * @return fragment
     */
    public static DispatchFragment newInstance() {
        return new DispatchFragment();
    }

    /**
     * Constructor.
     */
    public DispatchFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mImageWidth = getResourceDimension(getActivity());
        OttoBusDriver.register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        OttoBusDriver.unregister(this);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dispatch, container, false);
        ButterKnife.inject(this, rootView);
        drawLogo();
        rootView.postDelayed(loginRunnable, HOLD_ON_2_SECONDS);
        return rootView;
    }

    Runnable loginRunnable = new Runnable() {
        @Override
        public void run() {
            IntentLauncher.launchLoginActivity(getActivity(), false);
            getActivity().finish();
        }
    };

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
     * Set the Logo drawable on this activities {@link ImageView}.
     */
    private void drawLogo() {
        ViewCompat.setLayerType(mImageView, ViewCompat.LAYER_TYPE_SOFTWARE, null);
        try {
            SVG svg = SVG.getFromResource(getActivity(), R.raw.proxy_logo);
            svg.setDocumentWidth(mImageWidth);
            svg.setDocumentHeight(mImageWidth);
            Drawable drawable = new PictureDrawable(svg.renderToPicture());
            mImageView.setImageDrawable(drawable);
        } catch (SVGParseException e) {
            Timber.e(e, TAG + "initializeSVG()");
        }
        ViewCompat.setElevation(mImageView, getElevation());
    }

    /**
     * Get the elevation resource for {@link com.proxy.widget.FloatingActionButton}.
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
