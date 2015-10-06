package com.shareyourproxy.api.domain.model;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;

import java.util.Locale;

import timber.log.Timber;

import static android.graphics.Color.WHITE;

/**
 * Used to sort channels for their eventually called ACTION_INTENT or VIEW_INTENT in {@link
 * IntentLauncher}.
 */
public enum ChannelType {

    Custom(0, "Custom", R.raw.ic_star, WHITE),
    Phone(1, "Phone", R.raw.ic_call, WHITE),
    SMS(2, "SMS", R.raw.ic_sms, WHITE),
    Email(3, "Email", R.raw.ic_email, WHITE),
    Web(4, "Web", R.raw.ic_link, WHITE),
    URL(4, "URL", R.raw.ic_link, WHITE),
    Facebook(5, "Facebook", R.raw.ic_facebook, WHITE),
    Twitter(6, "Twitter", R.raw.ic_twitter, WHITE),
    Meerkat(7, "Meerkat", R.raw.ic_meerkat, null),
    Snapchat(8, "Snapchat", R.raw.ic_snapchat, null),
    Spotify(9, "Spotify", R.raw.ic_spotify, null),
    Reddit(10, "Reddit", R.raw.ic_reddit, null),
    Linkedin(11, "Linkedin", R.raw.ic_linkedin, null),
    FBMessenger(12, "Facebook Messenger", R.raw.ic_facebook_messenger, null),
    Hangouts(13, "Hangouts", R.raw.ic_google_hangouts, null),
    Whatsapp(14, "Whats App", R.raw.ic_whatsapp, null),
    Yo(15, "Yo", R.raw.ic_yo, null),
    Googleplus(16, "Google Plus", R.raw.ic_google_plus, null),
    Github(17, "Github", R.raw.ic_github, null),
    Address(18, "Address", R.raw.ic_address, null),
    Slack(19, "Slack", R.raw.ic_slack, null),
    Youtube(20, "Youtube", R.raw.ic_youtube, null),
    Instagram(21, "Instagram", R.raw.ic_instagram, null),
    Tumblr(22, "Tumblr", R.raw.ic_tumblr, null),
    Ello(23, "Ello", R.raw.ic_ello, null),
    Venmo(24, "Venmo", R.raw.ic_venmo, null),
    Periscope(25, "Periscope", R.raw.ic_periscope, null),
    Medium(26, "Medium", R.raw.ic_medium, null),
    Soundcloud(27, "Soundcloud", R.raw.ic_soundcloud, null),
    Skype(28, "Skype", R.raw.ic_skype, null),
    LeagueOfLegends(29, "League of Legends", R.raw.ic_lol, null),
    PlaystationNetwork(30, "Playstation Network", R.raw.ic_playstation, null),
    NintendoNetwork(31, "Nintendo Network", R.raw.ic_nintendo, null),
    Steam(32, "Steam", R.raw.ic_steam, null),
    Twitch(33, "Twitch", R.raw.ic_twitch, null),
    XboxLive(34, "Xbox Live", R.raw.ic_xbox, null);

    private final int weight;
    private final String label;
    private final int resId;
    private final Integer resColor;

    /**
     * Constructor.
     *
     * @param label name of newChannel
     */
    ChannelType(int weight, String label, int resId, Integer resColor) {
        this.weight = weight;
        this.label = label;
        this.resId = resId;
        this.resColor = resColor;
    }

    public static ChannelType valueOfLabel(String label) {
        ChannelType[] values = ChannelType.values();
        for (ChannelType value : values) {
            String channelLabel = value.getLabel().toLowerCase(Locale.US);
            String lowerCaseLabel = label.toLowerCase(Locale.US);
            if (channelLabel.equals(lowerCaseLabel)) {
                return value;
            }
        }
        Timber.e("Bad ChannelType:" + label);
        throw new IllegalArgumentException();
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

    public int getWeight() {
        return weight;
    }

    public Integer getResColor() {
        return resColor;
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