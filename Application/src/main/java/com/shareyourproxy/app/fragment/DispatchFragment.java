package com.shareyourproxy.app.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.app.MainActivity;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static android.support.v4.graphics.drawable.DrawableCompat.setTintList;
import static android.support.v4.graphics.drawable.DrawableCompat.setTintMode;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Handle dispatching a {@link com.shareyourproxy.app.LoginActivity} or a {@link MainActivity} base
 * off the current user.
 */
public class DispatchFragment extends BaseFragment {
    @BindDimen(R.dimen.common_svg_ultra)
    int logoSize;
    @Bind(R.id.fragment_dispatch_image)
    TextView textView;
    @Bind(R.id.fragment_dispatch_progress)
    ProgressBar progressBar;
    @BindColor(android.R.color.white)
    ColorStateList colorWhite;

    /**
     * Constructor.
     */
    public DispatchFragment() {
    }

    /**
     * Return new Fragment instance.
     *
     * @return layouts.fragment
     */
    public static DispatchFragment newInstance() {
        return new DispatchFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dispatch, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    public void initialize() {
        drawLogo();
        tintProgressBar();
    }

    public void tintProgressBar() {
        setTintMode(progressBar.getIndeterminateDrawable(), SRC_ATOP);
        setTintList(progressBar.getIndeterminateDrawable(), colorWhite);
    }

    /**
     * Set the Logo image.drawable on this activities {@link ImageView}.
     */
    private void drawLogo() {
        textView.setCompoundDrawablesWithIntrinsicBounds(null, svgToBitmapDrawable(getActivity(),
            R.raw.ic_doge_channels, logoSize), null, null);
    }
}
