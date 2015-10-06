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
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.util.ObjectUtils;

import butterknife.Bind;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.api.domain.model.ChannelType.Address;
import static com.shareyourproxy.api.domain.model.ChannelType.Ello;
import static com.shareyourproxy.api.domain.model.ChannelType.Email;
import static com.shareyourproxy.api.domain.model.ChannelType.Facebook;
import static com.shareyourproxy.api.domain.model.ChannelType.Github;
import static com.shareyourproxy.api.domain.model.ChannelType.Googleplus;
import static com.shareyourproxy.api.domain.model.ChannelType.Instagram;
import static com.shareyourproxy.api.domain.model.ChannelType.LeagueOfLegends;
import static com.shareyourproxy.api.domain.model.ChannelType.Linkedin;
import static com.shareyourproxy.api.domain.model.ChannelType.Medium;
import static com.shareyourproxy.api.domain.model.ChannelType.Meerkat;
import static com.shareyourproxy.api.domain.model.ChannelType.NintendoNetwork;
import static com.shareyourproxy.api.domain.model.ChannelType.Phone;
import static com.shareyourproxy.api.domain.model.ChannelType.PlaystationNetwork;
import static com.shareyourproxy.api.domain.model.ChannelType.Reddit;
import static com.shareyourproxy.api.domain.model.ChannelType.Skype;
import static com.shareyourproxy.api.domain.model.ChannelType.Snapchat;
import static com.shareyourproxy.api.domain.model.ChannelType.Soundcloud;
import static com.shareyourproxy.api.domain.model.ChannelType.Steam;
import static com.shareyourproxy.api.domain.model.ChannelType.Tumblr;
import static com.shareyourproxy.api.domain.model.ChannelType.Twitch;
import static com.shareyourproxy.api.domain.model.ChannelType.Twitter;
import static com.shareyourproxy.api.domain.model.ChannelType.Venmo;
import static com.shareyourproxy.api.domain.model.ChannelType.Web;
import static com.shareyourproxy.api.domain.model.ChannelType.Whatsapp;
import static com.shareyourproxy.api.domain.model.ChannelType.XboxLive;
import static com.shareyourproxy.api.domain.model.ChannelType.Youtube;

/**
 * Adapter that handles displaying channels.
 */
public class AddChannelAdapter extends BaseRecyclerViewAdapter {
    private static final Channel PHONE = createModelInstance(Phone);
    private static final Channel SMS = createModelInstance(ChannelType.SMS);
    private static final Channel EMAIL = createModelInstance(Email);
    private static final Channel WEB = createModelInstance(Web);
    private static final Channel FACEBOOK = createModelInstance(Facebook);
    private static final Channel TWITTER = createModelInstance(Twitter);
    private static final Channel MEERKAT = createModelInstance(Meerkat);
    private static final Channel REDDIT = createModelInstance(Reddit);
    private static final Channel LINKEDIN = createModelInstance(Linkedin);
    private static final Channel GOOGLEPLUS = createModelInstance(Googleplus);
    private static final Channel GITHUB = createModelInstance(Github);
    private static final Channel ADDRESS = createModelInstance(Address);
    private static final Channel YOUTUBE = createModelInstance(Youtube);
    private static final Channel INSTAGRAM = createModelInstance(Instagram);
    private static final Channel TUMBLR = createModelInstance(Tumblr);
    private static final Channel ELLO = createModelInstance(Ello);
    private static final Channel VENMO = createModelInstance(Venmo);
    private static final Channel MEDIUM = createModelInstance(Medium);
    private static final Channel SOUNDCLOUD = createModelInstance(Soundcloud);
    private static final Channel SKYPE = createModelInstance(Skype);
    private static final Channel SNAPCHAT =createModelInstance(Snapchat);
    private static final Channel WHATSAPP = createModelInstance(Whatsapp);
    private static final Channel LEAGUEOFLEGENDS = createModelInstance(LeagueOfLegends);
    private static final Channel PLAYSTATIONNETWORK = createModelInstance(PlaystationNetwork);
    private static final Channel NINTENDONETWORK = createModelInstance(NintendoNetwork);
    private static final Channel STEAM = createModelInstance(Steam);
    private static final Channel TWITCH = createModelInstance(Twitch);
    private static final Channel XBOXLIVE = createModelInstance(XboxLive);

    private Callback<Channel> _sortedListCallback;
    private SortedList<Channel> _channels = new SortedList<>(Channel.class, getSortedCallback());
    private ItemClickListener _clickListener;

    public AddChannelAdapter(ItemClickListener listener) {
        _clickListener = listener;
        Channel[] channels = new Channel[]{
            PHONE, SMS, EMAIL, WEB, FACEBOOK, TWITTER, MEERKAT, REDDIT, LINKEDIN,
            GOOGLEPLUS, GITHUB, ADDRESS, YOUTUBE, INSTAGRAM, TUMBLR, ELLO,
            VENMO, MEDIUM, SOUNDCLOUD, SKYPE, SNAPCHAT, WHATSAPP, LEAGUEOFLEGENDS,
            PLAYSTATIONNETWORK, NINTENDONETWORK, STEAM, TWITCH,XBOXLIVE
        };
        _channels.addAll(channels);
    }

    /**
     * Create a newInstance of a {@link AddChannelAdapter} with blank data.
     *
     * @return an {@link AddChannelAdapter} with no data
     */

    public static AddChannelAdapter newInstance(
        ItemClickListener
            listener) {
        return new AddChannelAdapter(listener);
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
                        return item1.label().compareToIgnoreCase(item2.label());
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
                public boolean areContentsTheSame(Channel item1, Channel item2) {
                    return (item1.id().equals(item2.id())
                        && item1.label().equals(item2.label())
                        && item1.channelType().equals(item2.channelType()));
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
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_add_channel_list_item, parent, false);
        return ItemViewHolder.newInstance(view, _clickListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        bindItemViewData((ItemViewHolder) holder, getItemData(position));
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

    /**
     * Set the Channel Intent link content.
     *
     * @param holder  {@link Channel} {@link BaseViewHolder}
     * @param channel {@link Channel} data
     */
    @SuppressLint("NewApi")
    private void bindItemViewData(ItemViewHolder holder, Channel channel) {
        Context context = holder.view.getContext();
        ChannelType channelType = channel.channelType();
        holder.itemImage.setImageDrawable(
            getChannelIconDrawable(context, channel,
                getChannelBackgroundColor(context, channelType)));
        holder.itemLabel.setText(ObjectUtils.capitalize(channel.label()));
    }

    @Override
    public int getItemCount() {
        return _channels.size();
    }

    /**
     * ViewHolder for the settings header.
     */
    public static final class SectionHeaderViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_add_channel_list_section_image)
        ImageView sectionImage;
        @Bind(R.id.adapter_add_channel_list_section_label)
        TextView sectionLabel;

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
            View view, ItemClickListener itemClickListener) {
            return new SectionHeaderViewHolder(view, itemClickListener);
        }
    }

    /**
     * ViewHolder for the entered settings data.
     */
    public static final class ItemViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_add_channel_list_item_image)
        ImageView itemImage;
        @Bind(R.id.adapter_add_channel_list_item_label)
        TextView itemLabel;

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

