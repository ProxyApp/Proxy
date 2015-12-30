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
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ViewUtils.getUserImageHierarchy
import com.shareyourproxy.util.ButterKnife.bindView

/**
 * Display group contacts.
 */
class GroupContactsAdapter(recyclerView: BaseRecyclerView, private val clickListener: ItemClickListener) : SortedRecyclerAdapter<User>(User::class.java, recyclerView) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_user_item, parent, false)
        return UserViewHolder(view, clickListener)
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
        val context = holder.view.context
        holder.userName.text = user.fullName
        val profileURL = user.profileURL

        holder.userImage.hierarchy = getUserImageHierarchy(context)
        holder.userImage.controller = newDraweeControllerBuilder().setUri(Uri.parse(profileURL)).setAutoPlayAnimations(true).build()
    }

    override fun compare(item1: User, item2: User): Int {
        val comapreFirst = item1.first.compareTo(item2.first, true)

        if (comapreFirst == 0) {
            return item1.last.compareTo(item2.last, true)
        } else {
            return comapreFirst
        }
    }

    override fun areContentsTheSame(item1: User, item2: User): Boolean {
        return item1.id.equals(item2.id)
    }

    override fun areItemsTheSame(item1: User, item2: User): Boolean {
        return item1.id.equals(item2.id)
    }

    /**
     * ViewHolder for the entered [User] data.
     * @param view              the inflated view
     * @param itemClickListener click listener for each item
     */
    private final class UserViewHolder(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val userName: TextView by bindView(R.id.adapter_user_name)
        val userImage: SimpleDraweeView by bindView(R.id.adapter_user_image)
    }
}