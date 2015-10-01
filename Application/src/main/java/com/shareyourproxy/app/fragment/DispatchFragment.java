package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shareyourproxy.R;
import com.shareyourproxy.app.MainActivity;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;

import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Handle dispatching a {@link com.shareyourproxy.app.LoginActivity} or a {@link MainActivity} base
 * off the current user.
 */
public class DispatchFragment extends BaseFragment {
    @BindDimen(R.dimen.common_svg_xlarge)
    protected int logoSize;
    @Bind(R.id.fragment_dispatch_image)
    ImageView imageView;

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
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dispatch, container, false);
        ButterKnife.bind(this, rootView);
        drawLogo();
        return rootView;
    }

    /**
     * Set the Logo image.drawable on this activities {@link ImageView}.
     */
    private void drawLogo() {
        imageView.setImageDrawable(svgToBitmapDrawable(getActivity(),
            R.raw.ic_proxy_logo, logoSize));
    }
}
