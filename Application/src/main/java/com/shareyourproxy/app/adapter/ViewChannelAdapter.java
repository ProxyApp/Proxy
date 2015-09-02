package com.shareyourproxy.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v7.util.SortedList;
import android.support.v7.util.SortedList.Callback;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener;
import com.shareyourproxy.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindColor;

/**
 * Adapter for a users profile and their {@link Channel} package permissions.
 */
public class ViewChannelAdapter extends BaseRecyclerViewAdapter {
    public static final int VIEW_TYPE_CONTENT = 2;
    private final ItemLongClickListener _clickListener;
    @ColorInt
    @BindColor(R.color.common_gray)
    protected int _gray;
    private Callback<Channel> _sortedListCallback;
    private SortedList<Channel> _channels;
    private boolean _needsRefresh = true;

    /**
     * Constructor for {@link ViewChannelAdapter}.
     *
     * @param listener click listener
     */
    private ViewChannelAdapter(HashMap<String, Channel> channels, ItemLongClickListener listener) {
        _clickListener = listener;
        if (channels != null) {
            _channels = new SortedList<>(
                Channel.class, getSortedCallback(), channels.size());
        } else {
            _channels = new SortedList<>(
                Channel.class, getSortedCallback());
        }
        updateChannels(channels);
    }

    /**
     * Create a newInstance of a {@link ViewChannelAdapter} with blank data.
     *
     * @return an {@link ViewChannelAdapter} with no data
     */
    public static ViewChannelAdapter newInstance(
        HashMap<String, Channel> channels, ItemLongClickListener listener) {
        return new ViewChannelAdapter(channels, listener);
    }

    public void updateChannels(HashMap<String, Channel> channels) {
        if (channels != null) {
            _channels.beginBatchedUpdates();
            _channels.clear();
            for (Map.Entry<String, Channel> channel : channels.entrySet()) {
                _channels.add(channel.getValue());
            }
            _channels.endBatchedUpdates();
        }
    }

    public void addChannel(Channel channel) {
        _channels.add(channel);
    }

    public void updateChannel(Channel oldChannel, Channel newChannel) {
        _channels.updateItemAt(_channels.indexOf(oldChannel), newChannel);
    }

    public Callback<Channel> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new Callback<Channel>() {
                @Override
                public int compare(Channel item1, Channel item2) {
                    int weight1 = item1.channelType().getWeight();
                    int weight2 = item2.channelType().getWeight();
                    int compareFirst = ObjectUtils.compare(weight1, weight2);
                    if (compareFirst == 0 || (weight1 > 4 && weight2 > 4)) {
                        String label1 = item1.channelType().getLabel();
                        String label2 = item2.channelType().getLabel();
                        return label1.compareTo(label2);
                    } else {
                        return compareFirst;
                    }
                }

                @Override
                public void onInserted(int position, int count) {
                    if (_needsRefresh) {
                        notifyDataSetChanged();
                        _needsRefresh = false;
                    }
                    notifyItemRangeInserted(position, count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    if (getItemCount() == 0) {
                        notifyDataSetChanged();
                        _needsRefresh = true;
                    } else {
                        notifyItemRangeRemoved(position, count);
                    }
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
                public boolean areContentsTheSame(Channel item1, Channel item2) {
                    return (item1.id().value().equals(item2.id().value())
                        && item1.label().equals(item2.label())
                        && item1.channelType().equals(item2.channelType()))
                        && item1.actionAddress().equals(item2.actionAddress());
                }

                @Override
                public boolean areItemsTheSame(Channel item1, Channel item2) {
                    return item1.id().equals(item2.id());
                }
            };
        }
        return _sortedListCallback;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_CONTENT;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_channel_view_content, parent, false);
        return ContentViewHolder.newInstance(view, _clickListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        bindContentViewData((ContentViewHolder) holder, getItemData(position));
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
        String channelTypeString = channel.channelType().getLabel();
        String label = channel.label();
        String address = channel.actionAddress();

        SpannableStringBuilder sb = getSpannableStringBuilder(context, channelTypeString, label,
            address);

        holder.channelImage.setImageDrawable(
            getSVGIconDrawable(context, channel,
                getChannelBackgroundColor(context, channelType)));
        holder.channelContentText.setText(sb);
    }

    private SpannableStringBuilder getSpannableStringBuilder(Context context, String
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
    public static final class ContentViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_channel_view_content_image)
        protected ImageView channelImage;
        @Bind(R.id.adapter_channel_view_content)
        protected TextView channelContentText;

        /**
         * Constructor for the ItemViewHolder.
         *
         * @param itemClickListener click listener for this view
         * @param view              the inflated view
         */
        private ContentViewHolder(View view, ItemLongClickListener itemClickListener) {
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
            View view, ItemLongClickListener itemClickListener) {
            return new ContentViewHolder(view, itemClickListener);
        }
    }
}