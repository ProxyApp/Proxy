package com.shareyourproxy.app.fragment

import android.R.integer.config_shortAnimTime
import android.content.res.ColorStateList
import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.graphics.drawable.DrawableCompat.setTintList
import android.support.v4.graphics.drawable.DrawableCompat.setTintMode
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.getStackTraceString
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import com.shareyourproxy.IntentLauncher.launchInviteFriendIntent
import com.shareyourproxy.IntentLauncher.launchUserProfileActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_blue
import com.shareyourproxy.R.color.common_gray
import com.shareyourproxy.R.dimen.common_svg_large
import com.shareyourproxy.R.dimen.common_svg_null_screen_mini
import com.shareyourproxy.R.layout.fragment_search
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.RxHelper.observeMain
import com.shareyourproxy.api.rx.RxQuery.searchMatchingUsers
import com.shareyourproxy.api.rx.RxTextWatcherRelay.post
import com.shareyourproxy.api.rx.RxTextWatcherRelay.textWatcherObserverable
import com.shareyourproxy.api.rx.event.OnBackPressedEvent
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.api.rx.event.TextViewEditorActionEvent
import com.shareyourproxy.api.rx.event.UserSelectedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.SearchUserAdapter
import com.shareyourproxy.app.adapter.SearchUserAdapter.UserViewHolder
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindColorStateList
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindInt
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.Enumerations.ViewState.EMPTY
import com.shareyourproxy.util.Enumerations.ViewState.MAIN
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard
import com.shareyourproxy.util.ViewUtils.showSoftwareKeyboard
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.CustomEditText
import rx.Observer
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.*

/**
 * Search for [User]s.
 */
internal final class SearchFragment() : BaseFragment(), ItemClickListener {

    private val imageViewBackButton: ImageView by bindView(R.id.fragment_search_back_button)
    private val editText: CustomEditText by bindView(R.id.fragment_search_edittext)
    private val imageViewClearButton: ImageView by bindView(R.id.fragment_search_clear_button)
    private val recyclerView: BaseRecyclerView by bindView(R.id.fragment_search_recyclerview)
    private val emptyTextView: TextView by bindView(R.id.fragment_search_empty_textview)
    private val emptyViewContainer: LinearLayout by bindView(R.id.fragment_search_empty_view_container)
    private val emptyViewButton: Button by bindView(R.id.fragment_search_empty_button)
    private val loadingView: ProgressBar by bindView(R.id.fragment_search_loadingview)
    private val animationDuration: Int by bindInt(config_shortAnimTime)
    private val sexBotSize: Int by bindDimen(common_svg_null_screen_mini)
    private val dimenSvgLarge: Int by bindDimen(common_svg_large)
    private val colorGray: Int by bindColor(common_gray)
    private val colorBlue: ColorStateList by bindColorStateList(common_blue)
    private val adapter: SearchUserAdapter = SearchUserAdapter(recyclerView, this)
    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private val sexBotDrawable: Drawable = svgToBitmapDrawable(activity, R.raw.ic_sexbot, sexBotSize)
    private val backArrowDrawable: Drawable = svgToBitmapDrawable(activity, R.raw.ic_arrow_back, dimenSvgLarge, colorGray)
    private val clearSearchDrawable: Drawable = svgToBitmapDrawable(activity, R.raw.ic_clear, dimenSvgLarge, colorGray)
    /**
     * Observe the next event.
     * @return next event observer
     */
    private val onNextEvent = object : JustObserver<Any>(SearchFragment::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is UserSelectedEvent) {
                onUserSelected(event)
            } else if (event is OnBackPressedEvent) {
                imageViewClearButton.animate().alpha(0f).setDuration(animationDuration.toLong())
            } else if (event is TextViewEditorActionEvent) {
                if (event.keyEvent.keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.length() == 0) {
                        adapter.clearUserList()
                        recyclerView.updateViewState(RecyclerViewDatasetChangedEvent(adapter, EMPTY))
                        loadingView.visibility = GONE
                    } else {
                        loadingView.visibility = VISIBLE
                    }
                }
            }
        }
    }
    /**
     * Handle back button press in this fragments parent [SearchActivity].
     */
    private val onClickBack: View.OnClickListener = View.OnClickListener {
        hideSoftwareKeyboard(editText)
        activity.onBackPressed()
    }

    private val onClickInviteFriend: View.OnClickListener = View.OnClickListener {
        editText.setText("")
        launchInviteFriendIntent(activity)
    }

    /**
     * Clear the search edit text and search for all users.
     */
    private val onClickClear: View.OnClickListener = View.OnClickListener {
        editText.setText("")
        //TODO: Make this clear to the featured users.
        adapter.clearUserList()
        loadingView.visibility = GONE
        recyclerView.updateViewState(RecyclerViewDatasetChangedEvent(adapter, EMPTY))
    }

    /**
     * [android.text.TextWatcher.afterTextChanged]
     */
    private val onSearchStringChanged: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun afterTextChanged(editable: Editable) {
            post(editable.toString())
            loadingView.visibility = VISIBLE
        }

    }

    /**
     * Textwatcher Subject Observer. Refresh the user list based on a search query.
     * @return search observer
     */
    private val searchObserver: Observer<HashMap<String, User>> = object : Observer<HashMap<String, User>> {
        override fun onCompleted() {
            needsUpdate = true
            if (userCount == 0) {
                recyclerView.updateViewState(
                        RecyclerViewDatasetChangedEvent(adapter, EMPTY))
            }
            loadingView.visibility = GONE
        }

        override fun onError(e: Throwable) {
            needsUpdate = true
            recyclerView.updateViewState(RecyclerViewDatasetChangedEvent(adapter, EMPTY))
            loadingView.visibility = GONE
            Timber.e("Error ${getStackTraceString(e)}")
        }

        override fun onNext(users: HashMap<String, User>) {
            userCount = users.size
            if (userCount > 0) {
                if (needsUpdate) {
                    adapter.clearUserList()
                    needsUpdate = false
                }
                adapter.refreshData(users.values)
                recyclerView.updateViewState(
                        RecyclerViewDatasetChangedEvent(adapter, MAIN))
                recyclerView.scrollToPosition(0)
            }
        }
    }

    private val getUsersObserver = object : JustObserver<String>(SearchFragment::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(queryName: String) {
            val trimmedName: String = queryName.trim { it <= ' ' }
            if (!trimmedName.isEmpty()) {
                adapter.setQueryString(trimmedName)
                searchMatchingUsers(activity, trimmedName, loggedInUser.id).compose(observeMain<HashMap<String, User>>()).subscribe(searchObserver)
            }
        }
    }

    private var needsUpdate = true
    private var userCount: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(fragment_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(rxBusObservable().subscribe(onNextEvent))
        subscriptions.add(textWatcherObserverable().compose(observeMain<String>()).subscribe(getUsersObserver))
        //search entered text
        post(editText.text.toString().trim { it <= ' ' })
        showSoftwareKeyboard(editText)
    }

    override fun onPause() {
        super.onPause()
        hideSoftwareKeyboard(view)
        subscriptions.unsubscribe()
    }

    override fun onItemClick(view: View, position: Int) {
        val holder = recyclerView.getChildViewHolder(view) as UserViewHolder
        post(UserSelectedEvent(holder.userImage, holder.userName, adapter.getItemData(position)))
    }

    /**
     * Initialize this view.
     */
    private fun initialize() {
        editText.addTextChangedListener(onSearchStringChanged)
        imageViewBackButton.setOnClickListener(onClickBack)
        emptyViewButton.setOnClickListener(onClickInviteFriend)
        imageViewClearButton.setOnClickListener(onClickClear)
        imageViewBackButton.setImageDrawable(backArrowDrawable)
        imageViewClearButton.setImageDrawable(clearSearchDrawable)
        initializeRecyclerView()
        tintProgressBar()
    }

    private fun tintProgressBar() {
        setTintMode(loadingView.indeterminateDrawable, SRC_ATOP)
        setTintList(loadingView.indeterminateDrawable, colorBlue)
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private fun initializeRecyclerView() {
        emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, sexBotDrawable, null, null)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setEmptyView(emptyViewContainer)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addOnScrollListener(dismissScrollListener)
        recyclerView.adapter = adapter
    }

    /**
     * User selected, open their profile.
     * @param event data
     */
    private fun onUserSelected(event: UserSelectedEvent) {
        launchUserProfileActivity(activity, event.user, loggedInUser.id, event.imageView, event.textView)
    }
}
