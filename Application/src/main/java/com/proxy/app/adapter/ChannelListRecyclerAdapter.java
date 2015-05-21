package com.proxy.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.util.SortedList.Callback;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.domain.factory.ChannelFactory;
import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.ChannelType;
import com.proxy.api.domain.model.User;
import com.proxy.app.adapter.BaseViewHolder.ItemClickListener;

import butterknife.InjectView;

import static com.proxy.api.domain.model.ChannelSection.General;
import static com.proxy.api.domain.model.ChannelType.Custom;
import static com.proxy.util.ViewUtils.getActivityIcon;

/**
 * Created by Evan on 5/5/15.
 */
public class ChannelListRecyclerAdapter extends BaseRecyclerViewAdapter {
    public static final Channel DIALER = ChannelFactory.getPhoneChannel();
    public static final Channel HANGOUTS = ChannelFactory.getSMSChannel();
    public static final Channel EMAIL = ChannelFactory.getEmailChannel();
    public static final Channel WEB = ChannelFactory.getWebChannel();
    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_LIST_ITEM = 1;
    Callback<Channel> _sortedListCallback;
    SortedList<Channel> _channels = new SortedList<>(Channel.class, getSortedCallback());
    private ItemClickListener _clickListener;

    public ChannelListRecyclerAdapter(ItemClickListener listener) {
        _clickListener = listener;
        _channels.add(DIALER);
        _channels.add(HANGOUTS);
        _channels.add(EMAIL);
        _channels.add(WEB);
    }

    /**
     * Create a newInstance of a {@link ChannelListRecyclerAdapter} with blank data.
     *
     * @return an {@link ChannelListRecyclerAdapter} with no data
     */
    public static ChannelListRecyclerAdapter newInstance(
        ItemClickListener
            listener) {
        return new ChannelListRecyclerAdapter(listener);
    }

    /**
     * Is the item at the specified position a header?
     *
     * @param position of item
     * @return is the item a header
     */
    public static boolean isSectionHeader(int position) {
        return position == 0;
    }

    public Callback<Channel> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new Callback<Channel>() {


                @Override
                public int compare(Channel o1, Channel o2) {
                    return o1.id().value().compareTo(o2.id().value());
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
                public boolean areContentsTheSame(Channel oldItem, Channel newItem) {
                    // we dont compare resId because its probably going to be removed
                    return (oldItem.id().equals(newItem.id())
                        && oldItem.label().equals(newItem.label())
                        && oldItem.packageName().equals(newItem.packageName())
                        && oldItem.channelSection() == newItem.channelSection()
                        && oldItem.channelType().equals(newItem.channelType()));
                }

                @Override
                public boolean areItemsTheSame(Channel item1, Channel item2) {
                    //Sections will have the same ID but different categories
                    return (item1.id().equals(item2.id())
                        && item1.channelSection() == item2.channelSection());
                }
            };
        }
        return _sortedListCallback;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_add_channel_list_section, parent, false);
            return SectionHeaderViewHolder.newInstance(view, _clickListener);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_add_channel_list_item, parent, false);
            return ItemViewHolder.newInstance(view, _clickListener);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof SectionHeaderViewHolder) {
            bindSectionContent((SectionHeaderViewHolder) holder, getItemData(position));
        } else if (holder instanceof ItemViewHolder) {
            //section offset
            position = position - 1;
            bindItemViewData((ItemViewHolder) holder, getItemData(position));
        }
    }

    /**
     * Get the desired {@link Channel} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link User}
     */
    public Channel getItemData(int position) {
        return _channels.get(position);
    }

    private void bindSectionContent(
        SectionHeaderViewHolder holder, Channel channelData) {
        Context context = holder._view.getContext();
        int resourceId = General.getResId();

        holder.sectionLabel.setText(General.toString());
        holder.sectionImage.setImageDrawable(getSectionResourceDrawable(context, resourceId));
    }

    /**
     * Set the Channel Intent link content.
     *
     * @param holder  {@link Channel} {@link BaseViewHolder}
     * @param channel {@link Channel} data
     */
    @SuppressLint("NewApi")
    private void bindItemViewData(ItemViewHolder holder, Channel channel) {
        Context context = holder._view.getContext();
        ChannelType channelType = channel.channelType();
        if (channelType.equals(Custom)) {
            holder.itemImage.setImageDrawable(getAndroidIconDrawable(
                context, getActivityIcon(context, channel.packageName())));
        } else {
            holder.itemImage.setImageDrawable(
                getSVGIconDrawable(context, channel.channelType().getResId()));
        }
        holder.itemLabel.setText(channel.label().toLowerCase());
    }

    @Override
    public int getItemViewType(int position) {
        return position == TYPE_SECTION_HEADER ? TYPE_SECTION_HEADER : TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        return _channels.size() + 1;
    }

    /**
     * ViewHolder for the settings header.
     */
    public static final class SectionHeaderViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_add_channel_list_section_image)
        protected ImageView sectionImage;
        @InjectView(R.id.adapter_add_channel_list_section_label)
        protected TextView sectionLabel;

        /**
         * Constructor for the HeaderViewHolder.
         *
         * @param view              the inflated view
         * @param itemClickListener click listener for this view
         */
        private SectionHeaderViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for this view
         * @return a ViewHolder instance
         */
        public static SectionHeaderViewHolder newInstance(
            View view, ItemClickListener
            itemClickListener) {
            return new SectionHeaderViewHolder(view, itemClickListener);
        }
    }

    /**
     * ViewHolder for the entered settings data.
     */
    public static final class ItemViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_add_channel_list_item_image)
        protected ImageView itemImage;
        @InjectView(R.id.adapter_add_channel_list_item_label)
        protected TextView itemLabel;

        /**
         * Constructor for the ItemViewHolder.
         *
         * @param view              the inflated view
         * @param itemClickListener click listener for this view
         */
        private ItemViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for this view
         * @return a ViewHolder instance
         */
        public static ItemViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new ItemViewHolder(view, itemClickListener);
        }
    }
}

