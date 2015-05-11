package com.proxy.api.domain.factory;


import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.ChannelSection;
import com.proxy.api.domain.model.ChannelType;
import com.proxy.api.domain.realm.RealmChannel;
import com.proxy.api.domain.realm.RealmChannelSection;
import com.proxy.api.domain.realm.RealmChannelType;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

import static com.proxy.api.domain.model.ChannelSection.General;
import static com.proxy.api.domain.model.ChannelType.Email;
import static com.proxy.api.domain.model.ChannelType.Phone;
import static com.proxy.api.domain.model.ChannelType.SMS;
import static com.proxy.api.domain.model.ChannelType.valueOf;


/**
 * Factory to create realm channels.
 */
public class ChannelFactory {

    public static RealmChannel createRealmInstance(
        ChannelType channelType, ChannelSection channelSection, String actionAddress) {
        RealmChannel realmChannel = new RealmChannel();
        realmChannel.setChannelId(channelType.toString());
        realmChannel.setLabel(channelType.toString());
        realmChannel.setPackageName(channelType.toString());
        realmChannel.setActionAddress(actionAddress);
        realmChannel.setSection(getRealmSection(channelSection));
        realmChannel.setChannelType(getRealmChannelType(channelType));
        return realmChannel;
    }

    public static RealmChannel createRealmInstance(Channel channel) {
        return createRealmInstance(channel.channelType(), channel.channelSection(),
            channel.actionAddress());
    }

    public static Channel createModelInstance(RealmChannel realmChannel) {
        return Channel.create(realmChannel.getChannelId(), realmChannel.getLabel(),
            realmChannel.getPackageName(), getModelChannelSection(realmChannel.getSection()),
            getModelChannelType(realmChannel.getChannelType()), realmChannel.getActionAddress());
    }

    /**
     * Convert {@link ChannelSection) Enum into a {@link RealmChannelSection}.
     *
     * @param section
     * @return RealmChannelCategory
     */
    public static RealmChannelSection getRealmSection(ChannelSection section) {
        RealmChannelSection realmSection = new RealmChannelSection();
        realmSection.setWeight(section.getWeight());
        realmSection.setLabel(section.toString());
        realmSection.setResId(section.getResId());
        return realmSection;
    }

    public static RealmChannelType getRealmChannelType(ChannelType channelType) {

        RealmChannelType realmChannelType = new RealmChannelType();
        realmChannelType.setLabel(channelType.getLabel());
        realmChannelType.setResId(channelType.getResId());
        return realmChannelType;
    }

    /**
     * Convert {@link ChannelSection ) Enum into a {@link RealmChannelSection}.
     *
     * @param channelSection
     * @return RealmChannelCategory
     */
    public static ChannelSection getModelChannelSection(RealmChannelSection section) {
        return ChannelSection.valueOf(section.getLabel());
    }

    public static ChannelType getModelChannelType(RealmChannelType channelType) {
        return valueOf(channelType.getLabel());
    }

    /**
     * Get a gmail channel.
     *
     * @return gmail button
     */
    public static RealmChannel getEmailChannel() {
        RealmChannel realmChannel = new RealmChannel();
        realmChannel.setChannelId(Email.toString());
        realmChannel.setLabel(Email.toString());
        realmChannel.setPackageName(Email.toString());
        realmChannel.setSection(getRealmSection(General));
        realmChannel.setChannelType(getRealmChannelType(Email));
        return realmChannel;
    }

    /**
     * Get a hangouts channel.
     *
     * @return hangouts button
     */
    public static RealmChannel getSMSChannel() {
        RealmChannel realmChannel = new RealmChannel();
        realmChannel.setChannelId(SMS.toString());
        realmChannel.setLabel(SMS.toString());
        realmChannel.setPackageName(SMS.toString());
        realmChannel.setSection(getRealmSection(General));
        realmChannel.setChannelType(getRealmChannelType(SMS));
        return realmChannel;
    }

    /**
     * Get a dialer channel.
     *
     * @return dialer button
     */
    public static RealmChannel getPhoneChannel() {
        RealmChannel realmChannel = new RealmChannel();
        realmChannel.setChannelId(Phone.toString());
        realmChannel.setLabel(Phone.toString());
        realmChannel.setPackageName(Phone.toString());
        realmChannel.setSection(getRealmSection(General));
        realmChannel.setChannelType(getRealmChannelType(Phone));
        return realmChannel;
    }

    public static RealmList<RealmChannel> getRealmChannels(List<Channel> channels) {
        RealmList<RealmChannel> realmChannelArray = null;
        if (channels != null) {
            realmChannelArray = new RealmList<>();
            for (Channel channel : channels) {
                RealmChannel realmChannel = new RealmChannel();
                RealmChannelSection realmChannelSection = new RealmChannelSection();
                RealmChannelType realmChannelType = new RealmChannelType();
                //construct the channel section
                realmChannelSection.setWeight(channel.channelSection().getWeight());
                realmChannelSection.setLabel(channel.channelSection().name());
                realmChannelSection.setResId(channel.channelSection().getResId());

                //construct the channel type
                realmChannelType.setLabel(channel.channelType().getLabel());
                realmChannelType.setResId(channel.channelType().getResId());

                //construct the channel
                realmChannel.setChannelId(channel.channelId());
                realmChannel.setLabel(channel.label());
                realmChannel.setPackageName(channel.packageName());
                realmChannel.setSection(realmChannelSection);
                realmChannel.setChannelType(realmChannelType);
                realmChannel.setActionAddress(channel.actionAddress());

                //add to array
                realmChannelArray.add(realmChannel);
            }
        }
        return realmChannelArray;
    }

    /**
     * Return a RealmList of Contacts from a user
     *
     * @param realmChannels to get channels from
     * @return RealmList of Contacts
     */
    public static ArrayList<Channel> getModelChannels(
        RealmList<RealmChannel> realmChannels) {
        if (realmChannels != null) {
            ArrayList<Channel> channels = new ArrayList<>();
            for (RealmChannel realmChannel : realmChannels) {
                channels.add(Channel.create(realmChannel.getChannelId(),
                    realmChannel.getLabel(), realmChannel.getPackageName(),
                    getModelChannelSection(realmChannel.getSection()),
                    getModelChannelType(realmChannel.getChannelType()),
                    realmChannel.getActionAddress()));
            }
            return channels;
        }
        return null;
    }

}