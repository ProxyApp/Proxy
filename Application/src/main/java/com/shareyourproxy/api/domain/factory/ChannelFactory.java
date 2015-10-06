package com.shareyourproxy.api.domain.factory;


import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.realm.RealmChannel;
import com.shareyourproxy.api.domain.realm.RealmChannelType;
import com.shareyourproxy.api.domain.realm.RealmString;

import java.util.HashMap;
import java.util.HashSet;

import io.realm.RealmList;

import static com.shareyourproxy.api.domain.model.ChannelType.valueOfLabel;


/**
 * Factory to create Channels or RealmChannels.
 */
public class ChannelFactory {

    public static Channel createModelInstance(
        String id, String label, ChannelType channelType, String actionAddress) {
        return Channel.create(id, label, channelType, actionAddress, false);
    }

    public static Channel createModelInstance(ChannelType channelType) {
        String label = channelType.toString();
        return Channel.create(label, label, channelType, label, false);
    }

    public static Channel createModelInstance(Channel copyChannel, String actionAddress) {
        return Channel.create(
            copyChannel.id(), copyChannel.label(), copyChannel.channelType(), actionAddress, false);
    }

    public static Channel createPublicChannel(Channel copyChannel, Boolean isPublic) {
        return Channel.create(copyChannel.id(), copyChannel.label(), copyChannel.channelType(),
            copyChannel.actionAddress(), isPublic);
    }

    public static Channel createModelInstance(RealmChannel realmChannel) {
        return Channel.create(realmChannel.getId(), realmChannel.getLabel(),
            getModelChannelType(realmChannel.getChannelType()),
            realmChannel.getActionAddress(), realmChannel.getIsPublic());
    }

    public static ChannelType getModelChannelType(RealmChannelType channelType) {
        return valueOfLabel(channelType.getLabel());
    }

    /**
     * Return a RealmList of Contacts from a user
     *
     * @param realmChannels to get channels from
     * @return RealmList of Contacts
     */
    public static HashMap<String, Channel> getModelChannels(RealmList<RealmChannel> realmChannels) {
        if (realmChannels != null) {
            HashMap<String, Channel> channels = new HashMap<>(realmChannels.size());
            for (RealmChannel realmChannel : realmChannels) {
                channels.put(realmChannel.getId(), createModelInstance(realmChannel));
            }
            return channels;
        }
        return null;
    }

    /**
     * Return a RealmList of Contact ids from a user
     *
     * @param realmChannels to get channels from
     * @return RealmList of Contacts
     */
    public static HashSet<String> getModelChannelList(RealmList<RealmString> realmChannels) {
        if (realmChannels != null) {
            HashSet<String> channels = new HashSet<>(realmChannels.size());
            for (RealmString realmChannel : realmChannels) {
                channels.add(realmChannel.getValue());
            }
            return channels;
        }
        return null;
    }

}