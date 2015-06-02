package com.shareyourproxy.api.domain.model;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;

/**
 * Used to sort channels for their eventually called ACTION_INTENT or VIEW_INTENT in
 * {@link IntentLauncher}.
 */
public enum ChannelType {

    Phone("Phone", R.raw.ic_call), SMS("SMS", R.raw.ic_sms),
    Email("Email", R.raw.ic_email), Web("Web", R.raw.ic_link),
    Custom("Custom", R.raw.ic_star);

    private final String label;
    private final int resId;

    /**
     * Constructor.
     *
     * @param label name of channel
     */
    ChannelType(String label, int resId) {
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