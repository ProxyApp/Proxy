package com.shareyourproxy.app.fragment

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

import com.shareyourproxy.IntentLauncher
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxHelper
import com.shareyourproxy.api.rx.RxQuery
import com.shareyourproxy.api.rx.RxTextWatcherSubject
import com.shareyourproxy.api.rx.event.OnBackPressedEvent
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.api.rx.event.TextViewEditorActionEvent
import com.shareyourproxy.api.rx.event.UserSelectedEvent
import com.shareyourproxy.app.SearchActivity
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.SearchUserAdapter
import com.shareyourproxy.app.adapter.SearchUserAdapter.UserViewHolder
import com.shareyourproxy.widget.ContentDescriptionDrawable
import com.shareyourproxy.widget.CustomEditText

import java.util.HashMap

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.BindInt
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTextChanged
import rx.Observer
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.support.v4.graphics.drawable.DrawableCompat.setTintList
import android.support.v4.graphics.drawable.DrawableCompat.setTintMode
import android.view.View.GONE
import android.view.View.VISIBLE
import com.shareyourproxy.IntentLauncher.launchUserProfileActivity
import com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.EMPTY
import com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.MAIN
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard
import com.shareyourproxy.util.ViewUtils.showSoftwareKeyboard
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable

/**
 * Search for [User]s.
 */
class SearchFragment : BaseFragment(), ItemClickListener {

    var rxQuery = RxQuery
    @Bind(R.id.fragment_search_bar_container)
    internal var searchBarContainer: LinearLayout
    @Bind(R.id.fragment_search_back_button)
    internal var imageViewBackButton: ImageView
    @Bind(R.id.fragment_search_edittext)
    internal var editText: CustomEditText
    @Bind(R.id.fragment_search_clear_button)
    internal var imageViewClearButton: ImageView
    @Bind(R.id.fragment_search_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @Bind(R.id.fragment_search_empty_textview)
    internal var emptyTextView: TextView
    @Bind(R.id.fragment_search_empty_view_container)
    internal var emptyViewContainer: LinearLayout
    @Bind(R.id.fragment_search_loadingview)
    internal var loadingView: ProgressBar
    @BindInt(android.R.integer.config_shortAnimTime)
    internal var _animationDuration: Int = 0
    @BindDimen(R.dimen.common_svg_null_screen_mini)
    internal var sexBotSize: Int = 0
    @BindDimen(R.dimen.common_svg_large)
    internal var dimenSvgLarge: Int = 0
    @BindColor(R.color.common_gray)
    internal var colorGray: Int = 0
    @BindColor(R.color.common_blue)
    internal var colorBlue: ColorStateList
    private var _adapter: SearchUserAdapter? = null
    private var _subscriptions: CompositeSubscription? = null
    private val _textWatcherSubject = RxTextWatcherSubject
    private val rxHelper = RxHelper
    private var _needsUpdate = true
    private var _userCount: Int = 0

    /**
     * Handle back button press in this fragments parent [SearchActivity].
     */
    @OnClick(R.id.fragment_search_back_button)
    internal fun onClickBack() {
        hideSoftwareKeyboard(editText)
        activity.onBackPressed()
    }

    @OnClick(R.id.fragment_search_empty_button)
    internal fun onClickInviteFriend() {
        editText.setText("")
        IntentLauncher.launchInviteFriendIntent(activity)
    }

    /**
     * Clear the search edit text and search for all users.
     */
    @OnClick(R.id.fragment_search_clear_button)
    internal fun onClickClear() {
        editText.setText("")
        //TODO: Make this clear to the featured users.
        _adapter!!.clearUserList()
        loadingView.visibility = GONE
        recyclerView.updateViewState(
                RecyclerViewDatasetChangedEvent(_adapter, EMPTY))
    }

    /**
     * [android.text.TextWatcher.afterTextChanged]

     * @param editable new search string
     */
    @OnTextChanged(value = R.id.fragment_search_edittext, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    internal fun onSearchStringChanged(editable: Editable) {
        _textWatcherSubject.post(editable.toString())
        loadingView.visibility = VISIBLE
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_search, container, false)
        ButterKnife.bind(this, rootView)
        initialize()
        return rootView
    }

    /**
     * Initialize this view.
     */
    private fun initialize() {
        editText.setRxBus(rxBus)
        imageViewBackButton.setImageDrawable(backArrowDrawable)
        imageViewClearButton.setImageDrawable(clearSearchDrawable)
        initializeRecyclerView()
        tintProgressBar()
    }

    fun tintProgressBar() {
        setTintMode(loadingView.indeterminateDrawable, SRC_ATOP)
        setTintList(loadingView.indeterminateDrawable, colorBlue)
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private fun initializeRecyclerView() {
        emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
                null, sexBotDrawable, null, null)
        _adapter = SearchUserAdapter.newInstance(recyclerView, this)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setEmptyView(emptyViewContainer)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addOnScrollListener(dismissScrollListener)
        recyclerView.adapter = _adapter
    }

    /**
     * Parse a svg and return a null screen sized [ContentDescriptionDrawable] .

     * @return Drawable with a contentDescription
     */
    private val sexBotDrawable: Drawable
        get() = svgToBitmapDrawable(activity, R.raw.ic_sexbot, sexBotSize)

    override fun onItemClick(view: View, position: Int) {
        val holder = recyclerView.getChildViewHolder(view) as UserViewHolder
        rxBus.post(
                UserSelectedEvent(
                        holder.userImage, holder.userName, _adapter!!.getItemData(position)))
    }

    /**
     * Return an SVG image drawable icon for the back arrow.

     * @return back arrow image.drawable
     */
    private val backArrowDrawable: Drawable
        get() = svgToBitmapDrawable(activity, R.raw.ic_arrow_back, dimenSvgLarge, colorGray)

    /**
     * Return an SVG image drawable icon for the clear button.

     * @return clear button image.drawable
     */
    private val clearSearchDrawable: Drawable
        get() = svgToBitmapDrawable(activity, R.raw.ic_clear, dimenSvgLarge, colorGray)

    override fun onResume() {
        super.onResume()
        _subscriptions = rxHelper.checkCompositeButton(_subscriptions)
        _subscriptions!!.add(rxBus.toObservable().subscribe(onNextEvent()))

        val loggedInUser = loggedInUser
        if (loggedInUser != null) {
            _subscriptions!!.add(
                    _textWatcherSubject.toObserverable().compose(rxHelper.observeMain<String>()).subscribe(getUsersObserver(loggedInUser)))
            //search entered text
            _textWatcherSubject.post(editText.text.toString().trim { it <= ' ' })
        }
        showSoftwareKeyboard(editText)
    }

    fun getUsersObserver(loggedInUser: User): JustObserver<String> {
        return object : JustObserver<String>() {

            fun next(queryName: String) {
                val trimmedName = queryName.trim { it <= ' ' }
                if (!trimmedName.isEmpty()) {
                    _adapter!!.setQueryString(trimmedName)
                    rxQuery.searchMatchingUsers(activity, trimmedName, loggedInUser.id()).compose(rxHelper.observeMain<HashMap<String, User>>()).subscribe(searchObserver)
                }
            }
        }
    }

    /**
     * Textwatcher Subject Observer. Refresh the user list based on a search query.

     * @return search observer
     */
    private val searchObserver: Observer<HashMap<String, User>>
        get() = object : Observer<HashMap<String, User>> {
            override fun onCompleted() {
                _needsUpdate = true
                if (_userCount == 0) {
                    recyclerView.updateViewState(
                            RecyclerViewDatasetChangedEvent(_adapter, EMPTY))
                }
                loadingView.visibility = GONE
            }

            override fun onError(e: Throwable) {
                _needsUpdate = true
                recyclerView.updateViewState(
                        RecyclerViewDatasetChangedEvent(_adapter, EMPTY))
                loadingView.visibility = GONE
                Timber.e("Error %1$s", Log.getStackTraceString(e))
            }

            override fun onNext(users: HashMap<String, User>) {
                _userCount = users.size
                if (_userCount > 0) {
                    if (_needsUpdate) {
                        _adapter!!.clearUserList()
                        _needsUpdate = false
                    }
                    _adapter!!.refreshData(users.values)
                    recyclerView.updateViewState(
                            RecyclerViewDatasetChangedEvent(_adapter, MAIN))
                    recyclerView.scrollToPosition(0)
                }
            }
        }

    /**
     * Observe the next event.

     * @return next event observer
     */
    private fun onNextEvent(): JustObserver<Any> {
        return object : JustObserver<Any>() {
            fun next(event: Any) {
                if (event is UserSelectedEvent) {
                    onUserSelected(event)
                } else if (event is OnBackPressedEvent) {
                    imageViewClearButton.animate().alpha(0f).setDuration(_animationDuration.toLong())
                } else if (event is TextViewEditorActionEvent) {
                    if (event.keyEvent.keyCode == KeyEvent.KEYCODE_DEL) {
                        if (editText.length() == 0) {
                            _adapter!!.clearUserList()
                            recyclerView.updateViewState(
                                    RecyclerViewDatasetChangedEvent(_adapter, EMPTY))
                            loadingView.visibility = GONE
                        } else {
                            loadingView.visibility = VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideSoftwareKeyboard(view)
        _subscriptions!!.unsubscribe()
        _subscriptions = null
    }

    /**
     * User selected, open their profile.

     * @param event data
     */
    fun onUserSelected(event: UserSelectedEvent) {
        launchUserProfileActivity(activity, event.user, loggedInUser.id(),
                event.imageView, event.textView)
    }

    companion object {

        /**
         * Return a new instance for the parent [SearchActivity].

         * @return new [SearchFragment]
         */
        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }

}
/**
 * Constructor.
 */
