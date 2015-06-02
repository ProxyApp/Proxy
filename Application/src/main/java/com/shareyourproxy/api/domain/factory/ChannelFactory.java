package com.shareyourproxy.api.domain.factory;


import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Channel.Builder;
import com.shareyourproxy.api.domain.model.ChannelSection;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.realm.RealmChannel;
import com.shareyourproxy.api.domain.realm.RealmChannelSection;
import com.shareyourproxy.api.domain.realm.RealmChannelType;

import java.util.ArrayList;

import io.realm.RealmList;

import static com.shareyourproxy.api.domain.model.Channel.builder;
import static com.shareyourproxy.api.domain.model.ChannelSection.General;
import static com.shareyourproxy.api.domain.model.ChannelType.Email;
import static com.shareyourproxy.api.domain.model.ChannelType.Phone;
import static com.shareyourproxy.api.domain.model.ChannelType.SMS;
import static com.shareyourproxy.api.domain.model.ChannelType.Web;
import static com.shareyourproxy.api.domain.model.ChannelType.valueOf;


/**
 * Factory to create realm channels.
 */
public class ChannelFactory {

    public static Channel createModelInstance(String id,String label,
        ChannelType channelType, ChannelSection channelSection, String actionAddress) {
        Channel.Builder channel = Channel.builder();
        channel.id(Id.builder().value(id).build());
        channel.label(label);
        channel.packageName(channelType.toString());
        channel.actionAddress(actionAddress);
        channel.channelSection(channelSection);
        channel.channelType(channelType);
        return channel.build();
    }

    public static Channel createModelInstance(RealmChannel realmChannel) {
        return Channel.create(Id.builder().value(realmChannel.getId()).build(), realmChannel
                .getLabel(),
            realmChannel.getPackageName(), getModelChannelSection(realmChannel.getChannelSection()),
            getModelChannelType(realmChannel.getChannelType()), realmChannel.getActionAddress());
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
     * Get a web channel.
     *
     * @return web button
     */
    public static Channel getWebChannel() {
        Builder channel = builder();
        channel.id(Id.builder().value(Web.toString()).build());
        channel.label(Web.toString());
        channel.packageName(Web.toString());
        channel.channelSection(General);
        channel.channelType(Web);
        channel.actionAddress(Web.toString());
        return channel.build();
    }

    /**
     * Get a gmail channel.
     *
     * @return gmail button
     */
    public static Channel getEmailChannel() {
        Builder channel = builder();
        channel.id(Id.builder().value(Email.toString()).build());
        channel.label(Email.toString());
        channel.packageName(Email.toString());
        channel.channelSection(General);
        channel.channelType(Email);
        channel.actionAddress(Email.toString());
        return channel.build();
    }

    /**
     * Get a hangouts channel.
     *
     * @return hangouts button
     */
    public static Channel getSMSChannel() {
        Builder channel = builder();
        channel.id(Id.builder().value(SMS.toString()).build());
        channel.label(SMS.toString());
        channel.packageName(SMS.toString());
        channel.channelSection(General);
        channel.channelType(SMS);
        channel.actionAddress(SMS.toString());
        return channel.build();
    }

    /**
     * Get a dialer channel.
     *
     * @return dialer button
     */
    public static Channel getPhoneChannel() {
        Builder channel = builder();
        channel.id(Id.builder().value(Phone.toString()).build());
        channel.label(Phone.toString());
        channel.packageName(Phone.toString());
        channel.channelSection(General);
        channel.channelType(Phone);
        channel.actionAddress(Phone.toString());
        return channel.build();
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
                channels.add(Channel.create(Id.builder().value(realmChannel.getId()).build(),
                    realmChannel.getLabel(), realmChannel.getPackageName(),
                    getModelChannelSection(realmChannel.getChannelSection()),
                    getModelChannelType(realmChannel.getChannelType()),
                    realmChannel.getActionAddress()));
            }
            return channels;
        }
        return null;
    }

}