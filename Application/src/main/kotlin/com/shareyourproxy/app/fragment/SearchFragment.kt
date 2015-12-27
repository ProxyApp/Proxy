package com.shareyourproxy.app.fragment

import android.R.integer.config_shortAnimTime
import android.content.res.ColorStateList
import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat.getColor
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
import butterknife.bindView
import com.shareyourproxy.IntentLauncher
import com.shareyourproxy.IntentLauncher.launchUserProfileActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_blue
import com.shareyourproxy.R.color.common_gray
import com.shareyourproxy.R.dimen.common_svg_large
import com.shareyourproxy.R.dimen.common_svg_null_screen_mini
import com.shareyourproxy.R.layout.fragment_search
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.RxHelper.observeMain
import com.shareyourproxy.api.rx.RxQuery.searchMatchingUsers
import com.shareyourproxy.api.rx.RxTextWatcherSubject.post
import com.shareyourproxy.api.rx.RxTextWatcherSubject.toObserverable
import com.shareyourproxy.api.rx.event.OnBackPressedEvent
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.api.rx.event.TextViewEditorActionEvent
import com.shareyourproxy.api.rx.event.UserSelectedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.EMPTY
import com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.MAIN
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.SearchUserAdapter
import com.shareyourproxy.app.adapter.SearchUserAdapter.UserViewHolder
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard
import com.shareyourproxy.util.ViewUtils.showSoftwareKeyboard
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.CustomEditText
import org.jetbrains.anko.onClick
import org.jetbrains.anko.textChangedListener
import rx.Observer
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.*

/**
 * Search for [User]s.
 */
class SearchFragment : BaseFragment(), ItemClickListener {

    private val imageViewBackButton: ImageView by bindView(R.id.fragment_search_back_button)
    private val editText: CustomEditText by bindView(R.id.fragment_search_edittext)
    private val imageViewClearButton: ImageView by bindView(R.id.fragment_search_clear_button)
    private val recyclerView: BaseRecyclerView by bindView(R.id.fragment_search_recyclerview)
    private val emptyTextView: TextView by bindView(R.id.fragment_search_empty_textview)
    private val emptyViewContainer: LinearLayout by bindView(R.id.fragment_search_empty_view_container)
    private val emptyViewButton: Button by bindView(R.id.fragment_search_empty_button)
    private val loadingView: ProgressBar by bindView(R.id.fragment_search_loadingview)
    internal var animationDuration: Int = resources.getInteger(config_shortAnimTime)
    internal var sexBotSize: Int = resources.getDimensionPixelSize(common_svg_null_screen_mini)
    internal var dimenSvgLarge: Int = resources.getDimensionPixelSize(common_svg_large)
    internal var colorGray: Int = getColor(context, common_gray)
    internal var colorBlue: ColorStateList = resources.getColorStateList(common_blue, null)
    private var adapter: SearchUserAdapter = SearchUserAdapter.newInstance(recyclerView, this)
    private var subscriptions: CompositeSubscription = CompositeSubscription()
    private var needsUpdate = true
    private var userCount: Int = 0

    /**
     * Handle back button press in this fragments parent [SearchActivity].
     */
    internal val onClickBack: View.OnClickListener = View.OnClickListener {
        hideSoftwareKeyboard(editText)
        activity.onBackPressed()
    }

    internal val onClickInviteFriend: View.OnClickListener = View.OnClickListener {
        editText.setText("")
        IntentLauncher.launchInviteFriendIntent(activity)
    }

    /**
     * Clear the search edit text and search for all users.
     */
    internal val onClickClear: View.OnClickListener = View.OnClickListener {
        editText.setText("")
        //TODO: Make this clear to the featured users.
        adapter.clearUserList()
        loadingView.visibility = GONE
        recyclerView.updateViewState(RecyclerViewDatasetChangedEvent(adapter, EMPTY))
    }

    /**
     * [android.text.TextWatcher.afterTextChanged]
     * @param editable new search string
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(fragment_search, container, false)
        initialize()
        return rootView
    }

    /**
     * Initialize this view.
     */
    private fun initialize() {
        editText.textChangedListener { onSearchStringChanged }
        imageViewBackButton.onClick { onClickBack }
        emptyViewButton.onClick { onClickInviteFriend }
        imageViewClearButton.onClick { onClickClear }
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
        emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, sexBotDrawable, null, null)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setEmptyView(emptyViewContainer)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addOnScrollListener(dismissScrollListener)
        recyclerView.adapter = adapter
    }

    /**
     * Parse a svg and return a null screen sized [ContentDescriptionDrawable] .
     * @return Drawable with a contentDescription
     */
    private val sexBotDrawable: Drawable get() = svgToBitmapDrawable(activity, R.raw.ic_sexbot, sexBotSize)

    override fun onItemClick(view: View, position: Int) {
        val holder = recyclerView.getChildViewHolder(view) as UserViewHolder
        post(UserSelectedEvent(holder.userImage, holder.userName, adapter.getItemData(position)))
    }

    /**
     * Return an SVG image drawable icon for the back arrow.
     * @return back arrow image.drawable
     */
    private val backArrowDrawable: Drawable get() = svgToBitmapDrawable(activity, R.raw.ic_arrow_back, dimenSvgLarge, colorGray)

    /**
     * Return an SVG image drawable icon for the clear button.
     * @return clear button image.drawable
     */
    private val clearSearchDrawable: Drawable get() = svgToBitmapDrawable(activity, R.raw.ic_clear, dimenSvgLarge, colorGray)

    override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusDriver.rxBusObservable().subscribe(onNextEvent()))
        subscriptions.add(toObserverable().compose(observeMain<String>()).subscribe(getUsersObserver(loggedInUser)))
        //search entered text
        post(editText.text.toString().trim { it <= ' ' })
        showSoftwareKeyboard(editText)
    }

    fun getUsersObserver(loggedInUser: User): JustObserver<String> {
        return object : JustObserver<String>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(queryName: String?) {
                val trimmedName: String = queryName?.trim { it <= ' ' }!!
                if (!trimmedName.isEmpty()) {
                    adapter.setQueryString(trimmedName)
                    searchMatchingUsers(activity, trimmedName, loggedInUser.id).compose(observeMain<HashMap<String, User>>()).subscribe(searchObserver)
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

    /**
     * Observe the next event.
     * @return next event observer
     */
    private fun onNextEvent(): JustObserver<Any> {
        return object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any?) {
                if (event is UserSelectedEvent) {
                    onUserSelected(event)
                } else if (event is OnBackPressedEvent) {
                    imageViewClearButton.animate().alpha(0f).setDuration(animationDuration.toLong())
                } else if (event is TextViewEditorActionEvent) {
                    if (event.keyEvent.keyCode == KeyEvent.KEYCODE_DEL) {
                        if (editText.length() == 0) {
                            adapter.clearUserList()
                            recyclerView.updateViewState(
                                    RecyclerViewDatasetChangedEvent(adapter, EMPTY))
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
        subscriptions.unsubscribe()
    }

    /**
     * User selected, open their profile.
     * @param event data
     */
    fun onUserSelected(event: UserSelectedEvent) {
        launchUserProfileActivity(activity, event.user, loggedInUser.id, event.imageView, event.textView)
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
