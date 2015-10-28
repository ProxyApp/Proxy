package com.shareyourproxy.app.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.RxTextWatcherSubject;
import com.shareyourproxy.api.rx.event.OnBackPressedEvent;
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent;
import com.shareyourproxy.api.rx.event.TextViewEditorActionEvent;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.SearchActivity;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.SearchUserAdapter;
import com.shareyourproxy.app.adapter.SearchUserAdapter.UserViewHolder;
import com.shareyourproxy.widget.ContentDescriptionDrawable;
import com.shareyourproxy.widget.CustomEditText;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindInt;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observer;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.api.rx.RxQuery.searchMatchingUsers;
import static com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.EMPTY;
import static com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.LOADING;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;
import static com.shareyourproxy.util.ViewUtils.showSoftwareKeyboard;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Search for {@link User}s.
 */
public class SearchFragment extends BaseFragment implements ItemClickListener {

    @Bind(R.id.fragment_search_bar_container)
    LinearLayout searchBarContainer;
    @Bind(R.id.fragment_search_back_button)
    ImageView imageViewBackButton;
    @Bind(R.id.fragment_search_edittext)
    CustomEditText editText;
    @Bind(R.id.fragment_search_clear_button)
    ImageView imageViewClearButton;
    @Bind(R.id.fragment_search_recyclerview)
    BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_search_empty_textview)
    TextView emptyTextView;
    @Bind(R.id.fragment_search_empty_view_container)
    LinearLayout emptyViewContainer;
    @Bind(R.id.fragment_search_loadingview)
    ProgressBar loadingView;
    @BindInt(android.R.integer.config_shortAnimTime)
    int _animationDuration;
    @BindDimen(R.dimen.common_svg_null_screen_small)
    int sexBotSize;
    @BindDimen(R.dimen.common_svg_large)
    int dimenSvgLarge;
    @BindColor(R.color.common_gray)
    int colorGray;
    private SearchUserAdapter _adapter;
    private CompositeSubscription _subscriptions;
    private RxTextWatcherSubject _textWatcherSubject;
    private boolean _needsUpdate = true;

    /**
     * Constructor.
     */
    public SearchFragment() {
    }

    /**
     * Return a new instance for the parent {@link SearchActivity}.
     *
     * @return new {@link SearchFragment}
     */
    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    /**
     * Handle back button press in this fragments parent {@link SearchActivity}.
     */
    @OnClick(R.id.fragment_search_back_button)
    void onClickBack() {
        hideSoftwareKeyboard(editText);
        getActivity().onBackPressed();
    }

    @OnClick(R.id.fragment_search_empty_button)
    void onClickInviteFriend() {
        editText.setText("");
        IntentLauncher.launchInviteFriendIntent(getActivity());
    }

    /**
     * Clear the search edit text and search for all users.
     */
    @OnClick(R.id.fragment_search_clear_button)
    void onClickClear() {
        editText.setText("");
        //TODO: Make this clear to the featured users.
        _adapter.clearUserList();
    }

    /**
     * {@link android.text.TextWatcher#afterTextChanged(Editable)}
     *
     * @param editable new search string
     */
    @OnTextChanged(value = R.id.fragment_search_edittext,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onSearchStringChanged(Editable editable) {
        getRxBus().post(new RecyclerViewDatasetChangedEvent(_adapter, LOADING));
        _textWatcherSubject.post(editable.toString());
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
        editText.setRxBus(getRxBus());
        _textWatcherSubject = RxTextWatcherSubject.getInstance();
        imageViewBackButton.setImageDrawable(getBackArrowDrawable());
        imageViewClearButton.setImageDrawable(getClearSearchDrawable());
        initializeRecyclerView();
        editText.requestFocus();
        showSoftwareKeyboard(editText);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
            null, getSexBotDrawable(), null, null);
        _adapter = SearchUserAdapter.newInstance(recyclerView, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyView(emptyViewContainer);
        recyclerView.setLoadingView(loadingView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(getDismissScrollListener());
        recyclerView.setAdapter(_adapter);
    }

    /**
     * Parse a svg and return a null screen sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private Drawable getSexBotDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_sexbot, sexBotSize);
    }

    @Override
    public void onItemClick(View view, int position) {
        UserViewHolder holder = (UserViewHolder) recyclerView.getChildViewHolder(view);
        getRxBus().post(
            new UserSelectedEvent(
                holder.userImage, holder.userName, _adapter.getItemData(position)));
    }

    /**
     * Return an SVG image drawable icon for the back arrow.
     *
     * @return back arrow image.drawable
     */
    private Drawable getBackArrowDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_arrow_back, dimenSvgLarge, colorGray);
    }

    /**
     * Return an SVG image drawable icon for the clear button.
     *
     * @return clear button image.drawable
     */
    private Drawable getClearSearchDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_clear, dimenSvgLarge, colorGray);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = checkCompositeButton(_subscriptions);
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(onNextEvent()));

        final User loggedInUser = getLoggedInUser();
        if (loggedInUser != null) {
            _subscriptions.add(
                _textWatcherSubject.toObserverable()
                    .compose(RxHelper.<String>applySchedulers())
                    .subscribe(getUsersObserver(loggedInUser)));
            //search entered text
            _textWatcherSubject.post(editText.getText().toString().trim());
        }
    }

    public JustObserver<String> getUsersObserver(final User loggedInUser) {
        return new JustObserver<String>() {
            @Override
            public void next(String queryName) {
                String trimmedName = queryName.trim();
                if (!trimmedName.isEmpty()) {
                    _adapter.clearUserList();
                    searchMatchingUsers(getActivity(), trimmedName, loggedInUser.id())
                        .compose(RxHelper.<HashMap<String, User>>applySchedulers())
                        .subscribe(getSearchObserver());
                } else {
                    //TODO:Change this to featured user list
                    _adapter.clearUserList();
                }
            }

            @Override
            public void error(Throwable e) {
                Timber.e("Search Observer error: %1$s", Log.getStackTraceString(e));
            }
        };
    }

    /**
     * Textwatcher Subject Observer. Refresh the user list based on a search query.
     *
     * @return search observer
     */
    private Observer<HashMap<String, User>> getSearchObserver() {

        return new Observer<HashMap<String, User>>() {
            @Override
            public void onCompleted() {
                if (_adapter.getItemCount() == 0) {
                    getRxBus().post(new RecyclerViewDatasetChangedEvent(_adapter,  EMPTY));
                }
                _needsUpdate = true;
            }

            @Override
            public void onError(Throwable e) {
                _needsUpdate = true;
            }

            @Override
            public void onNext(HashMap<String, User> users) {
                //this update flag clears the list each time a new user query is called
                if (_needsUpdate) {
                    _adapter.clearUserList();
                    _needsUpdate = false;
                }
                _adapter.refreshData(users.values());
            }
        };
    }

    /**
     * Observe the next event.
     *
     * @return next event observer
     */
    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof UserSelectedEvent) {
                    onUserSelected((UserSelectedEvent) event);
                } else if (event instanceof OnBackPressedEvent) {
                    imageViewClearButton.animate().alpha(0f).setDuration(_animationDuration);
                } else if (event instanceof RecyclerViewDatasetChangedEvent) {
                    recyclerView.updateViewState(((RecyclerViewDatasetChangedEvent) event));
                } else if (event instanceof TextViewEditorActionEvent) {
                    if (editText.length() == 0 &&
                        ((TextViewEditorActionEvent) event).keyEvent.getKeyCode() ==
                            KeyEvent.KEYCODE_DEL) {
                        _textWatcherSubject.post("");
                    }
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        hideSoftwareKeyboard(getView());
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    /**
     * User selected, open their profile.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.user, getLoggedInUser().id(),
            event.imageView, event.textView);
    }

}
