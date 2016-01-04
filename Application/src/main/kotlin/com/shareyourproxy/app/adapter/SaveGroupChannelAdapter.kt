package com.shareyourproxy.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.factory.GroupFactory
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.StringUtils.capitalize
import java.util.*

/**
 * Add a new channel to groups after its made.
 */
internal final class SaveGroupChannelAdapter(recyclerView: BaseRecyclerView, groups: HashMap<String, Group>) : SortedRecyclerAdapter<GroupToggle>(GroupToggle::class.java, recyclerView), ItemClickListener {
    private val publicGroup = GroupToggle(GroupFactory.createPublicGroup(), false)
    internal val dataArray: ArrayList<GroupToggle> get() {
        val groups = data
        if (!groups.isEmpty()) {
            groups.remove(publicGroup)
        }
        return groups
    }

    internal val isAnyItemChecked: Boolean get() {
        val groups = data
        for (group in groups) {
            if (group.isChecked) {
                return true
            }
        }
        return false
    }

    //public should always be the last item
    val isPublicChecked: Boolean get() = lastItem.isChecked

    init {
        val groupToggles : ArrayList<GroupToggle> = ArrayList(groups.size)
        for (group in groups.entries) {
            val newEntry = GroupToggle(group.value, false)
            groupToggles.add(newEntry)
        }
        groupToggles.add(publicGroup)
        refreshGroupToggleData(groupToggles)
    }

    private fun refreshGroupToggleData(groupToggles: ArrayList<GroupToggle>) {
        refreshData(groupToggles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_user_groups_checklist, parent, false)
        return ContentViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        bindContentView(holder as ContentViewHolder, position)
    }

    private fun bindContentView(holder: ContentViewHolder, position: Int) {
        holder.checkedTextView.text = capitalize(getItemData(position).group.label)
        holder.checkedTextView.isChecked = getItemData(position).isChecked
    }

    override fun compare(item1: GroupToggle, item2: GroupToggle): Int {
        if (item1 == publicGroup) {
            return 1
        } else if (item2 == publicGroup) {
            return -1
        }
        val label1 = item1.group.label
        val label2 = item2.group.label
        return label1.compareTo(label2, ignoreCase = true)
    }

    override fun areContentsTheSame(item1: GroupToggle, item2: GroupToggle): Boolean {
        return item1.group == item2.group && item1.isChecked == item2.isChecked
    }

    override fun areItemsTheSame(item1: GroupToggle, item2: GroupToggle): Boolean {
        return item1.group == item2.group
    }

    override fun onItemClick(view: View, position: Int) {
        val text: CheckedTextView = view.findViewById(R.id.adapter_user_groups_textview) as CheckedTextView
        text.isChecked = !text.isChecked
        val group = getItemData(position)
        group.isChecked = text.isChecked
    }

    /**
     * ViewHolder for the entered [Group] data.
     * @param view the inflated view
     */
    private final class ContentViewHolder(view: View, listener: ItemClickListener) : BaseViewHolder(view, listener) {
        val checkedTextView: CheckedTextView by bindView(R.id.adapter_user_groups_textview)
    }
}
