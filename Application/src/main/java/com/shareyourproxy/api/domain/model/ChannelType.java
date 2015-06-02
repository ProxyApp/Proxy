package com.shareyourproxy.api.domain.model;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;

/**
 * Used to sort channels for their eventually called ACTION_INTENT or VIEW_INTENT in {@link
 * IntentLauncher}.
 */
public enum ChannelType {

    Phone(0, "Phone", R.raw.ic_call), SMS(1, "SMS", R.raw.ic_sms),
    Email(2, "Email", R.raw.ic_email), Web(3, "Web", R.raw.ic_link),
    Custom(4, "Custom", R.raw.ic_star);

    private final int weight;
    private final String label;
    private final int resId;

    /**
     * Constructor.
     *
     * @param label name of channel
     */
    ChannelType(int weight, String label, int resId) {
        this.weight = weight;
        this.label = label;
        this.resId = resId;
    }

    /**
     * Getter.
     *
     * @return Channel label
     */
    public String getLabel() {
        return label;
    }

    public int getResId() {
        return resId;
    }

    public int getWeight() {
        return weight;
    }

    /**
     * String value.
     *
     * @return label String
     */
    @Override
    public String toString() {
        return label;
    }

}