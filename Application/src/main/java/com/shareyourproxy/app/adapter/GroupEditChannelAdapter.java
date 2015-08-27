package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v7.util.SortedList;
import android.support.v7.util.SortedList.Callback;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.GroupEditChannel;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.rx.RxGroupChannelSync;
import com.shareyourproxy.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindColor;
import timber.log.Timber;

import static com.shareyourproxy.app.GroupEditChannelActivity.ADD_GROUP;
import static com.shareyourproxy.app.GroupEditChannelActivity.EDIT_GROUP;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

public class GroupEditChannelAdapter extends BaseRecyclerViewAdapter {
    public static final int TYPE_LIST_ITEM = 1;
    public static final int TYPE_LIST_EDIT_TEXT = 2;
    public static final int TYPE_LIST_DELETE = 3;
    private final int _addOrEdit;
    @BindColor(R.color.common_text_disabled)
    protected int _gray;
    private ItemClickListener _clickListener;
    private Callback<GroupEditChannel> _sortedListCallback;
    private SortedList<GroupEditChannel> _channels;
    private String _groupLabel;
    private TextWatcher _textWatcher = getTextWatcher();

    public GroupEditChannelAdapter(
        ItemClickListener listener, String groupLabel, HashMap<String, Channel> userChannels,
        HashMap<String, Id> groupChannels, int addOrEdit) {
        _clickListener = listener;
        _groupLabel = groupLabel;
        _addOrEdit = addOrEdit;
        _channels = new SortedList<>(
            GroupEditChannel.class, getSortedCallback(), userChannels.size());
        updateChannels(userChannels, groupChannels);
    }

    public static GroupEditChannelAdapter newInstance(
        ItemClickListener listener, String groupLabel, HashMap<String, Channel> userChannels,
        HashMap<String, Id> groupChannels, int addOrEdit) {
        return new GroupEditChannelAdapter(listener, groupLabel,
            userChannels, groupChannels, addOrEdit);
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
        HashMap<String, Channel> userChannels, HashMap<String, Id> groupChannels) {
        if (userChannels != null) {
            ArrayList<GroupEditChannel> groupEditChannels = new ArrayList<>();
            for (Map.Entry<String, Channel> userChannel : userChannels.entrySet()) {
                groupEditChannels.add(
                    new GroupEditChannel(userChannel.getValue(),
                        channelInGroup(userChannel.getValue(), groupChannels)));
            }
            _channels.beginBatchedUpdates();
            for (GroupEditChannel channel : groupEditChannels) {
                _channels.add(channel);
            }
            _channels.endBatchedUpdates();
        }
    }

    private boolean channelInGroup(Channel userChannel, HashMap<String, Id> groupChannels) {
        if (groupChannels != null) {
            for (Map.Entry<String, Id> groupChannel : groupChannels.entrySet()) {
                if (groupChannel.getKey().equals(userChannel.id().value())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Callback<GroupEditChannel> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new Callback<GroupEditChannel>() {

                @Override
                public int compare(GroupEditChannel item1, GroupEditChannel item2) {
                    int weight1 = item1.getChannel().channelType().getWeight();
                    int weight2 = item2.getChannel().channelType().getWeight();
                    int compareFirst = ObjectUtils.compare(weight1, weight2);
                    if (compareFirst == 0) {
                        return item1.getChannel().label().compareTo(item2.getChannel().label());
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
                    GroupEditChannel item1, GroupEditChannel item2) {
                    return item1.getChannel().id().equals(item2.getChannel().id());
                }

                @Override
                public boolean areItemsTheSame(GroupEditChannel item1, GroupEditChannel item2) {
                    return item1.getChannel().id().equals(item2.getChannel().id());
                }
            };
        }
        return _sortedListCallback;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_LIST_EDIT_TEXT;
        } else if (_addOrEdit == EDIT_GROUP && position == getListSize() - 1) {
            return TYPE_LIST_DELETE;
        } else {
            return TYPE_LIST_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return getListSize();
    }

    /**
     * We add addOrEdit to avoid a delete button on add(0) and require it on edit(1)
     *
     * @return list size
     */
    public int getListSize() {
        return _channels.size() + 1 + _addOrEdit;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LIST_EDIT_TEXT) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_edit_group_channel_header, parent, false);
            return HeaderViewHolder.newInstance(view, _clickListener);
        } else if (viewType == TYPE_LIST_DELETE) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_edit_group_channel_footer, parent, false);
            return FooterViewHolder.newInstance(view, _clickListener);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_edit_group_channel_item, parent, false);
            return ItemViewHolder.newInstance(view, _clickListener);
        }

    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_LIST_EDIT_TEXT) {
            bindHeaderViewData(
                (HeaderViewHolder) holder);
        } else if (holder.getItemViewType() == TYPE_LIST_ITEM
            && (_addOrEdit == ADD_GROUP || (position != getItemCount() - 1))) {
            bindItemViewData(
                (ItemViewHolder) holder, getItemData(position - 1));
        }
    }

    public String getGroupLabel() {
        return _groupLabel;
    }

    private void bindHeaderViewData(HeaderViewHolder holder) {
        holder.editText.setText(_groupLabel);
        holder.editText.addTextChangedListener(_textWatcher);
    }

    public GroupEditChannel getItemData(int position) {
        if (position < _channels.size()) {
            return _channels.get(position);
        } else {
            return null;
        }
    }

    private void bindItemViewData(ItemViewHolder holder, GroupEditChannel editChannel) {
        Context context = holder._view.getContext();
        ChannelType channelType = editChannel.getChannel().channelType();
        holder.itemImage.setImageDrawable(
            getSVGIconDrawable(context, editChannel.getChannel(),
                getChannelBackgroundColor(context, channelType)));

        String channelTypeString = editChannel.getChannel().channelType().getLabel();
        String label = editChannel.getChannel().label();
        String address = editChannel.getChannel().actionAddress();

        SpannableStringBuilder sb = getSpannableStringBuilder(context, channelTypeString, label,
            address);

        View.OnClickListener clickListener = switchListener(holder);
        CompoundButton.OnCheckedChangeListener checkedListener = checkedListener(holder);

        holder.itemLabel.setText(sb);
        holder.itemSwitch.setChecked(editChannel.inGroup());

        holder.container.setOnClickListener(clickListener);
        holder.itemSwitch.setOnCheckedChangeListener(checkedListener);
    }

    private SpannableStringBuilder getSpannableStringBuilder(
        Context context, String
        channelTypeString, String label, String address) {
        SpannableStringBuilder sb;
        int start;
        int end;
        if (label.length() > 0) {
            sb = new SpannableStringBuilder(
                context.getString(R.string.channel_view_item_content,
                    channelTypeString, label, address));
            start = channelTypeString.length() + label.length() + 3;
            end = sb.length();
            sb.setSpan(new ForegroundColorSpan(Color.LTGRAY), start, end,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            sb = new SpannableStringBuilder(
                context.getString(R.string.channel_view_item_content_no_label,
                    channelTypeString, address));
            start = channelTypeString.length();
            end = sb.length();

        }
        sb.setSpan(new ForegroundColorSpan(Color.LTGRAY), start, end,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

    private View.OnClickListener switchListener(final ItemViewHolder viewHolder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewHolder.getItemPosition() - 1;
                Switch itemSwitch = viewHolder.itemSwitch;
                Timber.i("position:" + position);

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
                _channels.get(position)
                    .setInGroup(isChecked);
            }
        };
    }

    public HashMap<String, Id> getSelectedChannels() {
        return RxGroupChannelSync.getSelectedChannels(_channels);
    }

    public static final class ItemViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_edit_group_list_item_container)
        public RelativeLayout container;
        @Bind(R.id.adapter_edit_group_list_item_switch)
        public Switch itemSwitch;
        @Bind(R.id.adapter_edit_group_list_item_image)
        protected ImageView itemImage;
        @Bind(R.id.adapter_edit_group_list_item_label)
        protected TextView itemLabel;

        private ItemViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static ItemViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new ItemViewHolder(view, itemClickListener);
        }

    }

    public static final class HeaderViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_group_edit_channel_header_edittext)
        protected EditText editText;
        @Bind(R.id.adapter_group_edit_channel_header_floatlabel)
        protected TextInputLayout textInputLayout;

        private HeaderViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static HeaderViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new HeaderViewHolder(view, itemClickListener);
        }

    }

    public static final class FooterViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_group_edit_channel_footer_delete)
        protected Button deleteButton;

        private FooterViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static FooterViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new FooterViewHolder(view, itemClickListener);
        }

    }
}
