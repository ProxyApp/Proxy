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
import com.shareyourproxy.api.domain.factory.ChannelFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.GroupEditChannel;
import com.shareyourproxy.util.ObjectUtils;

import butterknife.InjectView;

import static com.shareyourproxy.api.domain.model.ChannelType.Custom;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.ViewUtils.getActivityIcon;

public class EditGroupChannelAdapter extends BaseRecyclerViewAdapter {
    //    private static final int TYPE_SECTION_HEADER = 0;
    public static final int TYPE_LIST_ITEM = 1;
    public static final Channel DIALER = ChannelFactory.getPhoneChannel();
    public static final Channel HANGOUTS = ChannelFactory.getSMSChannel();
    public static final Channel GMAIL = ChannelFactory.getEmailChannel();
    public static final Channel WEB = ChannelFactory.getWebChannel();
    private ItemClickListener _clickListener;
    private Callback<GroupEditChannel> _sortedListCallback;
    private SortedList<GroupEditChannel> _channels = new SortedList<>(GroupEditChannel.class,
        getSortedCallback());


    public EditGroupChannelAdapter(ItemClickListener listener) {
        _clickListener = listener;
        _channels.add(GroupEditChannel.create(DIALER, true));
        _channels.add(GroupEditChannel.create(HANGOUTS, true));
        _channels.add(GroupEditChannel.create(GMAIL, true));
        _channels.add(GroupEditChannel.create(WEB, true));
    }

    public static EditGroupChannelAdapter newInstance(ItemClickListener listener) {
        return new EditGroupChannelAdapter(listener);
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

    public boolean isSectionHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemViewType(int position) {
//        return position == 0 ? TYPE_SECTION_HEADER : TYPE_LIST_ITEM;
        return TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        return _channels.size();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if (isSectionHeader(viewType)) {
//            View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.adapter_edit_group_list_section, parent, false);
//            return SectionHeaderViewHolder.newInstance(view, _clickListener);
//        } else {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_edit_group_item, parent, false);
        return ItemViewHolder.newInstance(view, _clickListener);
//        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
//        if (holder instanceof SectionHeaderViewHolder) {
//            bindSectionViewData(
//                (SectionHeaderViewHolder) holder, getItemData(position));
//        } else if (holder instanceof ItemViewHolder) {
        bindItemViewData(
            (ItemViewHolder) holder, getItemData(position));
//        } else {
//            Timber.e("Invalid ViewHolder");
//        }
    }

    public GroupEditChannel getItemData(int position) {
        return _channels.get(position);
    }

    private void bindItemViewData(ItemViewHolder holder, GroupEditChannel editChannel) {
        Context context = holder._view.getContext();
        ChannelType channelType = editChannel.getChannel().channelType();
        if (channelType.equals(Custom)) {
            holder.itemImage.setImageDrawable(getAndroidIconDrawable(
                context, getActivityIcon(context, editChannel.getChannel().packageName())));
        } else {
            holder.itemImage.setImageDrawable(
                getSVGIconDrawable(context, editChannel.getChannel().channelType().getResId(),
                    getChannelBackgroundColor(context, channelType)));
        }
        holder.itemLabel.setText(editChannel.getChannel().label().toLowerCase());
        holder.itemSwitch.setChecked(editChannel.inGroup());
    }

//    private void bindSectionViewData(
//        SectionHeaderViewHolder holder,
//        GroupEditChannel sectionData) {
//        String lbl = sectionData.getChannel().channelSection().getLabel();
//        holder.sectionLabel.setText(lbl);
//    }


//    public static final class SectionHeaderViewHolder extends BaseViewHolder {
//        @InjectView(R.id.adapter_edit_group_list_section_label)
//        protected TextView sectionLabel;
//
//        private SectionHeaderViewHolder(View view, ItemClickListener itemClickListener) {
//            super(view, itemClickListener);
//        }
//
//        public static SectionHeaderViewHolder newInstance(
//            View view, ItemClickListener itemClickListener) {
//            return new SectionHeaderViewHolder(view, itemClickListener);
//        }
//    }


    public static final class ItemViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_edit_group_list_item_switch)
        public Switch itemSwitch;
        @InjectView(R.id.adapter_edit_group_list_item_image)
        protected ImageView itemImage;
        @InjectView(R.id.adapter_edit_group_list_item_label)
        protected TextView itemLabel;

        private ItemViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static ItemViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new ItemViewHolder(view, itemClickListener);
        }

    }
}
