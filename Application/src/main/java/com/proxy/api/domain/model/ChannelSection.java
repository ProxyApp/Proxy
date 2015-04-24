package com.proxy.api.domain.model;

import com.proxy.R;
import com.proxy.api.domain.realm.RealmChannel;
import static com.proxy.app.adapter.ChannelListRecyclerAdapter.SectionHeaderViewHolder;
import static com.proxy.app.adapter.ChannelGridRecyclerAdapter.SectionViewHolder;

/**
 * {@link ChannelSection}'s are used to bind data for {@link SectionViewHolder} and
 * {@link SectionHeaderViewHolder}. They are also used to sort {@link RealmChannel}s with
 * the {@link ChannelSection#weight} value.
 */
public enum ChannelSection {
    General(0, "General", R.raw.apps), Chat(1, "Chat", R.raw.sms),
    Follow(2, "Follow", R.raw.directions), Locate(3, "Locate", R.raw.location),
    Meet(4, "Meet", R.raw.event), Transact(5, "Transact", R.raw.money),
    Play(6, "Play", R.raw.emoticon);

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
