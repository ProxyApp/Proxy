package com.shareyourproxy.app.adapter;

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

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.User;

import java.util.ArrayList;

import butterknife.InjectView;
import timber.log.Timber;

import static com.shareyourproxy.api.domain.model.ChannelSection.General;
import static com.shareyourproxy.api.domain.model.ChannelType.Custom;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.ViewUtils.getActivityIcon;

/**
 * Adapter for a users profile and their {@link Channel} package permissions.
 */
public class ChannelGridRecyclerAdapter extends BaseRecyclerViewAdapter {

    public static final int VIEW_TYPE_SECTION = 1;
    public static final int VIEW_TYPE_CONTENT = 2;
    private final ItemClickListener _clickListener;
    private Callback<Channel> _sortedListCallback;
    private SortedList<Channel> _channels = new SortedList<>(Channel.class, getSortedCallback());

    /**
     * Constructor for {@link ChannelGridRecyclerAdapter}.
     *
     * @param currentUser currently logged in User
     * @param listener    click listener
     */
    private ChannelGridRecyclerAdapter(
        User currentUser, ItemClickListener listener) {
        _clickListener = listener;
        updateChannels(currentUser);
    }

    /**
     * Create a newInstance of a {@link ChannelGridRecyclerAdapter} with blank data.
     *
     * @param currentUser currently Logged in {@link User}
     * @return an {@link ChannelGridRecyclerAdapter} with no data
     */
    public static ChannelGridRecyclerAdapter newInstance(
        User currentUser, ItemClickListener listener) {
        return new ChannelGridRecyclerAdapter(currentUser, listener);
    }

    private void updateChannels(User user) {
        if (user != null && user.channels() != null) {
            _channels.beginBatchedUpdates();
            ArrayList<Channel> channels = user.channels();
            for (Channel channel : channels) {
                _channels.add(channel);
            }
            _channels.endBatchedUpdates();
        }
    }

    public void addChannel(Channel channel) {
        _channels.add(channel);
    }

    public Callback<Channel> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new Callback<Channel>() {
                @Override
                public int compare(Channel channel1, Channel channel2) {
                    String channel1Id = channel1.id().value();
                    String channel2Id = channel2.id().value();
                    //TODO: BETTER COMPARISON
//                    int channel1Weight = channel1.channelSection().getWeight();
//                    int channel2Weight = channel2.channelSection().getWeight();
//                    ObjectUtils.compare(channel1Weight, channel2Weight);
                    return channel1Id.compareTo(channel2Id);
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
                    return (oldItem.id().value().equals(newItem.id().value())
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
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_SECTION;
        } else {
            return VIEW_TYPE_CONTENT;
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SECTION) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_channel_grid_section, parent, false);
            return SectionViewHolder.newInstance(view, _clickListener);
        } else if (viewType == VIEW_TYPE_CONTENT) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_channel_grid_content, parent, false);
            return ContentViewHolder.newInstance(view, _clickListener);
        } else {
            Timber.e("Error, Unknown ViewType");
            return null;
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof SectionViewHolder) {
            bindSectionViewData((SectionViewHolder) holder);
        } else if (holder instanceof ContentViewHolder) {
            bindContentViewData((ContentViewHolder) holder, getItemData(position));
        }
    }

    /**
     * Set the Channel Intent link content.
     *
     * @param holder  {@link Channel} {@link BaseViewHolder}
     * @param channel {@link Channel} data
     */
    @SuppressLint("NewApi")
    private void bindContentViewData(ContentViewHolder holder, Channel channel) {
        Context context = holder._view.getContext();
        ChannelType channelType = channel.channelType();
        if (channelType.equals(Custom)) {
            holder.channelImage.setImageDrawable(getAndroidIconDrawable(
                context, getActivityIcon(context, channel.packageName())));
        } else {
            holder.channelImage.setImageDrawable(
                getSVGIconDrawable(context, channel.channelType().getResId()));
        }
        holder.channelName.setText(channel.label().toLowerCase());
    }

    /**
     * Set this {@link BaseViewHolder} underlying section data.
     *
     * @param holder {@link Channel} {@link BaseViewHolder}
     */
    private void bindSectionViewData(SectionViewHolder holder) {
        Context context = holder._view.getContext();
        holder.sectionName.setText(General.toString());
        int resourceId = General.getResId();
        holder.sectionImage.setImageDrawable(getSectionResourceDrawable(context, resourceId));
    }

    @Override
    public int getItemCount() {
        return _channels.size();
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

    public void removeChannel(Channel channel) {
        _channels.remove(channel);
    }

    /**
     * ViewHolder for the entered settings data.
     */
    public static final class SectionViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_channel_grid_section_image)
        protected ImageView sectionImage;
        @InjectView(R.id.adapter_channel_grid_section_name)
        protected TextView sectionName;

        /**
         * Constructor for the ItemViewHolder.
         *
         * @param itemClickListener click listener for this view
         * @param view              the inflated view
         */
        private SectionViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for this view
         * @return a ViewHolder instance
         */
        public static SectionViewHolder newInstance(
            View view, ItemClickListener itemClickListener) {
            return new SectionViewHolder(view, itemClickListener);
        }
    }

    /**
     * ViewHolder for the entered settings data.
     */
    public static final class ContentViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_channel_grid_content_image)
        protected ImageView channelImage;
        @InjectView(R.id.adapter_channel_grid_content_name)
        protected TextView channelName;

        /**
         * Constructor for the ItemViewHolder.
         *
         * @param itemClickListener click listener for this view
         * @param view              the inflated view
         */
        private ContentViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param itemClickListener click listener for this view
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @return a ViewHolder instance
         */
        public static ContentViewHolder newInstance(
            View view, ItemClickListener itemClickListener) {
            return new ContentViewHolder(view, itemClickListener);
        }
    }
}