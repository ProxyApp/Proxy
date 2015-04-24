package com.proxy.app.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.proxy.R;
import com.proxy.api.RestClient;
import com.proxy.api.model.Contact;
import com.proxy.api.model.User;
import com.proxy.api.service.UserService;
import com.proxy.app.SearchActivity;
import com.proxy.app.adapter.UserRecyclerAdapter;
import com.proxy.event.OttoBusDriver;
import com.proxy.widget.BaseRecyclerView;

import java.util.ArrayList;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.functions.Action1;

import static com.proxy.util.ViewUtils.getLargeIconDimen;
import static com.proxy.util.ViewUtils.hideSoftwareKeyboard;
import static com.proxy.util.ViewUtils.showSoftwareKeyboard;
import static com.proxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Fragment to handle searching for {@link Contact}s.
 */
public class SearchFragment extends BaseFragment {

    @InjectView(R.id.fragment_search_back_button)
    protected ImageView mBackButton;
    @InjectView(R.id.fragment_search_edittext)
    protected EditText mEditText;
    @InjectView(R.id.fragment_search_clear_button)
    protected ImageView mClearButton;
    @InjectView(R.id.fragment_search_recyclerview)
    protected BaseRecyclerView mRecyclerView;
    private UserRecyclerAdapter mAdapter;

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
    @OnClick(R.id.fragment_search_back_button)
    public void onClickBack() {
        hideSoftwareKeyboard(mEditText);
        getActivity().onBackPressed();
    }

    /**
     * Handle back button press in this fragments parent {@link SearchActivity}.
     */
    @OnClick(R.id.fragment_search_clear_button)
    public void onClickClear() {
        mEditText.setText("");
    }

    /**
     * {@link android.text.TextWatcher#afterTextChanged(Editable)}
     *
     * @param editable new search string
     */
    @OnTextChanged(value = R.id.fragment_search_edittext,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    @SuppressWarnings("unused")
    public void onSearchStringChanged(Editable editable) {
        mAdapter.getFilter().filter(editable.toString());
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
        initializeRecyclerView();
        initializeUserData();
        showSoftwareKeyboard(mEditText);
    }

    /**
     * Get the {@link User} data.
     */
    private void initializeUserData() {
        UserService userService = RestClient.newInstance(getActivity()).getUserService();
        userService.listUsers().subscribe(new Action1<Map<String, User>>() {
            @Override
            public void call(Map<String, User> userMap) {
                for (Map.Entry<String, User> entry : userMap.entrySet()) {
                    addUserToAdapter(entry.getValue());
                }
            }
        });
    }

    /**
     * Add a user to the {@link ArrayList} persisted in the {@link UserRecyclerAdapter}.
     *
     * @param user to add
     */
    private void addUserToAdapter(User user) {
        mAdapter.addUserData(user);
        mAdapter.notifyItemInserted(mAdapter.getItemCount());
    }

    /**
     * Initialize a RecyclerView with User data.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = UserRecyclerAdapter.newInstance();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
