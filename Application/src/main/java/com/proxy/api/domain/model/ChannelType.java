package com.proxy.api.domain.model;

import com.proxy.R;

/**
 * Created by Evan on 5/5/15.
 */
public enum ChannelType {

    Phone("Phone", R.raw.call), SMS("SMS", R.raw.sms),
    Email("Email", R.raw.email), Web("Web", R.raw.web),
    Custom("Custom", R.raw.star);

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