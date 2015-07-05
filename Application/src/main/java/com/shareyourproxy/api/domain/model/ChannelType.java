package com.shareyourproxy.api.domain.model;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;

/**
 * Used to sort channels for their eventually called ACTION_INTENT or VIEW_INTENT in {@link
 * IntentLauncher}.
 */
public enum ChannelType {

    Custom(0, "Custom", R.raw.ic_star), Phone(1, "Phone", R.raw.ic_call),
    SMS(2, "SMS", R.raw.ic_sms), Email(3, "Email", R.raw.ic_email),
    Web(4, "Web", R.raw.ic_link), Facebook(5, "Facebook", R.raw.ic_facebook);

    private final int weight;
    private final String label;
    private final int resId;

    /**
     * Constructor.
     *
     * @param label name of newChannel
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