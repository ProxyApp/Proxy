package com.shareyourproxy.app.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco.newDraweeControllerBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ViewUtils.getAlphaOverlayHierarchy
import com.shareyourproxy.util.ViewUtils.getMenuIconDark
import com.shareyourproxy.util.ViewUtils.getUserImageHierarchy
import com.shareyourproxy.util.bindView
import java.util.Arrays.asList

/**
 * Adapter to handle creating a drawer with a User Header and User Settings.
 */
class DrawerAdapter
/**
 * Constructor for [DrawerAdapter].
 * @param currentUser currently logged in User
 * @param clickListener click listener
 */
private constructor(private var currentUser: User, private val clickListener: ItemClickListener) : BaseRecyclerViewAdapter() {
    private val drawerItems: List<DrawerItem>

    init {
        drawerItems = asList(
                DrawerItem.SHARE_PROFILE,
                DrawerItem.INVITE_FRIEND,
                DrawerItem.TOUR,
                DrawerItem.REPORT_ISSUE,
                DrawerItem.LOGOUT)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_drawer_header, parent, false)
            return HeaderViewHolder.newInstance(view, clickListener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_drawer_item, parent, false)
            return ItemViewHolder.newInstance(view, clickListener)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            bindHeaderViewHolder(holder)
        } else {
            bindItemViewHolder(holder as ItemViewHolder, position)
        }
    }

    /**
     * Bind Item View. Set the text of the menu option.
     * @param holder   view holder
     * @param position in list
     */
    fun bindItemViewHolder(holder: ItemViewHolder, position: Int) {
        val context = holder.view.context
        val name = getItemStringValue(context, position)
        val resId = getItemIconValue(position)

        holder.name.text = name
        holder.image.setImageDrawable(getMenuIconDark(context, resId))
    }

    /**
     * Bind a header view. Create a user profile and background with a title.
     * @param holder view holder
     */
    fun bindHeaderViewHolder(holder: HeaderViewHolder) {
        val context = holder.view.context
        val res = holder.view.resources
        holder.userName.text = currentUser.fullName

        val profileURL = currentUser.profileURL
        holder.userImage.hierarchy = getUserImageHierarchy(context)
        holder.userImage.controller = newDraweeControllerBuilder().setUri(Uri.parse(profileURL)).build()

        val coverURL = currentUser.coverURL
        holder.backgroundImage.hierarchy = getAlphaOverlayHierarchy(holder.backgroundImage, res)
        holder.backgroundImage.controller = newDraweeControllerBuilder().setUri(Uri.parse(coverURL)).build()
    }

    private fun getItemStringValue(context: Context, position: Int): String {
        return context.getString(drawerItems[position - 1].labelRes)
    }

    private fun getItemIconValue(position: Int): Int {
        return drawerItems[position - 1].resId
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_LIST_ITEM
    }

    override fun getItemCount(): Int {
        // +1 for the header
        return drawerItems.size + 1
    }

    /**
     * Get Settings name.
     * @param position position of item
     * @return _strings string
     */
    fun getSettingValue(position: Int): DrawerItem {
        return if ((position == 0)) DrawerItem.HEADER else drawerItems[position - 1]
    }

    /**
     * Update the logged in user.
     * @param user updated user
     */
    fun updateUser(user: User) {
        currentUser = user
        notifyDataSetChanged()
    }

    enum class DrawerItem private constructor(val resId: Int, val labelRes: Int) {
        HEADER(R.raw.ic_chameleon, R.string.header),
        PROFILE(R.raw.ic_account_circle, R.string.profile),
        SHARE_PROFILE(R.raw.ic_share, R.string.share_profile),
        INVITE_FRIEND(R.raw.ic_local_play, R.string.invite_a_friend),
        TOUR(R.raw.ic_explore, R.string.tour),
        REPORT_ISSUE(R.raw.ic_bug_report, R.string.report_issue),
        LOGOUT(R.raw.ic_exit_to_app, R.string.logout)
    }

    /**
     * ViewHolder for the settings header.
     */
    class HeaderViewHolder
    /**
     * Constructor for the HeaderViewHolder.
     * @param view              the inflated view
     * @param itemClickListener click listener for this view
     */
    private constructor(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val backgroundImage: SimpleDraweeView by bindView(R.id.adapter_drawer_header_background)
        val userImage: SimpleDraweeView by bindView(R.id.adapter_drawer_header_image)
        val userName: TextView by bindView(R.id.adapter_drawer_header_name)

        companion object {

            /**
             * Create a new Instance of the ViewHolder.
             * @param view inflated in [Adapter.onCreateViewHolder]
             * @param itemClickListener click listener for this view
             * @return a ViewHolder instance
             */
            fun newInstance(view: View, itemClickListener: ItemClickListener): HeaderViewHolder {
                return HeaderViewHolder(view, itemClickListener)
            }
        }
    }

    /**
     * ViewHolder for the entered settings data.
     */
    class ItemViewHolder
    /**
     * Constructor for the ItemViewHolder.
     * @param view              the inflated view
     * @param itemClickListener click listener for this view
     */
    private constructor(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val name: TextView by bindView(R.id.adapter_drawer_item_name)
        val image: ImageView by bindView(R.id.adapter_drawer_item_image)

        companion object {

            /**
             * Create a new Instance of the ViewHolder.
             * @param view              inflated in [Adapter.onCreateViewHolder]
             * @param itemClickListener click listener for this view
             * @return a ViewHolder instance
             */
            fun newInstance(view: View, itemClickListener: ItemClickListener): ItemViewHolder {
                return ItemViewHolder(view, itemClickListener)
            }
        }
    }

    companion object {
        private val TYPE_HEADER = 0
        private val TYPE_LIST_ITEM = 1

        /**
         * Create a newInstance of a [DrawerAdapter] with blank data.
         * @param currentUser currently Logged in [User]
         * @return an [DrawerAdapter] with no data
         */
        fun newInstance(currentUser: User, listener: ItemClickListener): DrawerAdapter {
            return DrawerAdapter(currentUser, listener)
        }
    }
}
