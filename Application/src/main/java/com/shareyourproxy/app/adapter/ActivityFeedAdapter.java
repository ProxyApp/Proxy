package com.shareyourproxy.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.ActivityFeedItem;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;

import static com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter.getChannelBackgroundColor;
import static com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter.getChannelIconDrawable;

/**
 * Created by Evan on 10/13/15.
 */
public class ActivityFeedAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    public static final int VIEWTYPE_HEADER = 0;
    public static final int VIEWTYPE_CONTENT = 1;
    private final User _contact;
    private SortedList<ActivityFeedItem> _feedItems;
    private SortedList.Callback<ActivityFeedItem> _sortedListCallback;
    private boolean _needsRefresh = true;
    private ItemClickListener _listener;
    private Date _currentDate = new Date();

    /**
     * Constructor for {@link ActivityFeedAdapter}.
     *
     * @param feedItems a list of {@link ActivityFeedItem}s
     */
    public ActivityFeedAdapter(
        User contact, List<ActivityFeedItem> feedItems, ItemClickListener listener) {
        if (feedItems != null) {
            _feedItems = new SortedList<>(ActivityFeedItem.class, getSortedCallback(), feedItems
                .size());
        } else {
            _feedItems = new SortedList<>(ActivityFeedItem.class, getSortedCallback(), 0);
        }
        refreshFeedData(feedItems);
        _listener = listener;
        _contact = contact;
    }

    /**
     * Create a newInstance of a {@link ActivityFeedAdapter} with data.
     *
     * @param feedItems initialize data
     * @return an {@link ActivityFeedAdapter} with no data
     */
    public static ActivityFeedAdapter newInstance(
        User contact,
        List<ActivityFeedItem> feedItems, ItemClickListener listener) {
        return new ActivityFeedAdapter(contact, feedItems, listener);
    }

    public void refreshFeedData(@NonNull List<ActivityFeedItem> feedItems) {
        //get an approximation to "now"
        _currentDate = new Date();
        _feedItems.beginBatchedUpdates();
        if (feedItems != null) {
            _feedItems.addAll(feedItems);
        }
        _feedItems.endBatchedUpdates();
    }

    public SortedList.Callback<ActivityFeedItem> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new SortedList.Callback<ActivityFeedItem>() {

                @Override
                public int compare(ActivityFeedItem item1, ActivityFeedItem item2) {
                    Date date1 = item1.timestamp();
                    Date date2 = item2.timestamp();
                    if (date1 == null) {
                        return -1;
                    } else if (date2 == null) {
                        return 1;
                    } else {
                        //reverse chronological
                        return -date1.compareTo(date2);
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
                public boolean areContentsTheSame(
                    ActivityFeedItem oldItem, ActivityFeedItem newItem) {
                    return oldItem.actionAddress().equalsIgnoreCase(newItem.actionAddress()) &&
                        oldItem.channelType().equals(newItem.channelType());
                }

                @Override
                public boolean areItemsTheSame(ActivityFeedItem oldItem, ActivityFeedItem newItem) {
                    return oldItem.actionAddress().equalsIgnoreCase(newItem.actionAddress()) &&
                        oldItem.channelType().equals(newItem.channelType());
                }
            };
        }
        return _sortedListCallback;
    }

    @Override
    public int getItemViewType(int position) {
        return getItemData(position).isError() ? VIEWTYPE_HEADER : VIEWTYPE_CONTENT;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_activity_feed_item, parent, false);
        return FeedViewHolder.newInstance(view, _listener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEWTYPE_HEADER:
                bindHeaderViewData((FeedViewHolder) holder, getItemData(position));
                break;
            case VIEWTYPE_CONTENT:
                bindFeedViewData((FeedViewHolder) holder, getItemData(position));
                break;
        }
    }

    private void bindHeaderViewData(FeedViewHolder holder, ActivityFeedItem feedItem) {
        Context context = holder.view.getContext();
        ChannelType channelType = feedItem.channelType();

        holder.channelImage.setImageDrawable(
            getChannelIconDrawable(context, channelType,
                getChannelBackgroundColor(context, channelType)));

        holder.displayText.setText(context.getString(
            R.string.activity_feed_auth_message, channelType, _contact.first()));
        holder.timestampText.setVisibility(View.GONE);
    }

    /**
     * Get the desired {@link Channel} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link User}
     */
    public ActivityFeedItem getItemData(int position) {
        return _feedItems.get(position);
    }

    public void removeItemData(int position){
        _feedItems.removeItemAt(position);
    }
    /**
     * Set the Channel Intent link content.
     *
     * @param holder {@link Channel} {@link BaseViewHolder}
     */
    @SuppressLint("NewApi")
    private void bindFeedViewData(FeedViewHolder holder, ActivityFeedItem feedItem) {
        Context context = holder.view.getContext();
        ChannelType channelType = feedItem.channelType();

        holder.channelImage.setImageDrawable(
            getChannelIconDrawable(context, channelType,
                getChannelBackgroundColor(context, channelType)));

        holder.displayText.setText(getSpannableString(context, feedItem));
        holder.timestampText.setText(getTimePassedString(context, feedItem));
    }

    private SpannableStringBuilder getSpannableString(Context context, ActivityFeedItem item) {
        int start = item.handle().length();
        SpannableStringBuilder sb = new SpannableStringBuilder(
            context.getString(R.string.item1_return_item2, item.handle(), item.subtext()));

        TextAppearanceSpan span =
            new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body);
        sb.setSpan(span, start, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

    private String getTimePassedString(Context context, ActivityFeedItem feedItem) {
        Resources res = context.getResources();
        long diff = _currentDate.getTime() - feedItem.timestamp().getTime();
        int diffMinutes = new BigDecimal(TimeUnit.MILLISECONDS.toMinutes(diff)).intValueExact();
        int diffHours = new BigDecimal(TimeUnit.MILLISECONDS.toHours(diff)).intValueExact();
        int diffDays = new BigDecimal(TimeUnit.MILLISECONDS.toDays(diff)).intValueExact();
        int diffYears = diffDays / 365;

        if (diffYears > 0) {
            return res.getQuantityString(R.plurals.years_ago, diffYears, diffYears);
        } else if (diffDays > 0) {
            return res.getQuantityString(R.plurals.days_ago, diffDays, diffDays);
        } else if (diffHours > 0) {
            return res.getQuantityString(R.plurals.hours_ago, diffHours, diffHours);
        } else if (diffMinutes > 0) {
            return res.getQuantityString(R.plurals.minutes_ago, diffMinutes, diffMinutes);
        } else {
            return context.getString(R.string.moments_ago);
        }
    }

    @Override
    public int getItemCount() {
        return _feedItems.size();
    }

    /**
     * ViewHolder for the entered {@link Group} data.
     */
    public static class FeedViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_activity_feed_item_image)
        ImageView channelImage;
        @Bind(R.id.adapter_activity_feed_item_label)
        TextView displayText;
        @Bind(R.id.adapter_activity_feed_timestamp)
        TextView timestampText;


        /**
         * Constructor for the holder.
         *
         * @param view              the inflated view
         * @param itemClickListener click listener for each viewholder item
         */
        private FeedViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for each viewholder item
         * @return a ViewHolder instance
         */
        public static FeedViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new FeedViewHolder(view, itemClickListener);
        }
    }

}
