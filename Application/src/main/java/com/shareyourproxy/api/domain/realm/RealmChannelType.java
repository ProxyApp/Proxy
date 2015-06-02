package com.shareyourproxy.api.domain.realm;

import io.realm.RealmObject;

/**
 * Created by Evan on 5/4/15.
 */
public class RealmChannelType extends RealmObject {

    private int weight;
    private String label;
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

    /**
     * Getter.
     *
     * @return resource id
     */
    public int getResId() {
        return resId;
    }

    /**
     * Setter.
     *
     * @param resId resource id
     */
    public void setResId(int resId) {
        this.resId = resId;
    }

}
