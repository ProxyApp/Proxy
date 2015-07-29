package com.shareyourproxy.api.domain.model;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;

import timber.log.Timber;

import static android.graphics.Color.WHITE;
import static com.shareyourproxy.util.ViewUtils.NO_COLOR;

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
    Facebook(5, "Facebook", R.raw.ic_facebook, WHITE),
    Twitter(6, "Twitter", R.raw.ic_twitter, WHITE),
    Meerkat(7, "Meerkat", R.raw.ic_meerkat, NO_COLOR),
    Snapchat(8, "Snapchat", R.raw.ic_snapchat, NO_COLOR),
    Spotify(9, "Spotify", R.raw.ic_spotify, NO_COLOR),
    Reddit(10, "Reddit", R.raw.ic_reddit, NO_COLOR),
    Linkedin(11, "Linkedin", R.raw.ic_linkedin, NO_COLOR),
    FBMessenger(12, "Facebook Messenger", R.raw.ic_facebook_messenger, NO_COLOR),
    Hangouts(13, "Hangouts", R.raw.ic_google_hangouts, NO_COLOR),
    Whatsapp(14, "Whats App", R.raw.ic_whatsapp, NO_COLOR),
    Yo(15, "Yo", R.raw.ic_yo, NO_COLOR),
    Googleplus(16, "Google Plus", R.raw.ic_google_plus, NO_COLOR),
    Github(17, "Github", R.raw.ic_github, NO_COLOR),
    Address(18, "Address", R.raw.ic_address, NO_COLOR),
    Slack(19, "Slack", R.raw.ic_slack, NO_COLOR),
    Youtube(20, "Youtube", R.raw.ic_youtube, NO_COLOR),
    Instagram(21, "Instagram", R.raw.ic_instagram, NO_COLOR),
    Tumblr(22, "Tumblr", R.raw.ic_tumblr, NO_COLOR),
    Ello(23, "Ello", R.raw.ic_ello, NO_COLOR),
    Venmo(24, "Venmo", R.raw.ic_venmo, NO_COLOR),
    Periscope(25, "Periscope", R.raw.ic_periscope, NO_COLOR),
    Medium(26, "Medium", R.raw.ic_medium, NO_COLOR),
    Soundcloud(27, "Soundcloud", R.raw.ic_soundcloud, NO_COLOR),
    Skype(28, "Skype", R.raw.ic_skype, NO_COLOR);



    private final int weight;
    private final String label;
    private final int resId;
    private final int resColor;

    /**
     * Constructor.
     *
     * @param label name of newChannel
     */
    ChannelType(int weight, String label, int resId, int resColor) {
        this.weight = weight;
        this.label = label;
        this.resId = resId;
        this.resColor = resColor;
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

    public int getResColor() {
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

    public static ChannelType valueOfLabel(String label){
        ChannelType[] values = ChannelType.values();
        for (ChannelType value : values) {
            if (value.getLabel().toLowerCase().equals(label.toLowerCase())) {
                return value;
            }
        }
        Timber.e("Bad ChannelType:"+ label);
        throw new IllegalArgumentException();
    }

}