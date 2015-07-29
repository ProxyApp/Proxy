package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.util.SortedList.Callback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

public class GroupEditChannelAdapter extends BaseRecyclerViewAdapter {
    public static final int TYPE_LIST_ITEM = 1;
    private ItemClickListener _clickListener;
    private Callback<GroupEditChannel> _sortedListCallback;
    private SortedList<GroupEditChannel> _channels;


    public GroupEditChannelAdapter(
        ItemClickListener listener, HashMap<String, Channel> userChannels,
        HashMap<String, Id> groupChannels) {
        _clickListener = listener;
        _channels = new SortedList<>(
            GroupEditChannel.class, getSortedCallback(), userChannels.size());
        updateChannels(userChannels, groupChannels);
    }

    public static GroupEditChannelAdapter newInstance(
        ItemClickListener listener, HashMap<String, Channel> userChannels,
        HashMap<String, Id> groupChannels) {
        return new GroupEditChannelAdapter(listener, userChannels, groupChannels);
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
        return TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        return _channels.size();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_edit_group_item, parent, false);
        return ItemViewHolder.newInstance(view, _clickListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        bindItemViewData(
            (ItemViewHolder) holder, getItemData(position));
    }

    public GroupEditChannel getItemData(int position) {
        return _channels.get(position);
    }

    private void bindItemViewData(ItemViewHolder holder, GroupEditChannel editChannel) {
        Context context = holder._view.getContext();
        ChannelType channelType = editChannel.getChannel().channelType();
            holder.itemImage.setImageDrawable(
                getSVGIconDrawable(context, editChannel.getChannel(),
                    getChannelBackgroundColor(context, channelType)));

        holder.itemLabel.setText(editChannel.getChannel().label());
        holder.itemSwitch.setChecked(editChannel.inGroup());
        holder.itemSwitch.setOnClickListener(switchListener(holder));
    }

    private View.OnClickListener switchListener(final ItemViewHolder viewHolder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _channels.get(viewHolder.getAdapterPosition())
                    .setInGroup(((Switch) view).isChecked());
            }
        };
    }

    public HashMap<String, Id> getSelectedChannels() {
        return RxGroupChannelSync.getSelectedChannels(_channels);
    }

    public static final class ItemViewHolder extends BaseViewHolder {
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
}
