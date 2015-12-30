package com.shareyourproxy.app.adapter

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.factory.GroupFactory.createPublicGroup
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ObjectUtils.capitalize
import com.shareyourproxy.util.bindView
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.MAIN_GROUPS
import java.util.*

/**
 * An Adapter to handle displaying [Group]s.
 */
class GroupAdapter(recyclerView: BaseRecyclerView, sharedPreferences: SharedPreferences, showHeader: Boolean, private val listener: ItemClickListener) : NotificationRecyclerAdapter<Group>(Group::class.java, recyclerView, showHeader, false, sharedPreferences) {

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is GroupViewHolder) {
            val group = getItemData(position)
            setLineItemViewData(holder, group)
        } else if (holder is NotificationRecyclerAdapter.HeaderViewHolder) {
            bindHeaderViewData(holder, MAIN_GROUPS, true, false)
        }
    }

    /**
     * Set this ViewHolders underlying [Group] data.
     * @param holder [Group] [GroupViewHolder]
     * @param group  the [Group] data
     */
    private fun setLineItemViewData(holder: GroupViewHolder, group: Group) {
        holder.groupName.text = capitalize(group.label)
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.common_adapter_text_item, parent, false)
        return GroupViewHolder(view, listener)
    }

    override fun compare(item1: Group, item2: Group): Int {
        return item1.label.compareTo(item2.label, true)
    }

    override fun areContentsTheSame(item1: Group, item2: Group): Boolean {
        return item1.label.equals(item2.label)
    }

    override fun areItemsTheSame(item1: Group, item2: Group): Boolean {
        return item1.id.equals(item2.id)
    }

    internal fun refreshGroupData(groups: HashMap<String, Group>?) {
        val newGroups: HashMap<String, Group>
        if (groups != null) {
            newGroups = HashMap<String, Group>(groups.size)
            newGroups.putAll(groups)
        } else {
            newGroups = HashMap<String, Group>(1)
        }
        val publicGroup = createPublicGroup()
        newGroups.put(publicGroup.id, publicGroup)
        refreshData(newGroups.values)
    }

    /**
     * ViewHolder for the entered [Group] data.
     * @param view              the inflated view
     * @param itemClickListener click listener for each viewholder item
     */
    private final class GroupViewHolder(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val groupName: TextView by bindView(R.id.adapter_group_name)
    }
}
