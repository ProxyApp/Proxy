package com.shareyourproxy.app.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco.newDraweeControllerBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.LOADING
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ViewUtils.getUserImageHierarchy
import com.shareyourproxy.util.ButterKnife.bindView
import java.util.*


/**
 * An Adapter to handle displaying [User]s.
 */
class SearchUserAdapter(private val recyclerView: BaseRecyclerView, private val _clickListener: ItemClickListener) : SortedRecyclerAdapter<User>(User::class.java, recyclerView) {
    private var queryString = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_user_item, parent, false)
        return UserViewHolder(view, _clickListener)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        setItemViewData(holder as UserViewHolder, getItemData(position))
    }

    /**
     * Set this ViewHolders underlying [User] data.
     * @param holder [User] [BaseViewHolder]
     * @param user   the [User] data
     */
    private fun setItemViewData(holder: UserViewHolder, user: User) {
        holder.userName.text = user.fullName
        bindUserImage(holder, user)
    }

    private fun bindUserImage(holder: UserViewHolder, user: User) {
        val context = holder.view.context
        val profileURL = user.profileURL

        holder.userImage.hierarchy = getUserImageHierarchy(context)
        holder.userImage.controller = newDraweeControllerBuilder().setUri(Uri.parse(profileURL)).setAutoPlayAnimations(true).build()
    }

    fun clearUserList() {
        sortedList.clear()
        notifyDataSetChanged()
    }

    override fun compare(item1: User, item2: User): Int {
        val compareFirst = sortQueriedString(item1, item2)
        if (compareFirst == 0) {
            return item1.fullName.compareTo(item2.fullName, true)
        } else {
            return compareFirst
        }
    }

    fun sortQueriedString(item1: User, item2: User): Int {
        if (queryString.length > 1) {
            val item1Count = getStartsWithCount(item1.fullName)
            val item2Count = getStartsWithCount(item2.fullName)
            if (item1Count == item2Count) {
                return 0
            } else if (item1Count > item2Count) {
                return -1
            } else {
                return 1
            }
        } else {
            return 0
        }
    }

    fun getStartsWithCount(fullname: String): Int {
        var count = 0
        val fullnameArray = fullname.toUpperCase(Locale.US).toCharArray()
        val queryStringArray = queryString.toUpperCase(Locale.US).toCharArray()
        for (i in queryStringArray.indices) {
            if (fullnameArray[i] == queryStringArray[i]) {
                ++count
            }
        }
        return count
    }

    override fun areContentsTheSame(item1: User, item2: User): Boolean {
        return item1.id.equals(item2.id)
    }

    override fun areItemsTheSame(item1: User, item2: User): Boolean {
        return item1.id.equals(item2.id)
    }

    fun setQueryString(queryString: String) {
        this.queryString = queryString
    }

    override fun onRemoved(position: Int, count: Int) {
        if (itemCount == 0) {
            setNeedsRefresh(true)
            val event = RecyclerViewDatasetChangedEvent(this, LOADING)
            recyclerView.updateViewState(event)
        } else {
            notifyItemRangeRemoved(position, count)
        }
    }

    /**
     * ViewHolder for the entered [User] data.
     * @param view              the inflated view
     * @param itemClickListener click listener for each item
     */
    internal final class UserViewHolder(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val userName: TextView by bindView(R.id.adapter_user_name)
        val userImage: SimpleDraweeView by bindView(R.id.adapter_user_image)
    }
}
