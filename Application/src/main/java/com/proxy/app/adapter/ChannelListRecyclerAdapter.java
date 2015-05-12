package com.proxy.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.domain.factory.ChannelFactory;
import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmChannel;
import com.proxy.api.domain.realm.RealmChannelType;

import butterknife.InjectView;
import io.realm.RealmObject;

import static com.proxy.api.domain.factory.ChannelFactory.getRealmChannelType;
import static com.proxy.api.domain.model.ChannelSection.General;
import static com.proxy.api.domain.model.ChannelType.Custom;
import static com.proxy.util.ViewUtils.getActivityIcon;

/**
 * Created by Evan on 5/5/15.
 */
public class ChannelListRecyclerAdapter extends BaseRecyclerViewAdapter {
    public static final RealmChannel DIALER = ChannelFactory.getPhoneChannel();
    public static final RealmChannel HANGOUTS = ChannelFactory.getSMSChannel();
    public static final RealmChannel GMAIL = ChannelFactory.getEmailChannel();
    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_LIST_ITEM = 1;
    SortedList.Callback<RealmChannel> mSortedListCallback;
    SortedList<RealmChannel> mChannels = new SortedList<>(RealmChannel.class,
        getSortedCallback());
    private BaseViewHolder.ItemClickListener mClickListener;

    public ChannelListRecyclerAdapter(BaseViewHolder.ItemClickListener listener) {
        mClickListener = listener;
        mChannels.add(DIALER);
        mChannels.add(HANGOUTS);
        mChannels.add(GMAIL);
    }

    /**
     * Create a newInstance of a {@link ChannelListRecyclerAdapter} with blank data.
     *
     * @return an {@link ChannelListRecyclerAdapter} with no data
     */
    public static ChannelListRecyclerAdapter newInstance(
        BaseViewHolder.ItemClickListener
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

    public SortedList.Callback<RealmChannel> getSortedCallback() {
        if (mSortedListCallback == null) {
            mSortedListCallback = new SortedList.Callback<RealmChannel>() {


                @Override
                public int compare(RealmChannel o1, RealmChannel o2) {
                    return o1.getChannelId().compareTo(o2.getChannelId());
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
                public boolean areContentsTheSame(RealmChannel oldItem, RealmChannel newItem) {
                    // we dont compare resId because its probably going to be removed
                    return (oldItem.getChannelId().equals(newItem.getChannelId())
                        && oldItem.getLabel().equals(newItem.getLabel())
                        && oldItem.getPackageName().equals(newItem.getPackageName())
                        && oldItem.getSection() == newItem.getSection()
                        && oldItem.getChannelType().equals(newItem.getChannelType()));
                }

                @Override
                public boolean areItemsTheSame(RealmChannel item1, RealmChannel item2) {
                    //Sections will have the same ID but different categories
                    return (item1.getChannelId().equals(item2.getChannelId())
                        && item1.getSection() == item2.getSection());
                }
            };
        }
        return mSortedListCallback;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_add_channel_list_section, parent, false);
            return SectionHeaderViewHolder.newInstance(view, mClickListener);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_add_channel_list_item, parent, false);
            return ItemViewHolder.newInstance(view, mClickListener);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof SectionHeaderViewHolder) {
            bindSectionContent((SectionHeaderViewHolder) holder, (RealmChannel)
                getItemData(position));
        } else if (holder instanceof ItemViewHolder) {
            //section offset
            position = position - 1;
            bindItemViewData((ItemViewHolder) holder, (RealmChannel) getItemData(position));
        }
    }

    /**
     * Get the desired {@link Channel} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link User}
     */
    public RealmObject getItemData(int position) {
        return mChannels.get(position);
    }

    private void bindSectionContent(
        SectionHeaderViewHolder holder, RealmChannel channelData) {
        Context context = holder.view.getContext();
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
    private void bindItemViewData(ItemViewHolder holder, RealmChannel channel) {
        Context context = holder.view.getContext();
        RealmChannelType realmChannelType = channel.getChannelType();
        if (realmChannelType.equals(getRealmChannelType(Custom))) {
            holder.itemImage.setImageDrawable(getAndroidIconDrawable(
                context, getActivityIcon(context, channel.getPackageName())));
        } else {
            holder.itemImage.setImageDrawable(
                getSVGIconDrawable(context, channel.getChannelType().getResId()));
        }
        holder.itemLabel.setText(channel.getLabel().toLowerCase());
    }

    @Override
    public int getItemViewType(int position) {
        return position == TYPE_SECTION_HEADER ? TYPE_SECTION_HEADER : TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        return mChannels.size() + 1;
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

