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
 * Third introduction slide content.
 */
public class ThirdIntroductionFragment extends BaseIntroductionFragment {
    @BindDimen(R.dimen.common_svg_xxlarge)
    int logoSize;
    @BindString(R.string.slide_three_title)
    String introTitle;
    @BindString(R.string.slide_three_body)
    String introBody;
    @Bind(R.id.fragment_introduction_third_imageview)
    ImageView imageView;
    @Bind(R.id.fragment_introduction_third_textview)
    TextView textView;

    /**
     * Default Constructor.
     */
    public ThirdIntroductionFragment() {
    }

    public static ThirdIntroductionFragment newInstance() {
        return new ThirdIntroductionFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_introduction_third, container, false);
        ButterKnife.bind(this, rootView);
        drawSlide(getActivity(), imageView, textView, R.raw.ic_guide_activity_slide3, logoSize,
            introTitle, introBody);
        return rootView;
    }

}
