package com.proxy.api.domain.realm;

import com.proxy.R;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Sections that divide Channels. Contains information about Channels Group.
 */
public class RealmChannelSection extends RealmObject {

    @PrimaryKey
    private int weight;
    private String name;
    private int resId;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

}
