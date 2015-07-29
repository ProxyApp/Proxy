package com.shareyourproxy.app.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxTextWatcherSubject;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.SearchActivity;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.UserAdapter;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.api.rx.RxQuery.queryFilteredUsers;
import static com.shareyourproxy.api.rx.RxQuery.searchUserString;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.DebugUtils.showBroToast;
import static com.shareyourproxy.util.ViewUtils.getLargeIconDimen;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;
import static com.shareyourproxy.util.ViewUtils.showSoftwareKeyboard;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Fragment to handle searching for {@link Contact}s.
 */
public class SearchFragment extends BaseFragment implements ItemClickListener {

    @Bind(R.id.fragment_search_back_button)
    protected ImageView imageViewBackButton;
    @Bind(R.id.fragment_search_edittext)
    protected EditText editText;
    @Bind(R.id.fragment_search_clear_button)
    protected ImageView imageViewClearButton;
    @Bind(R.id.fragment_search_recyclerview)
    protected BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_search_empty_textview)
    protected TextView emptyTextView;
    private UserAdapter _adapter;
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
     * @return layouts.fragment
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

    private JustObserver<HashMap<String, User>> getSearchObserver() {
        return new JustObserver<HashMap<String, User>>() {
            @Override
            public void onError(){}

            @Override
            public void onNext(HashMap<String, User> users) {
                _adapter.refreshUserList(users);
            }
        };
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, rootView);
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
        recyclerView.setEmptyView(emptyTextView);
        _adapter = UserAdapter.newInstance(this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(getScrollListener());
    }

    private RecyclerView.OnScrollListener getScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                hideSoftwareKeyboard(getView());
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
     * Return an SVG image.drawable icon for the back arrow.
     *
     * @return back arrow image.drawable
     */
    private Drawable getBackArrowDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_arrow_back,
            getLargeIconDimen(getActivity()), Color.GRAY);
    }

    /**
     * Return an SVG image.drawable icon for the clear button.
     *
     * @return clear button image.drawable
     */
    private Drawable getClearSearchDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_clear,
            getLargeIconDimen(getActivity()), Color.GRAY);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObserverable()
            .subscribe(onNextEvent()));

        _subscriptions.add(queryFilteredUsers(
            getActivity(), getLoggedInUser().id().value()).subscribe(getSearchObserver()));

        _subscriptions.add(
            _textWatcherSubject.toObserverable().map(
                searchUserString(getActivity(), getLoggedInUser().id().value()))
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
        launchUserProfileActivity(getActivity(), event.user, getLoggedInUser().id().value());
    }

}
