package com.shareyourproxy.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener;
import com.shareyourproxy.util.ObjectUtils;

import java.util.HashMap;

import butterknife.Bind;

import static com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.SHARE_PROFILE;

/**
 * Adapter for a users profile and their {@link Channel} package permissions.
 */
public class ViewChannelAdapter extends NotificationRecyclerAdapter<Channel> {
    private final ItemLongClickListener _clickListener;

    /**
     * Constructor for {@link ViewChannelAdapter}.
     *
     * @param listener click listener
     */
    private ViewChannelAdapter(
        BaseRecyclerView recyclerView, SharedPreferences sharedPreferences, boolean showHeader,
        ItemLongClickListener listener) {
        super(Channel.class, recyclerView, showHeader, false, sharedPreferences);
        _clickListener = listener;
    }

    /**
     * Create a newInstance of a {@link ViewChannelAdapter} with blank data.
     *
     * @return an {@link ViewChannelAdapter} with no data
     */
    public static ViewChannelAdapter newInstance(
        BaseRecyclerView recyclerView, SharedPreferences sharedPreferences, boolean showHeader,
        ItemLongClickListener listener) {
        return new ViewChannelAdapter(recyclerView, sharedPreferences, showHeader, listener);
    }

    public void updateChannels(HashMap<String, Channel> channels) {
        refreshData(channels.values());
    }

    @Override
    protected BaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_channel_view_content, parent, false);
        return ContentViewHolder.newInstance(view, _clickListener);
    }

    @Override
    protected int compare(Channel item1, Channel item2) {
        int weight1 = item1.channelType().getWeight();
        int weight2 = item2.channelType().getWeight();
        int compareFirst = ObjectUtils.Companion.compare(weight1, weight2);
        if (compareFirst == 0 || (weight1 > 4 && weight2 > 4)) {
            String label1 = item1.label();
            String label2 = item2.label();
            int compareSecond = label1.compareTo(label2);
            if (compareSecond == 0) {
                String action1 = item1.actionAddress();
                String action2 = item2.actionAddress();
                return action1.compareTo(action2);
            } else {
                return compareSecond;
            }
        } else {
            return compareFirst;
        }
    }

    @Override
    protected boolean areContentsTheSame(Channel item1, Channel item2) {
        return (item1.id().equals(item2.id())
            && item1.label().equals(item2.label())
            && item1.channelType().equals(item2.channelType()))
            && item1.actionAddress().equals(item2.actionAddress());
    }

    @Override
    protected boolean areItemsTheSame(Channel item1, Channel item2) {
        return item1.id().equals(item2.id());
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            bindHeaderViewData((HeaderViewHolder) holder, SHARE_PROFILE, true, false);
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
        Context context = holder.view.getContext();
        ChannelType channelType = channel.channelType();
        String channelTypeString = channel.channelType().getLabel();
        String label = channel.label();
        String address = channel.actionAddress();
        SpannableStringBuilder sb = getChannelSpannableStringBuilder(
            context, channelTypeString, label, address);

        holder.channelImage.setImageDrawable(
            getChannelIconDrawable(context, channel,
                getChannelBackgroundColor(context, channelType)));
        holder.channelContentText.setText(sb);
    }

    /**
     * ViewHolder for the entered settings data.
     */
    public static final class ContentViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_channel_view_content_image)
        ImageView channelImage;
        @Bind(R.id.adapter_channel_view_content)
        TextView channelContentText;

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