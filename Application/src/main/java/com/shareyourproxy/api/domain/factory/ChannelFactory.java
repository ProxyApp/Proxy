package com.shareyourproxy.api.domain.factory;


import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Channel.Builder;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.realm.RealmChannel;
import com.shareyourproxy.api.domain.realm.RealmChannelType;
import com.shareyourproxy.api.domain.realm.RealmString;

import java.util.HashMap;
import java.util.HashSet;

import io.realm.RealmList;

import static com.shareyourproxy.api.domain.model.Channel.builder;
import static com.shareyourproxy.api.domain.model.ChannelType.Address;
import static com.shareyourproxy.api.domain.model.ChannelType.Ello;
import static com.shareyourproxy.api.domain.model.ChannelType.Email;
import static com.shareyourproxy.api.domain.model.ChannelType.FBMessenger;
import static com.shareyourproxy.api.domain.model.ChannelType.Facebook;
import static com.shareyourproxy.api.domain.model.ChannelType.Github;
import static com.shareyourproxy.api.domain.model.ChannelType.Googleplus;
import static com.shareyourproxy.api.domain.model.ChannelType.Hangouts;
import static com.shareyourproxy.api.domain.model.ChannelType.Instagram;
import static com.shareyourproxy.api.domain.model.ChannelType.Linkedin;
import static com.shareyourproxy.api.domain.model.ChannelType.Medium;
import static com.shareyourproxy.api.domain.model.ChannelType.Meerkat;
import static com.shareyourproxy.api.domain.model.ChannelType.Periscope;
import static com.shareyourproxy.api.domain.model.ChannelType.Phone;
import static com.shareyourproxy.api.domain.model.ChannelType.Reddit;
import static com.shareyourproxy.api.domain.model.ChannelType.SMS;
import static com.shareyourproxy.api.domain.model.ChannelType.Skype;
import static com.shareyourproxy.api.domain.model.ChannelType.Slack;
import static com.shareyourproxy.api.domain.model.ChannelType.Snapchat;
import static com.shareyourproxy.api.domain.model.ChannelType.Soundcloud;
import static com.shareyourproxy.api.domain.model.ChannelType.Spotify;
import static com.shareyourproxy.api.domain.model.ChannelType.Tumblr;
import static com.shareyourproxy.api.domain.model.ChannelType.Twitter;
import static com.shareyourproxy.api.domain.model.ChannelType.Venmo;
import static com.shareyourproxy.api.domain.model.ChannelType.Web;
import static com.shareyourproxy.api.domain.model.ChannelType.Whatsapp;
import static com.shareyourproxy.api.domain.model.ChannelType.Yo;
import static com.shareyourproxy.api.domain.model.ChannelType.Youtube;
import static com.shareyourproxy.api.domain.model.ChannelType.valueOfLabel;


/**
 * Factory to create realm channels.
 */
public class ChannelFactory {

    public static Channel createModelInstance(
        String id, String label, ChannelType channelType,
        String actionAddress) {
        Channel.Builder channel = Channel.builder();
        channel.id(id);
        channel.label(label);
        channel.actionAddress(actionAddress);
        channel.channelType(channelType);
        channel.isPublic(false);
        return channel.build();
    }

    public static Channel createModelInstance(Channel copyChannel, String actionAddress) {
        Channel.Builder channel = Channel.builder();
        channel.id(copyChannel.id());
        channel.label(copyChannel.label());
        channel.actionAddress(actionAddress);
        channel.channelType(copyChannel.channelType());
        channel.isPublic(false);
        return channel.build();
    }

    public static Channel createPublicChannel(Channel copyChannel, Boolean isPublic) {
        Channel.Builder channel = Channel.builder();
        channel.id(copyChannel.id());
        channel.label(copyChannel.label());
        channel.actionAddress(copyChannel.actionAddress());
        channel.channelType(copyChannel.channelType());
        channel.isPublic(isPublic);
        return channel.build();
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
     * Get a web newChannel.
     *
     * @return web button
     */
    public static Channel getWebChannel() {
        Builder channel = builder();
        channel.id(Web.toString());
        channel.label(Web.toString());
        channel.channelType(Web);
        channel.actionAddress(Web.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a web newChannel.
     *
     * @return web button
     */
    public static Channel getFacebookChannel() {
        Builder channel = builder();
        channel.id(Facebook.toString());
        channel.label(Facebook.toString());
        channel.channelType(Facebook);
        channel.actionAddress(Facebook.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new twitter channel.
     *
     * @return twitter
     */
    public static Channel getTwitterChannel() {
        Builder channel = builder();
        channel.id(Twitter.toString());
        channel.label(Twitter.toString());
        channel.channelType(Twitter);
        channel.actionAddress(Twitter.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new meerkat channel.
     *
     * @return meerkat
     */
    public static Channel getMeerkatChannel() {
        Builder channel = builder();
        channel.id(Meerkat.toString());
        channel.label(Meerkat.toString());
        channel.channelType(Meerkat);
        channel.actionAddress(Meerkat.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new snapchat channel.
     *
     * @return snapchat
     */
    public static Channel getSnapchatChannel() {
        Builder channel = builder();
        channel.id(Snapchat.toString());
        channel.label(Snapchat.toString());
        channel.channelType(Snapchat);
        channel.actionAddress(Snapchat.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new spotify channel.
     *
     * @return spotify
     */
    public static Channel getSpotifyChannel() {
        Builder channel = builder();
        channel.id(Spotify.toString());
        channel.label(Spotify.toString());
        channel.channelType(Spotify);
        channel.actionAddress(Spotify.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new LinkedIn channel.
     *
     * @return LinkedIn
     */
    public static Channel getLinkedInChannel() {
        Builder channel = builder();
        channel.id(Linkedin.toString());
        channel.label(Linkedin.toString());
        channel.channelType(Linkedin);
        channel.actionAddress(Linkedin.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new FB Messenger channel.
     *
     * @return FB Messenger
     */
    public static Channel getFBMessengerChannel() {
        Builder channel = builder();
        channel.id(FBMessenger.toString());
        channel.label(FBMessenger.toString());
        channel.channelType(FBMessenger);
        channel.actionAddress(FBMessenger.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Google Hangouts channel.
     *
     * @return Hangouts
     */
    public static Channel getHangoutsChannel() {
        Builder channel = builder();
        channel.id(Hangouts.toString());
        channel.label(Hangouts.toString());
        channel.channelType(Hangouts);
        channel.actionAddress(Hangouts.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Whatsapp channel.
     *
     * @return Whatsapp
     */
    public static Channel getWhatsappChannel() {
        Builder channel = builder();
        channel.id(Whatsapp.toString());
        channel.label(Whatsapp.toString());
        channel.channelType(Whatsapp);
        channel.actionAddress(Whatsapp.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Reddit channel.
     *
     * @return Reddit
     */
    public static Channel getRedditChannel() {
        Builder channel = builder();
        channel.id(Reddit.toString());
        channel.label(Reddit.toString());
        channel.channelType(Reddit);
        channel.actionAddress(Reddit.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Yo channel.
     *
     * @return Yo
     */
    public static Channel getYoChannel() {
        Builder channel = builder();
        channel.id(Yo.toString());
        channel.label(Yo.toString());
        channel.channelType(Yo);
        channel.actionAddress(Yo.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Google Plus channel.
     *
     * @return Google Plus
     */
    public static Channel getGooglePlusChannel() {
        Builder channel = builder();
        channel.id(Googleplus.toString());
        channel.label(Googleplus.toString());
        channel.channelType(Googleplus);
        channel.actionAddress(Googleplus.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Github channel.
     *
     * @return Github
     */
    public static Channel getGithubChannel() {
        Builder channel = builder();
        channel.id(Github.toString());
        channel.label(Github.toString());
        channel.channelType(Github);
        channel.actionAddress(Github.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Address channel.
     *
     * @return Address
     */
    public static Channel getAddressChannel() {
        Builder channel = builder();
        channel.id(Address.toString());
        channel.label(Address.toString());
        channel.channelType(Address);
        channel.actionAddress(Address.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Slack channel.
     *
     * @return Slack
     */
    public static Channel getSlackChannel() {
        Builder channel = builder();
        channel.id(Slack.toString());
        channel.label(Slack.toString());
        channel.channelType(Slack);
        channel.actionAddress(Slack.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Youtube channel.
     *
     * @return Youtube
     */
    public static Channel getYoutubeChannel() {
        Builder channel = builder();
        channel.id(Youtube.toString());
        channel.label(Youtube.toString());
        channel.channelType(Youtube);
        channel.actionAddress(Youtube.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Instagram channel.
     *
     * @return Instagram
     */
    public static Channel getInstagramChannel() {
        Builder channel = builder();
        channel.id(Instagram.toString());
        channel.label(Instagram.toString());
        channel.channelType(Instagram);
        channel.actionAddress(Instagram.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Tumblr channel.
     *
     * @return Tumblr
     */
    public static Channel getTumblrChannel() {
        Builder channel = builder();
        channel.id(Tumblr.toString());
        channel.label(Tumblr.toString());
        channel.channelType(Tumblr);
        channel.actionAddress(Tumblr.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Ello channel.
     *
     * @return Ello
     */
    public static Channel getElloChannel() {
        Builder channel = builder();
        channel.id(Ello.toString());
        channel.label(Ello.toString());
        channel.channelType(Ello);
        channel.actionAddress(Ello.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Venmo channel.
     *
     * @return Venmo
     */
    public static Channel getVenmoChannel() {
        Builder channel = builder();
        channel.id(Venmo.toString());
        channel.label(Venmo.toString());
        channel.channelType(Venmo);
        channel.actionAddress(Venmo.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Periscope channel.
     *
     * @return Periscope
     */
    public static Channel getPeriscopeChannel() {
        Builder channel = builder();
        channel.id(Periscope.toString());
        channel.label(Periscope.toString());
        channel.channelType(Periscope);
        channel.actionAddress(Periscope.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Medium channel.
     *
     * @return Medium
     */
    public static Channel getMediumChannel() {
        Builder channel = builder();
        channel.id(Medium.toString());
        channel.label(Medium.toString());
        channel.channelType(Medium);
        channel.actionAddress(Medium.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new SoundCloud channel.
     *
     * @return SoundCloud
     */
    public static Channel getSoundCloudChannel() {
        Builder channel = builder();
        channel.id(Soundcloud.toString());
        channel.label(Soundcloud.toString());
        channel.channelType(Soundcloud);
        channel.actionAddress(Soundcloud.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a new Skype channel.
     *
     * @return Skype
     */
    public static Channel getSkypeChannel() {
        Builder channel = builder();
        channel.id(Skype.toString());
        channel.label(Skype.toString());
        channel.channelType(Skype);
        channel.actionAddress(Skype.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a gmail newChannel.
     *
     * @return gmail button
     */
    public static Channel getEmailChannel() {
        Builder channel = builder();
        channel.id(Email.toString());
        channel.label(Email.toString());
        channel.channelType(Email);
        channel.actionAddress(Email.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a hangouts newChannel.
     *
     * @return hangouts button
     */
    public static Channel getSMSChannel() {
        Builder channel = builder();
        channel.id(SMS.toString());
        channel.label(SMS.toString());
        channel.channelType(SMS);
        channel.actionAddress(SMS.toString());
        channel.isPublic(false);
        return channel.build();
    }

    /**
     * Get a dialer newChannel.
     *
     * @return dialer button
     */
    public static Channel getPhoneChannel() {
        Builder channel = builder();
        channel.id(Phone.toString());
        channel.label(Phone.toString());
        channel.channelType(Phone);
        channel.actionAddress(Phone.toString());
        channel.isPublic(false);
        return channel.build();
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