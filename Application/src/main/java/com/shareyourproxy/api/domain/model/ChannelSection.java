package com.shareyourproxy.api.domain.model;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.realm.RealmChannel;

import static com.shareyourproxy.app.adapter.ChannelGridRecyclerAdapter.SectionViewHolder;
import static com.shareyourproxy.app.adapter.ChannelListRecyclerAdapter.SectionHeaderViewHolder;

/**
 * {@link ChannelSection}'s are used to bind data for {@link SectionViewHolder} and
 * {@link SectionHeaderViewHolder}. They are also used to sort {@link RealmChannel}s with
 * the {@link ChannelSection#weight} value.
 */
public enum ChannelSection {
    General(0, "General", R.raw.ic_apps),
    Chat(1, "Chat", R.raw.ic_sms),
    Follow(2, "Follow", R.raw.ic_directions),
    Locate(3, "Locate", R.raw.ic_location),
    Meet(4, "Meet", R.raw.ic_event),
    Transact(5, "Transact", R.raw.ic_money),
    Play(6, "Play", R.raw.ic_emoticon);

    private final int weight;
    private final String label;
    private final int resId;

    /**
     * Constructor.
     *
     * @param weight channel weight
     * @param label   of channelSection
     */
    ChannelSection(int weight, String label, int resId) {
        this.weight = weight;
        this.label = label;
        this.resId = resId;
    }

    /**
     * Getter.
     *
     * @return weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Getter.
     *
     * @return resId
     */
    public int getResId() {
        return resId;
    }

    /**
     * Getter.
     *
     * @return Channel Section Name
     */
    public String getLabel() {
        return label;
    }

    /**
     * String value.
     *
     * @return enum label String
     */
    @Override
    public String toString() {
        return label;
    }

}
