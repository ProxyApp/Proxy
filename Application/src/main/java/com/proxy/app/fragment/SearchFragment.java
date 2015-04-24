package com.proxy.app.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.proxy.R;
import com.proxy.api.model.Contact;
import com.proxy.app.SearchActivity;
import com.proxy.event.OttoBusDriver;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.proxy.util.ViewUtils.getLargeIconDimen;
import static com.proxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Fragment to handle searching for {@link Contact}s.
 */
public class SearchFragment extends BaseFragment {

    @InjectView(R.id.fragment_search_bar_back_button)
    protected ImageView mBackButton;
    @InjectView(R.id.fragment_search_bar_edittext)
    protected EditText mEditText;
    @InjectView(R.id.fragment_search_bar_clear_button)
    protected ImageView mClearButton;

    /**
     * Constructor.
     */
    public SearchFragment() {
    }

    /**
     * Return new {@link SearchFragment} instance.
     *
     * @return fragment
     */
    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    /**
     * Handle back button press in this fragments parent {@link SearchActivity}.
     */
    @OnClick(R.id.fragment_search_bar_back_button)
    public void onClickBack() {
        getActivity().onBackPressed();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        OttoBusDriver.register(this);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize this view.
     */
    private void initialize() {
        mBackButton.setImageDrawable(getBackArrowDrawable());
        mClearButton.setImageDrawable(getClearSearchDrawable());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        OttoBusDriver.unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * Return an SVG drawable icon for the back arrow.
     *
     * @return back arrow drawable
     */
    private Drawable getBackArrowDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.arrow_back,
            getLargeIconDimen(getActivity()), Color.GRAY);
    }

    /**
     * Return an SVG drawable icon for the clear button.
     *
     * @return clear button drawable
     */
    private Drawable getClearSearchDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.clear,
            getLargeIconDimen(getActivity()), Color.GRAY);
    }

}
