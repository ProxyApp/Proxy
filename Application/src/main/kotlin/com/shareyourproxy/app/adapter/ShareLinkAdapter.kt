package com.shareyourproxy.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.StringUtils.capitalize
import java.util.*

/**
 * Display a list of groups to broadcast in a shared link intent.
 */
internal final class ShareLinkAdapter(private val recyclerView: BaseRecyclerView, groups: HashMap<String, Group>) : SortedRecyclerAdapter<GroupToggle>(GroupToggle::class.java, recyclerView), ItemClickListener {
    private var lastCheckedView: CheckedTextView = CheckedTextView(recyclerView.context)

    init {
        val groupToggles: ArrayList<GroupToggle> = ArrayList(groups.size)
        groups.values.forEach { groupToggles.add(GroupToggle(it, false)) }
        refreshData(groupToggles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_share_link, parent, false)
        return ContentViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        bindContentView(holder as ContentViewHolder, position)
    }

    private fun bindContentView(holder: ContentViewHolder, position: Int) {
        holder.checkedTextView.text = capitalize(getItemData(position).group.label)
        holder.checkedTextView.isChecked = getItemData(position).isChecked
    }

    override fun onItemClick(view: View, position: Int) {
        //set data
        clearGroupState()
        getItemData(position).isChecked = true

        //set view
        updateViewState(view)
    }

    fun updateViewState(view: View) {
        //turn off the last checked view and cache
        if (lastCheckedView != view) {
            lastCheckedView.isChecked = false
        }
        //set the selected view checked
        val checkedTextView = (recyclerView.getChildViewHolder(view) as ContentViewHolder).checkedTextView
        checkedTextView.isChecked = true

        lastCheckedView = checkedTextView
    }

    private fun clearGroupState() {
        for (i in 0..itemCount - 1) {
            sortedList.get(i).isChecked = false
        }
    }

    override fun compare(item1: GroupToggle, item2: GroupToggle): Int {
        val group1 = item1.group
        val group2 = item2.group
        return group1.label.compareTo(group2.label, true)
    }

    override fun areContentsTheSame(item1: GroupToggle, item2: GroupToggle): Boolean {
        val group1 = item1.group
        val group2 = item2.group
        return (group1.id.equals(group2.id) && group1.label.equals(group2.label))
    }

    override fun areItemsTheSame(item1: GroupToggle, item2: GroupToggle): Boolean {
        //Sections will have the same ID but different categories
        val group1 = item1.group
        val group2 = item2.group
        return group1.id.equals(group2.id)
    }

    /**
     * Constructor for the holder.
     * @param view inflated in [.onCreateViewHolder]
     * @return a [User] ViewHolder instance
     */
    private final class ContentViewHolder(view: View, listener: ItemClickListener) : BaseViewHolder(view, listener) {
        val checkedTextView: CheckedTextView by bindView(R.id.adapter_share_link_textview)
    }
}
