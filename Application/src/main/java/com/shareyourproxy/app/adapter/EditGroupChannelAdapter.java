package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.util.SortedList;
import android.support.v7.util.SortedList.Callback;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelToggle;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.RxGroupChannelSync;
import com.shareyourproxy.api.rx.event.ViewGroupContactsEvent;
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType;
import com.shareyourproxy.util.ObjectUtils;
import com.shareyourproxy.widget.DismissibleNotificationCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindString;
import timber.log.Timber;

import static com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.EDIT_GROUP;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.PUBLIC_GROUPS;

public class EditGroupChannelAdapter extends BaseRecyclerViewAdapter {
    public static final int TYPE_LIST_ITEM = 1;
    public static final int TYPE_LIST_HEADER = 2;
    public static final int TYPE_LIST_DELETE_FOOTER = 3;
    private final GroupEditType _groupEditType;
    private final RxBusDriver _rxBus = RxBusDriver.INSTANCE;
    private ItemClickListener _clickListener;
    private Callback<ChannelToggle> _sortedListCallback;
    private SortedList<ChannelToggle> _channels;
    private String _groupLabel;
    private TextWatcher _textWatcher = getTextWatcher();
    private HeaderViewHolder _groupLabelHeaderViewHolder;

    public EditGroupChannelAdapter(
        ItemClickListener listener, String groupLabel, HashMap<String, Channel> userChannels,
        HashSet<String> groupChannels, GroupEditType groupEditType) {
        _clickListener = listener;
        _groupLabel = groupLabel == null ? "" : groupLabel;
        _groupEditType = groupEditType;
        _channels = new SortedList<>(
            ChannelToggle.class, getSortedCallback(), userChannels.size());
        if (_groupEditType.equals(GroupEditType.PUBLIC_GROUP)) {
            updatePublicChannels(userChannels);
        } else {
            updateChannels(userChannels, groupChannels);
        }
    }

    public static EditGroupChannelAdapter newInstance(
        ItemClickListener listener, String groupLabel, HashMap<String, Channel> userChannels,
        HashSet<String> groupChannels, GroupEditType groupEditType) {
        return new EditGroupChannelAdapter(listener, groupLabel,
            userChannels, groupChannels, groupEditType);
    }

    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                _groupLabel = s.toString();
            }
        };
    }

    private void updateChannels(
        HashMap<String, Channel> userChannels, HashSet<String> groupChannels) {
        if (userChannels != null) {
            ArrayList<ChannelToggle> channelToggles = new ArrayList<>();
            for (Map.Entry<String, Channel> userChannel : userChannels.entrySet()) {
                channelToggles.add(
                    new ChannelToggle(userChannel.getValue(),
                        channelInGroup(userChannel.getValue(), groupChannels)));
            }
            _channels.beginBatchedUpdates();
            for (ChannelToggle channel : channelToggles) {
                _channels.add(channel);
            }
            _channels.endBatchedUpdates();
        }
    }

    private void updatePublicChannels(HashMap<String, Channel> userChannels) {
        if (userChannels != null) {
            ArrayList<ChannelToggle> channelToggles = new ArrayList<>();
            for (Map.Entry<String, Channel> userChannel : userChannels.entrySet()) {
                Channel channel = userChannel.getValue();
                channelToggles.add(
                    new ChannelToggle(channel, channel.isPublic()));
            }
            _channels.beginBatchedUpdates();
            for (ChannelToggle channel : channelToggles) {
                _channels.add(channel);
            }
            _channels.endBatchedUpdates();
        }
    }

    private boolean channelInGroup(Channel userChannel, HashSet<String> groupChannels) {
        if (groupChannels != null) {
            for (String groupChannel : groupChannels) {
                if (groupChannel.equals(userChannel.id())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Callback<ChannelToggle> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new Callback<ChannelToggle>() {

                @Override
                public int compare(ChannelToggle item1, ChannelToggle item2) {
                    int weight1 = item1.getChannel().channelType().getWeight();
                    int weight2 = item2.getChannel().channelType().getWeight();
                    int compareFirst = ObjectUtils.compare(weight1, weight2);
                    if (compareFirst == 0) {
                        return item1.getChannel().label()
                            .compareToIgnoreCase(item2.getChannel().label());
                    } else {
                        return compareFirst;
                    }
                }

                @Override
                public void onInserted(int position, int count) {
                    notifyItemRangeInserted(position, count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    notifyItemRangeRemoved(position, count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    notifyItemMoved(fromPosition, toPosition);
                }

                @Override
                public void onChanged(int position, int count) {
                    notifyItemRangeChanged(position, count);
                }

                @Override
                public boolean areContentsTheSame(
                    ChannelToggle item1, ChannelToggle item2) {
                    return item1.getChannel().id().equals(item2.getChannel().id());
                }

                @Override
                public boolean areItemsTheSame(ChannelToggle item1, ChannelToggle item2) {
                    return item1.getChannel().id().equals(item2.getChannel().id());
                }
            };
        }
        return _sortedListCallback;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_LIST_HEADER;
        } else if (_groupEditType.equals(EDIT_GROUP) && position == getListLength()) {
            return TYPE_LIST_DELETE_FOOTER;
        } else {
            return TYPE_LIST_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return getListSize();
    }

    /**
     * Get the list items length
     *
     * @return list length
     */
    private int getListLength() {
        return getExtraItemsCount() - 1;
    }

    /**
     * Get the list items size
     *
     * @return list size
     */
    private int getListSize() {
        return getExtraItemsCount();
    }

    private int getExtraItemsCount() {
        int count = 0;
        if (_groupEditType.equals(EDIT_GROUP)) {
            // show the header EditTextView and footer delete Button
            count = 2;
        } else if (_groupEditType.equals(GroupEditType.ADD_GROUP) ||
            _groupEditType.equals(GroupEditType.PUBLIC_GROUP)) {
            // just show the header EditTextView
            count = 1;
        }
        return _channels.size() + count;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_LIST_HEADER) {
            if (_groupEditType.equals(GroupEditType.PUBLIC_GROUP)) {
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_dismissible_notification, parent, false);
                return PublicHeaderViewHolder.newInstance(view, _clickListener);
            } else {
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_edit_group_channel_header, parent, false);
                return HeaderViewHolder.newInstance(view, _clickListener);
            }
        } else if (viewType == TYPE_LIST_DELETE_FOOTER) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_edit_group_channel_footer, parent, false);
            return FooterViewHolder.newInstance(view, _clickListener);
        } else {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_edit_group_channel_item, parent, false);
            return ItemViewHolder.newInstance(view, _clickListener);
        }

    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_LIST_HEADER) {
            if (_groupEditType.equals(GroupEditType.PUBLIC_GROUP)) {
                bindPublicHeaderViewData((PublicHeaderViewHolder) holder);
            } else {
                bindHeaderViewData((HeaderViewHolder) holder);
            }
        } else if (holder.getItemViewType() == TYPE_LIST_ITEM &&
            (!_groupEditType.equals(GroupEditType.EDIT_GROUP) ||
                (position != getListLength()))) {
            bindItemViewData((ItemViewHolder) holder, getItemData(position - 1));
        }
    }

    public String getGroupLabel() {
        return _groupLabel;
    }

    public void bindPublicHeaderViewData(PublicHeaderViewHolder holder) {
        holder.notificationCard.createNotificationCard(this, holder, PUBLIC_GROUPS, false, false);
    }

    private void bindHeaderViewData(HeaderViewHolder holder) {
        Context context = holder.view.getContext();
        _groupLabelHeaderViewHolder = holder;
        holder.editText.setText(_groupLabel);
        holder.editText.addTextChangedListener(_textWatcher);
        int end = holder.title.length();
        SpannableStringBuilder sb = new SpannableStringBuilder(holder.title)
            .append("\n").append(holder.button.toUpperCase(Locale.US));
        TextAppearanceSpan titleSpan = new TextAppearanceSpan(context, R.style
            .Proxy_TextAppearance_Body_Disabled);
        TextAppearanceSpan buttonSpan = new TextAppearanceSpan(context, R.style
            .Proxy_TextAppearance_Button_Blue);
        sb.setSpan(titleSpan, 0, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(buttonSpan, end + 1, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        holder.textViewContacts.setText(sb);
        holder.textViewContacts.setOnClickListener(getContactsListener());
    }

    private View.OnClickListener getContactsListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _rxBus.post(new ViewGroupContactsEvent());
            }
        };
    }

    public ChannelToggle getItemData(int position) {
        if (position < _channels.size()) {
            return _channels.get(position);
        } else {
            return null;
        }
    }

    public void promptGroupLabelError(Context context) {
        _groupLabelHeaderViewHolder.
            textInputLayout.setError(context.getString(R.string.required));
        _groupLabelHeaderViewHolder.textInputLayout.setErrorEnabled(true);
    }

    private void bindItemViewData(ItemViewHolder holder, ChannelToggle editChannel) {
        Context context = holder.view.getContext();
        ChannelType channelType = editChannel.getChannel().channelType();
        Channel channel = editChannel.getChannel();
        String channelTypeString = editChannel.getChannel().channelType().getLabel();
        String label = editChannel.getChannel().label();
        String address = editChannel.getChannel().actionAddress();
        SpannableStringBuilder sb = getChannelSpannableStringBuilder(
            context, channelTypeString, label, address);

        holder.itemImage.setImageDrawable(
            getChannelIconDrawable(context, channel, getChannelBackgroundColor(context,
                channelType)));

        View.OnClickListener clickListener = switchListener(holder);
        CompoundButton.OnCheckedChangeListener checkedListener = checkedListener(holder);

        holder.itemLabel.setText(sb);
        holder.container.setOnClickListener(clickListener);
        holder.itemSwitch.setOnCheckedChangeListener(checkedListener);
        holder.itemSwitch.setChecked(editChannel.inGroup());
    }

    private View.OnClickListener switchListener(final ItemViewHolder viewHolder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewHolder.getItemPosition() - 1;
                Switch itemSwitch = viewHolder.itemSwitch;
                Timber.i("position: %1$s", position);

                boolean toggle = !itemSwitch.isChecked();
                itemSwitch.setChecked(toggle);
            }
        };
    }

    private CompoundButton.OnCheckedChangeListener checkedListener(
        final ItemViewHolder
            viewHolder) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = viewHolder.getItemPosition() - 1;
                ChannelToggle channelToggle = _channels.get(position);
                channelToggle.setInGroup(isChecked);
            }
        };
    }

    public HashSet<String> getSelectedChannels() {
        return RxGroupChannelSync.INSTANCE.getSelectedChannels(_channels);
    }

    public ArrayList<ChannelToggle> getToggledChannels() {
        ArrayList<ChannelToggle> channels = new ArrayList<>(_channels.size());
        for (int i = 0; i < _channels.size(); i++) {
            ChannelToggle newChannel = _channels.get(i);
            channels.add(newChannel);
        }
        return channels;
    }

    public static final class ItemViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_edit_group_list_item_container)
        RelativeLayout container;
        @Bind(R.id.adapter_edit_group_list_item_switch)
        Switch itemSwitch;
        @Bind(R.id.adapter_edit_group_list_item_image)
        ImageView itemImage;
        @Bind(R.id.adapter_edit_group_list_item_label)
        TextView itemLabel;

        private ItemViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static ItemViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new ItemViewHolder(view, itemClickListener);
        }

    }

    public static final class PublicHeaderViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_dismissible_notification_card)
        DismissibleNotificationCard notificationCard;

        private PublicHeaderViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static PublicHeaderViewHolder newInstance(
            View view, ItemClickListener itemClickListener) {
            return new PublicHeaderViewHolder(view, itemClickListener);
        }

    }

    public static final class HeaderViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_group_edit_channel_header_edittext)
        EditText editText;
        @Bind(R.id.adapter_group_edit_channel_header_floatlabel)
        TextInputLayout textInputLayout;
        @Bind(R.id.adapter_group_edit_channel_header_contacts_button)
        TextView textViewContacts;
        @BindString(R.string.people_in_this_group)
        String title;
        @BindString(R.string.view_group_members)
        String button;

        private HeaderViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static HeaderViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new HeaderViewHolder(view, itemClickListener);
        }

    }

    public static final class FooterViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_group_edit_channel_footer_delete)
        Button deleteButton;

        private FooterViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static FooterViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new FooterViewHolder(view, itemClickListener);
        }

    }
}
