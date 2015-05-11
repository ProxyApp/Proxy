package com.proxy.app.fragment;

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

import com.proxy.IntentLauncher;
import com.proxy.R;
import com.proxy.api.domain.model.Contact;
import com.proxy.api.domain.model.User;
import com.proxy.api.rx.JustObserver;
import com.proxy.api.rx.RxRealmQuery;
import com.proxy.api.rx.event.UserSelectedEvent;
import com.proxy.app.SearchActivity;
import com.proxy.app.adapter.BaseViewHolder;
import com.proxy.app.adapter.UserRecyclerAdapter;
import com.proxy.util.ViewUtils;
import com.proxy.widget.BaseRecyclerView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.realm.Realm;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.proxy.util.ViewUtils.getLargeIconDimen;
import static com.proxy.util.ViewUtils.hideSoftwareKeyboard;
import static com.proxy.util.ViewUtils.showSoftwareKeyboard;
import static com.proxy.util.ViewUtils.svgToBitmapDrawable;
import static rx.android.app.AppObservable.bindFragment;

/**
 * Fragment to handle searching for {@link Contact}s.
 */
public class SearchFragment extends BaseFragment implements BaseViewHolder.ItemClickListener {

    @InjectView(R.id.fragment_search_back_button)
    protected ImageView mBackButton;
    @InjectView(R.id.fragment_search_edittext)
    protected EditText mEditText;
    @InjectView(R.id.fragment_search_clear_button)
    protected ImageView mClearButton;
    @InjectView(R.id.fragment_search_recyclerview)
    protected BaseRecyclerView mRecyclerView;
    private UserRecyclerAdapter mAdapter;
    private Realm mRealm;
    private CompositeSubscription mSubscriptions;

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
        RxRealmQuery.searchUsersTable(getActivity(), editable.toString().trim())
            .subscribe(getSearchObserver());
    }

    private JustObserver<ArrayList<User>> getSearchObserver() {
        return new JustObserver<ArrayList<User>>() {
            @Override
            public void error() {

            }

            @Override
            public void onNext(ArrayList<User> users) {
                mAdapter.setUsers(users);
            }
        };
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRealm = Realm.getInstance(getActivity());
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
        showSoftwareKeyboard(mEditText);
        RxRealmQuery.queryAllUsers(getActivity()).subscribe(getQueryObserver());
    }

    private JustObserver<ArrayList<User>> getQueryObserver() {
        return new JustObserver<ArrayList<User>>() {
            @Override
            public void error() {

            }

            @Override
            public void onNext(ArrayList<User> users) {
                mAdapter.setUsers(users);
            }
        };
    }


    /**
     * Initialize a RecyclerView with User data.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = UserRecyclerAdapter.newInstance(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        getRxBus().post(new UserSelectedEvent(mAdapter.getItemData(position)));
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

    @Override
    public void onResume() {
        super.onResume();
        mSubscriptions = new CompositeSubscription();
        mSubscriptions.add(bindFragment(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserSelectedEvent) {
                        onUserSelected((UserSelectedEvent) event);
                    }
                }
            }));
    }

    @Override
    public void onPause() {
        super.onPause();
        ViewUtils.hideSoftwareKeyboard(getView());
        mSubscriptions.unsubscribe();
    }

    /**
     * User selected is this Fragments underlying RecyclerView.Adapter.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        IntentLauncher.launchUserProfileActivity(getActivity(), event.user);
    }

}
