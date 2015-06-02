package com.shareyourproxy.app.fragment;

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

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxTextWatcherSubject;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.SearchActivity;
import com.shareyourproxy.app.adapter.UserRecyclerAdapter;
import com.shareyourproxy.widget.BaseRecyclerView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.api.rx.RxRealmQuery.queryAllUsers;
import static com.shareyourproxy.api.rx.RxRealmQuery.searchUserString;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.DebugUtils.showBroToast;
import static com.shareyourproxy.util.ViewUtils.getLargeIconDimen;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;
import static com.shareyourproxy.util.ViewUtils.showSoftwareKeyboard;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;
import static rx.android.app.AppObservable.bindFragment;

/**
 * Fragment to handle searching for {@link Contact}s.
 */
public class SearchFragment extends BaseFragment implements ItemClickListener {

    @InjectView(R.id.fragment_search_back_button)
    protected ImageView imageViewBackButton;
    @InjectView(R.id.fragment_search_edittext)
    protected EditText editText;
    @InjectView(R.id.fragment_search_clear_button)
    protected ImageView imageViewClearButton;
    @InjectView(R.id.fragment_search_recyclerview)
    protected BaseRecyclerView recyclerView;
    private UserRecyclerAdapter _adapter;
    private CompositeSubscription _subscriptions;
    private RxTextWatcherSubject _textWatcherSubject;

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
        hideSoftwareKeyboard(editText);
        getActivity().onBackPressed();
    }

    /**
     * Handle back button press in this fragments parent {@link SearchActivity}.
     */
    @OnClick(R.id.fragment_search_clear_button)
    public void onClickClear() {
        editText.setText("");
    }

    /**
     * {@link android.text.TextWatcher#afterTextChanged(Editable)}
     *
     * @param editable new search string
     */
    @OnTextChanged(value = R.id.fragment_search_edittext,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onSearchStringChanged(Editable editable) {
        _textWatcherSubject.post(editable.toString().trim());
    }

    private JustObserver<ArrayList<User>> getSearchObserver() {
        return new JustObserver<ArrayList<User>>() {
            @Override
            public void onError() {

            }

            @Override
            public void onNext(ArrayList<User> users) {
                _adapter.setUsers(users);
            }
        };
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize this view.
     */
    private void initialize() {
        _textWatcherSubject = RxTextWatcherSubject.getInstance();
        imageViewBackButton.setImageDrawable(getBackArrowDrawable());
        imageViewClearButton.setImageDrawable(getClearSearchDrawable());
        initializeRecyclerView();
        showSoftwareKeyboard(editText);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = UserRecyclerAdapter.newInstance(this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        getRxBus().post(new UserSelectedEvent(_adapter.getItemData(position)));
    }

    @Override
    public void onItemLongClick(View view, int position) {
        showBroToast(getActivity(), _adapter.getItemData(position).last());
    }

    /**
     * Return an SVG drawable icon for the back arrow.
     *
     * @return back arrow drawable
     */
    private Drawable getBackArrowDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_arrow_back,
            getLargeIconDimen(getActivity()), Color.GRAY);
    }

    /**
     * Return an SVG drawable icon for the clear button.
     *
     * @return clear button drawable
     */
    private Drawable getClearSearchDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_clear,
            getLargeIconDimen(getActivity()), Color.GRAY);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())
            .subscribe(onNextEvent()));
        _subscriptions.add(bindFragment(this, queryAllUsers(getActivity())).subscribe
            (getSearchObserver()));
        _subscriptions.add(bindFragment(this,
            _textWatcherSubject.toObserverable().map(searchUserString(getActivity())))
            .subscribe(getSearchObserver()));
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof UserSelectedEvent) {
                    onUserSelected((UserSelectedEvent) event);
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        hideSoftwareKeyboard(getView());
        _subscriptions.unsubscribe();
    }

    /**
     * User selected is this Fragments underlying recyclerView.Adapter.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user);
    }

}
