package com.shareyourproxy.app.adapter

import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.facebook.drawee.backends.pipeline.Fresco.newDraweeControllerBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ViewUtils.getUserImageHierarchy
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.INVITE_FRIENDS
import java.util.*

/**
 * An Adapter to handle displaying [User]s.
 */
class UserContactsAdapter
private constructor(recyclerView: BaseRecyclerView, sharedPreferences: SharedPreferences, showHeader: Boolean, private val _clickListener: ItemClickListener) :
        NotificationRecyclerAdapter<User>(User::class.java, recyclerView, showHeader, false, sharedPreferences) {

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_user_item, parent, false)
        return UserViewHolder.newInstance(view, _clickListener)
    }

    override fun compare(item1: User, item2: User): Int {
        val compareFirst = item1.first.compareTo(item2.first, true)
        if (compareFirst == 0) {
            return item1.last.compareTo(item2.last, true)
        } else {
            return compareFirst
        }
    }

    override fun areContentsTheSame(item1: User, item2: User): Boolean {
        return (item1.id.equals(item2.id))
    }

    override fun areItemsTheSame(item1: User, item2: User): Boolean {
        return (item1.id.equals(item2.id))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is NotificationRecyclerAdapter.HeaderViewHolder) {
            bindHeaderViewData(holder, INVITE_FRIENDS, true, true)
        } else {
            setItemViewData(holder as UserViewHolder, getItemData(position))
        }
    }

    /**
     * Set this ViewHolders underlying [User] data.
     * @param holder [User] [BaseViewHolder]
     * @param user   the [User] data
     */
    private fun setItemViewData(holder: UserViewHolder, user: User) {
        val context = holder.view.context
        holder.userName.text = user.fullName
        val profileURL = user.profileURL
        val uri = Uri.parse(profileURL)

        holder.userImage.hierarchy = getUserImageHierarchy(context)
        holder.userImage.controller = newDraweeControllerBuilder().setUri(uri).setAutoPlayAnimations(true).build()
    }

    fun refreshUserList(users: HashMap<String, User>) {
        sortedList.clear()
        refreshData(users.values)
    }

    /**
     * ViewHolder for the entered [User] data.
     * @param view              the inflated view
     * @param itemClickListener click listener for each item
     */
    internal class UserViewHolder
    private constructor(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val userName: TextView by bindView(R.id.adapter_user_name)
        val userImage: SimpleDraweeView by bindView(R.id.adapter_user_image)

        companion object {
            fun newInstance(view: View, itemClickListener: ItemClickListener): UserViewHolder {
                return UserViewHolder(view, itemClickListener)
            }
        }
    }

    /**
     * Create a newInstance of a [UserContactsAdapter] with blank data.
     * @return an [UserContactsAdapter] with no data
     */
    companion object {
        fun newInstance(recyclerView: BaseRecyclerView, sharedPreferences: SharedPreferences, showHeader: Boolean, listener: ItemClickListener): UserContactsAdapter {
            return UserContactsAdapter(recyclerView, sharedPreferences, showHeader, listener)
        }
    }
}
