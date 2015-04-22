package com.proxy.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.api.model.Contact;
import com.proxy.event.OttoBusDriver;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment to handle searching for {@link Contact}s.
 */
public class SearchFragment extends BaseFragment {

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
     * Handle back button press.
     *
     * @param view clicked
     */
    @OnClick(R.id.fragment_search_bar_back_button)
    public void onClickBack(View view) {
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
        return rootView;
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

}
