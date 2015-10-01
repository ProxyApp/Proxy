package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.R;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Second introduction slide content.
 */
public class SecondIntroductionFragment extends BaseIntroductionFragment {
    @BindDimen(R.dimen.common_svg_xlarge)
    int logoSize;
    @BindString(R.string.slide_two_title)
    String introTitle;
    @BindString(R.string.slide_two_body)
    String introBody;
    @Bind(R.id.fragment_introduction_second_imageview)
    ImageView imageView;
    @Bind(R.id.fragment_introduction_second_textview)
    TextView textView;

    /**
     * Default Constructor.
     */
    public SecondIntroductionFragment() {
    }

    public static SecondIntroductionFragment newInstance() {
        return new SecondIntroductionFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_introduction_second, container, false);
        ButterKnife.bind(this, rootView);
        drawSlide(getActivity(), imageView, textView, R.raw.ic_guide_activity_slide2, logoSize,
            introTitle, introBody);
        return rootView;
    }

}
