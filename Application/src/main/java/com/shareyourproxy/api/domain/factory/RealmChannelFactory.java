package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.realm.RealmChannel;
import com.shareyourproxy.api.domain.realm.RealmChannelType;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;

/**
 * Factory for creating {@link RealmChannel}s.
 */
public class RealmChannelFactory {

    public static RealmList<RealmChannel> getRealmChannels(HashMap<String, Channel> channels) {
        if (channels == null) {
            return null;
        }
        RealmList<RealmChannel> realmChannelArray;
        realmChannelArray = new RealmList<>();
        for (Map.Entry<String, Channel> channelEntry : channels.entrySet()) {
            Channel channel = channelEntry.getValue();
            RealmChannel realmChannel = new RealmChannel();
            RealmChannelType realmChannelType = new RealmChannelType();
            //construct the newChannel section

            //construct the newChannel type
            realmChannelType.setWeight(channel.channelType().getWeight());
            realmChannelType.setLabel(channel.channelType().getLabel());
            realmChannelType.setResId(channel.channelType().getResId());

            //construct the newChannel
            realmChannel.setId(channel.id().value());
            realmChannel.setLabel(channel.label());
            realmChannel.setChannelType(realmChannelType);
            realmChannel.setActionAddress(channel.actionAddress());

            //add to array
            realmChannelArray.add(realmChannel);
        }
        return realmChannelArray;
    }

}
