package com.proxy.api.domain.realm;

import io.realm.RealmObject;

/**
 * Created by ccoffey on 5/11/15.
 */
public class RealmGroupEditChannel extends RealmObject{

    private RealmChannel channel;
    private boolean inGroup;

    public static RealmGroupEditChannel newInstance(RealmChannel chan, Boolean inGrp) {
        RealmGroupEditChannel rgec = new RealmGroupEditChannel();
        rgec.setChannel(chan);
        rgec.setInGroup(inGrp);
        return rgec;
    }

    public RealmChannel getChannel() {
        return channel;
    }

    public void setChannel(RealmChannel channel) {
        this.channel = channel;
    }


    public boolean getInGroup() {
        return inGroup;
    }

    public void setInGroup(boolean inGroup) {
        this.inGroup = inGroup;
    }



}
