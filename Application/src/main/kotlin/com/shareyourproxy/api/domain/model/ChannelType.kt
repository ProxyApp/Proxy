package com.shareyourproxy.api.domain.model

import android.graphics.Color.WHITE
import com.shareyourproxy.R
import timber.log.Timber
import java.util.*

/**
 * Used to sort channels for their eventually called ACTION_INTENT or VIEW_INTENT in [IntentLauncher].
 */
enum class ChannelType
/**
 * Constructor.
 * @param label name of newChannel
 */
private constructor(val weight: Int, val label: String, val resId: Int, val resColor: Int?) {
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

    override fun toString(): String {
        return label
    }

    companion object {
        fun valueOfLabel(label: String): ChannelType {
            val values = ChannelType.values()
            for (value in values) {
                val channelLabel = value.label.toLowerCase(Locale.US)
                val lowerCaseLabel = label.toLowerCase(Locale.US)
                if (channelLabel == lowerCaseLabel) {
                    return value
                }
            }
            Timber.e("Bad ChannelType: $label")
            throw IllegalArgumentException()
        }
    }

}