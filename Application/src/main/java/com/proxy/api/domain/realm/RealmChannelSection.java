package com.proxy.api.domain.realm;

import android.support.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Sections that divide Channels. Contains information about Channels Group.
 */
public class RealmChannelSection extends RealmObject {

    @PrimaryKey
    private int weight;
    private String label;
    @Nullable
    private int resId;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

}
