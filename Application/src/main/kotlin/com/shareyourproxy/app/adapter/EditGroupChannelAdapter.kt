package com.shareyourproxy.app.adapter

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.v7.util.SortedList
import android.support.v7.util.SortedList.Callback
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.shareyourproxy.R
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.string.required
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelToggle
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxGroupChannelSync.getSelectedChannels
import com.shareyourproxy.api.rx.event.ViewGroupContactsEvent
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.EDIT_GROUP
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.widget.DismissibleNotificationCard
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.PUBLIC_GROUPS
import timber.log.Timber
import java.util.*

class EditGroupChannelAdapter(private val recyclerView: BaseRecyclerView, private val clickListener: ItemClickListener, internal var groupLabel: String, userChannels: HashMap<String, Channel>, groupChannels: HashSet<String>, private val groupEditType: GroupEditType) : BaseRecyclerViewAdapter() {
    companion object{
        internal val TYPE_LIST_ITEM = 1
        internal val TYPE_LIST_HEADER = 2
        internal val TYPE_LIST_DELETE_FOOTER = 3
    }
    private val channels: SortedList<ChannelToggle> = SortedList(ChannelToggle::class.java, sortedCallback, userChannels.size)
    private val stringTitle: String by bindString(recyclerView.context, R.string.people_in_this_group)
    private val stringButton: String by bindString(recyclerView.context, R.string.view_group_members)
    private val contactsListener: View.OnClickListener = View.OnClickListener { post(ViewGroupContactsEvent()) }
    internal val selectedChannels: HashSet<String> get()= getSelectedChannels(channels)
    private var groupLabelHeaderViewHolder: HeaderViewHolder? = null

    init {
        if (groupEditType == GroupEditType.PUBLIC_GROUP) {
            updatePublicChannels(userChannels)
        } else {
            updateChannels(userChannels, groupChannels)
        }
    }

    private val textWatcher: TextWatcher get() = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            groupLabel = s.toString()
        }
    }

    /**
     * Get the list items length
     * @return list length
     */
    private val listLength: Int = extraItemsCount - 1

    private val extraItemsCount: Int get() {
        var count = 0
        if (groupEditType == EDIT_GROUP) {
            count = 2
        } else if (groupEditType == GroupEditType.ADD_GROUP || groupEditType == GroupEditType.PUBLIC_GROUP) {
            count = 1
        }
        return channels.size() + count
    }

    internal val toggledChannels: ArrayList<ChannelToggle> get() {
        val channels = ArrayList<ChannelToggle>(channels.size())
        for (i in 0..channels.size - 1) {
            val newChannel = channels[i]
            channels.add(newChannel)
        }
        return channels
    }

    private val sortedCallback: Callback<ChannelToggle> get() = object : Callback<ChannelToggle>() {
        override fun compare(item1: ChannelToggle, item2: ChannelToggle): Int {
            val weight1 = item1.channel.channelType.weight
            val weight2 = item2.channel.channelType.weight
            val compareFirst = compareValues(weight1, weight2)
            if (compareFirst == 0) {
                return item1.channel.label.compareTo(item2.channel.label, true)
            } else {
                return compareFirst
            }
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            notifyItemRangeChanged(position, count)
        }

        override fun areContentsTheSame(
                item1: ChannelToggle, item2: ChannelToggle): Boolean {
            return item1.channel.id.equals(item2.channel.id)
        }

        override fun areItemsTheSame(item1: ChannelToggle, item2: ChannelToggle): Boolean {
            return item1.channel.id.equals(item2.channel.id)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_LIST_HEADER
        } else if (groupEditType == EDIT_GROUP && position == listLength) {
            return TYPE_LIST_DELETE_FOOTER
        } else {
            return TYPE_LIST_ITEM
        }
    }

    override fun getItemCount(): Int {
        return extraItemsCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view: View
        if (viewType == TYPE_LIST_HEADER) {
            if (groupEditType == GroupEditType.PUBLIC_GROUP) {
                view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_dismissible_notification, parent, false)
                return PublicHeaderViewHolder(view, clickListener)
            } else {
                view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_edit_group_channel_header, parent, false)
                return HeaderViewHolder(view, clickListener)
            }
        } else if (viewType == TYPE_LIST_DELETE_FOOTER) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_edit_group_channel_footer, parent, false)
            return FooterViewHolder(view, clickListener)
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_edit_group_channel_item, parent, false)
            return ItemViewHolder(view, clickListener)
        }

    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_LIST_HEADER) {
            if (groupEditType == GroupEditType.PUBLIC_GROUP) {
                bindPublicHeaderViewData(holder as PublicHeaderViewHolder)
            } else {
                bindHeaderViewData(holder as HeaderViewHolder)
            }
        } else if (holder.itemViewType == TYPE_LIST_ITEM && (groupEditType != GroupEditType.EDIT_GROUP || (position != listLength))) {
            bindItemViewData(holder as ItemViewHolder, getItemData(position - 1))
        }
    }

    private fun updateChannels(userChannels: HashMap<String, Channel>?, groupChannels: HashSet<String>) {
        if (userChannels != null) {
            val channelToggles = ArrayList<ChannelToggle>()
            for (userChannel in userChannels.entries) {
                channelToggles.add(ChannelToggle(userChannel.value, channelInGroup(userChannel.value, groupChannels)))
            }
            channels.beginBatchedUpdates()
            for (channel in channelToggles) {
                channels.add(channel)
            }
            channels.endBatchedUpdates()
        }
    }

    private fun updatePublicChannels(userChannels: HashMap<String, Channel>?) {
        if (userChannels != null) {
            val channelToggles = ArrayList<ChannelToggle>()
            for (userChannel in userChannels.entries) {
                val channel = userChannel.value
                channelToggles.add(
                        ChannelToggle(channel, channel.isPublic))
            }
            channels.beginBatchedUpdates()
            for (channel in channelToggles) {
                channels.add(channel)
            }
            channels.endBatchedUpdates()
        }
    }

    private fun channelInGroup(userChannel: Channel, groupChannels: HashSet<String>?): Boolean {
        if (groupChannels != null) {
            for (groupChannel in groupChannels) {
                if (groupChannel == userChannel.id) {
                    return true
                }
            }
        }
        return false
    }

    private fun bindPublicHeaderViewData(holder: PublicHeaderViewHolder) {
        holder.notificationCard.createNotificationCard(this, holder, PUBLIC_GROUPS, false, false)
    }

    private fun bindHeaderViewData(holder: HeaderViewHolder) {
        val context = holder.view.context
        groupLabelHeaderViewHolder = holder
        holder.editText.setText(groupLabel)
        holder.editText.addTextChangedListener(textWatcher)
        val end = stringTitle.length
        val sb = SpannableStringBuilder(stringTitle).append("\n").append(stringButton.toUpperCase(Locale.US))
        val titleSpan = TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body_Disabled)
        val buttonSpan = TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Button_Blue)
        sb.setSpan(titleSpan, 0, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        sb.setSpan(buttonSpan, end + 1, sb.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        holder.textViewContacts.text = sb
        holder.textViewContacts.setOnClickListener(contactsListener)
    }

    internal fun getItemData(position: Int): ChannelToggle {
        return channels.get(position)
    }

    internal fun promptGroupLabelError(context: Context) {
        groupLabelHeaderViewHolder!!.textInputLayout.error = context.getString(required)
        groupLabelHeaderViewHolder!!.textInputLayout.isErrorEnabled = true
    }

    private fun bindItemViewData(holder: ItemViewHolder, editChannel: ChannelToggle) {
        val context = holder.view.context
        val channelType = editChannel.channel.channelType
        val channel = editChannel.channel
        val channelTypeString = editChannel.channel.channelType.label
        val label = editChannel.channel.label
        val address = editChannel.channel.actionAddress
        val sb = getChannelSpannableStringBuilder(context, channelTypeString, label, address)
        val clickListener = switchListener(holder)
        val checkedListener = checkedListener(holder)

        holder.itemImage.setImageDrawable(getChannelDrawable(channel, channelType, context))
        holder.itemLabel.text = sb
        holder.container.setOnClickListener(clickListener)
        holder.itemSwitch.setOnCheckedChangeListener(checkedListener)
        holder.itemSwitch.isChecked = editChannel.inGroup
    }

    private fun switchListener(viewHolder: ItemViewHolder): View.OnClickListener {
        return View.OnClickListener {
            val position = viewHolder.itemPosition - 1
            val itemSwitch = viewHolder.itemSwitch
            Timber.i("position: $position")

            val toggle = !itemSwitch.isChecked
            itemSwitch.isChecked = toggle
        }
    }

    private fun checkedListener(viewHolder: ItemViewHolder): CompoundButton.OnCheckedChangeListener {
        return CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            val position = viewHolder.itemPosition - 1
            val channelToggle = channels.get(position)
            channelToggle.inGroup = isChecked
        }
    }

    private final class ItemViewHolder(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val container: RelativeLayout by bindView(adapter_edit_group_list_item_container)
        val itemSwitch: Switch by bindView(adapter_edit_group_list_item_switch)
        val itemImage: ImageView by bindView(adapter_edit_group_list_item_image)
        val itemLabel: TextView by bindView(adapter_edit_group_list_item_label)
    }

    private final class PublicHeaderViewHolder(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val notificationCard: DismissibleNotificationCard by bindView(adapter_dismissible_notification_card)
    }

    private final class HeaderViewHolder(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val editText: EditText by bindView(adapter_group_edit_channel_header_edittext)
        val textInputLayout: TextInputLayout by bindView(adapter_group_edit_channel_header_floatlabel)
        val textViewContacts: TextView by bindView(adapter_group_edit_channel_header_contacts_button)
    }

    private final class FooterViewHolder(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val deleteButton: Button by bindView(adapter_group_edit_channel_footer_delete)
    }
}
