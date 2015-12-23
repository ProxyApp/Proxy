package com.shareyourproxy

import android.app.Activity
import android.content.Intent
import android.content.Intent.*
import android.net.Uri.parse
import android.support.v4.app.ActivityOptionsCompat.makeSceneTransitionAnimation
import android.support.v4.util.Pair.create
import android.view.View
import android.view.Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME
import android.view.Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.shareyourproxy.Constants.ARG_EDIT_GROUP_TYPE
import com.shareyourproxy.Constants.ARG_MAINFRAGMENT_SELECTED_TAB
import com.shareyourproxy.Constants.ARG_MAINGROUPFRAGMENT_DELETED_GROUP
import com.shareyourproxy.Constants.ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED
import com.shareyourproxy.Constants.ARG_SELECTED_GROUP
import com.shareyourproxy.Intents.ACTION_ADD_CHANNEL_LIST_VIEW
import com.shareyourproxy.Intents.ACTION_EDIT_GROUP_CHANNEL
import com.shareyourproxy.Intents.ACTION_LOGIN
import com.shareyourproxy.Intents.ACTION_MAIN_VIEW
import com.shareyourproxy.Intents.ACTION_SEARCH_VIEW
import com.shareyourproxy.Intents.ACTION_VIEW_ABOUT
import com.shareyourproxy.Intents.ACTION_VIEW_GROUP_USERS
import com.shareyourproxy.Intents.getClipboardIntent
import com.shareyourproxy.Intents.getShareLinkIntent
import com.shareyourproxy.Intents.getUserProfileIntent
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.command.eventcallback.ShareLinkEventCallback
import com.shareyourproxy.app.EditGroupChannelsActivity


/**
 * Utility for launching Activities.
 */
object IntentLauncher {

    /**
     * BEGIN Activity Intents: Launch an About Activity
     * @param activity context
     */
    fun launchAboutActivity(activity: Activity) {
        val intent = Intent(ACTION_VIEW_ABOUT)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out)
    }

    /**
     * Launch the [AddChannelListActivity].
     * @param activity The context used to start this intent
     */
    fun launchChannelListActivity(activity: Activity) {
        val intent = Intent(ACTION_ADD_CHANNEL_LIST_VIEW)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out)
    }

    /**
     * Add a new group or edit a selected group and it's channels.
     * @param activity      context
     * @param group         selected
     * @param groupEditType add edit or public group
     */
    fun launchEditGroupChannelsActivity(
            activity: Activity, group: Group, groupEditType: EditGroupChannelsActivity.GroupEditType) {
        val intent = Intent(ACTION_EDIT_GROUP_CHANNEL)
        intent.putExtra(ARG_SELECTED_GROUP, group)
        intent.putExtra(ARG_EDIT_GROUP_TYPE, groupEditType)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out)
    }

    /**
     * Launch the [GroupContactsActivity].
     * @param activity The context used to start this intent
     * @param group    group data
     */
    fun launchEditGroupContactsActivity(activity: Activity, group: Group) {
        val intent = Intent(ACTION_VIEW_GROUP_USERS)
        intent.putExtra(ARG_SELECTED_GROUP, group)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out)
    }

    /**
     * Launch the [com.shareyourproxy.app.IntroductionActivity].
     * @param activity The context used to start this intent
     */
    fun launchIntroductionActivity(activity: Activity) {
        val intent = Intent(Intents.ACTION_INTRODUCTION).addFlags(
                FLAG_ACTIVITY_CLEAR_TOP)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    /**
     * Launch the [LoginActivity].
     * @param activity The context used to start this intent
     */
    fun launchLoginActivity(activity: Activity) {
        val intent = Intent(ACTION_LOGIN).addFlags(FLAG_ACTIVITY_CLEAR_TOP)
        activity.startActivity(intent)
    }

    /**
     * Launch the [AggregateFeedActivity].
     * @param activity The context used to start this intent
     */
    fun launchMainActivity(
            activity: Activity, selectTab: Int, groupDeleted: Boolean, group: Group) {
        val intent = Intent(ACTION_MAIN_VIEW).addFlags(FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(ARG_MAINFRAGMENT_SELECTED_TAB, selectTab)
        intent.putExtra(ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED, groupDeleted)
        intent.putExtra(ARG_MAINGROUPFRAGMENT_DELETED_GROUP, group)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out)
    }

    /**
     * Launch the [SearchActivity].
     * @param activity The context used to start this intent
     * @param textView search textview to animate
     * @param menu     hamburger icon to animate
     */
    fun launchSearchActivity(
            activity: Activity, container: View, textView: View, menu: View) {
        val intent = Intent(ACTION_SEARCH_VIEW)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val statusbar = activity.findViewById(android.R.id.statusBarBackground)
            val actionbar = activity.findViewById(android.R.id.navigationBarBackground)

            val pair1 = create(textView, textView.transitionName)
            val pair2 = create(menu, menu.transitionName)
            val pair3 = create(container, container.transitionName)
            val pair4 = create(statusbar, STATUS_BAR_BACKGROUND_TRANSITION_NAME)
            val pair5 = create(actionbar, NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)

            val options = makeSceneTransitionAnimation(activity, pair1, pair2, pair3, pair4, pair5)
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
        }

    }

    /**
     * Launch the [UserContactActivity].
     * @param activity The context used to start this intent
     * @param user     that was selected
     */
    fun launchUserProfileActivity(
            activity: Activity, user: User, loggedInUserId: String) {
        val intent = getUserProfileIntent(user, loggedInUserId)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out)
    }

    /**
     * Launch the [UserContactActivity].
     * @param activity The context used to start this intent
     * @param user     that was selected
     */
    fun launchUserProfileActivity(
            activity: Activity, user: User, loggedInUserId: String, profileImage: View, userName: View) {
        val intent = getUserProfileIntent(user, loggedInUserId)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val statusbar = activity.findViewById(android.R.id.statusBarBackground)
            val actionbar = activity.findViewById(android.R.id.navigationBarBackground)

            val pair1 = create(profileImage, profileImage.transitionName)
            val pair2 = create(userName, userName.transitionName)
            val pair3 = create(statusbar, STATUS_BAR_BACKGROUND_TRANSITION_NAME)
            val pair4 = create(actionbar, NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)

            val options = makeSceneTransitionAnimation(activity, pair1, pair2, pair3, pair4)
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
        }
    }

    /**
     * BEGIN Channel Intents: View Address in maps
     * @param activity      context
     * @param actionAddress location
     */
    fun launchAddressIntent(activity: Activity, actionAddress: String) {
        val sb = StringBuilder("geo:0,0?q=").append(actionAddress)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }


    /**
     * View Ello profile
     * @param activity context
     * @param userId   ello user id
     */
    fun launchElloIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("https:ello.co/").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * Launch Email Intent.
     * @param activity context
     * @param address  to send to
     */
    fun launchEmailIntent(activity: Activity, address: String) {
        val intent = Intent(ACTION_SENDTO)
        intent.setData(parse("mailto:"))

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(EXTRA_EMAIL, arrayOf(address))
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View facebook user page.
     * @param activity context
     * @param userId   user profile ID
     */
    fun launchFacebookIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("https:www.facebook.com/").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View facebook help page.
     * @param activity context
     */
    fun launchFacebookHelpIntent(activity: Activity) {
        val intent = Intent(ACTION_VIEW)
        intent.setData(parse("http:www.facebook.com/help/211813265517027"))

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View facebook messenger conversation
     * @param activity context
     * @param userId   fb messenger user id
     */
    fun launchFBMessengerIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("fb://messaging/").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View Github profile
     * @param activity context
     * @param userId   github user id
     */
    fun launchGithubIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("https:github.com/").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View google plus profile
     * @param activity context
     * @param userId   plus user id
     */
    fun launchGooglePlusIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("https://plus.google.com/").append(userId).append("/posts")
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * Send SMS to phone number on Hangouts.
     * @param activity      context
     * @param actionAddress to contactId
     */
    fun launchHangoutsIntent(activity: Activity, actionAddress: String) {
        val sb = StringBuilder("content://com.android.contacts/data/").append(actionAddress)
        val intent = Intent(ACTION_SENDTO, parse(sb.toString()))
        intent.setPackage("com.google.android.talk")

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View Invite friends
     * @param activity context
     */
    fun launchInviteFriendIntent(activity: Activity) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT,
                activity.getString(R.string.share_your_proxy))
        intent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.invite_friend_content))
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(createChooser(intent,
                    activity.getString(R.string.invite_a_friend)))
        }
    }

    /**
     * View Instagram profile
     * @param activity context
     * @param userId   instagram user id
     */
    fun launchInstagramIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("https:instagram.com/_u/").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * Launch a lol profile link.
     * @param activity context
     * @param address  address
     */
    fun launchLeagueOfLegendsIntent(activity: Activity, address: String) {
        val sb = StringBuilder("http:boards.na.leagueoflegends.com/en/player/NA/").append(address)

        val intent = Intent(ACTION_VIEW, parse(sb.toString()))
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Toast.makeText(activity, "Invalid link", LENGTH_LONG).show()
        }
    }

    /**
     * View Linked In profile
     * @param activity context
     * @param userId   linkedin user id
     */
    fun launchLinkedInIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("https:linkedin.com/in/").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View Medium profile
     * @param activity context
     * @param userId   medium user id
     */
    fun launchMediumIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("https:medium.com/@").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View Meerkat profile
     * @param activity context
     * @param userId   user profile ID
     */
    fun launchMeerkatIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("https://meerkatapp.co/").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * Launch a nintendo network profile link.
     * @param activity context
     * @param address  address
     */
    fun launchNintendoNetworkIntent(activity: Activity, address: String) {
        val sb = StringBuilder("http:miiverse.nintendo.net/users/").append(address)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Toast.makeText(activity, "Invalid link", LENGTH_LONG).show()
        }
    }

    /**
     * Launch the Dialer App.
     * @param activity    context
     * @param phoneNumber to dial
     */
    fun launchPhoneIntent(activity: Activity, phoneNumber: String) {
        val intent = Intent(ACTION_DIAL)
        intent.setData(parse("tel:" + phoneNumber))
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * Launch a playstation network profile link.
     * @param activity context
     * @param address  address
     */
    fun launchPlaystationNetworkIntent(activity: Activity, address: String) {
        val sb = StringBuilder("http:psnprofiles.com/").append(address)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Toast.makeText(activity, "Invalid link", LENGTH_LONG).show()
        }
    }

    /**
     * View Reddit profile or subreddit
     * @param activity      context
     * @param actionAddress endpoint
     */
    fun launchRedditIntent(activity: Activity, actionAddress: String) {
        val sb = StringBuilder("https://reddit.com").append(actionAddress)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * Launch an Intent chooser dialog for a Proxy User to select a method of sharing a profile link. The link is an http address to a User's group channels.
     * @param event message data, http link
     */
    fun launchShareLinkIntent(activity: Activity, event: ShareLinkEventCallback) {
        val copyAndPaste = arrayOf(getClipboardIntent(event.message))
        val chooser = createChooser(getShareLinkIntent(event.message),
                activity.getString(R.string.dialog_sharelink_title)).putExtra(EXTRA_INITIAL_INTENTS, copyAndPaste)
        activity.startActivity(chooser)
    }

    /**
     * View skype profile
     * @param activity context
     * @param username skype username
     */
    fun launchSkypeIntent(activity: Activity, username: String) {
        val intent = Intent(ACTION_VIEW)
        intent.setData(parse("skype:" + username))
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * Send SMS to phone number.
     * @param activity    context
     * @param phoneNumber to sms
     */
    fun launchSMSIntent(activity: Activity, phoneNumber: String) {
        val intent = Intent(ACTION_SENDTO)
        intent.setData(parse("smsto:" + phoneNumber))
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View SnapChant profile
     * @param activity      context
     * @param actionAddress location
     */
    fun launchSnapChatIntent(activity: Activity, actionAddress: String) {
        val sb = StringBuilder("http:snapchat.com/add/").append(actionAddress)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View SoundCloud profile
     * @param activity context
     * @param userId   soundcloud user id
     */
    fun launchSoundCloudIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("http:soundcloud.com/").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View Spotify profile

     * @param activity      context
     * *
     * @param actionAddress user endpoint
     */
    fun launchSpotifyIntent(activity: Activity, actionAddress: String) {
        val sb = StringBuilder("spotify:user:").append(actionAddress)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)

        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * Launch a steam profile link.
     * @param activity context
     * @param address  http address
     */
    fun launchSteamIntent(activity: Activity, address: String) {
        val sb = StringBuilder("http:steamcommunity.com/id/").append(address)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Toast.makeText(activity, "Invalid link", LENGTH_LONG).show()
        }
    }

    /**
     * View Tumblr profile
     * @param activity context
     * @param userId   tumblr user id
     */
    fun launchTumblrIntent(activity: Activity, userId: String) {
        val intent = Intent(ACTION_VIEW)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.packageManager.getPackageInfo("com.tumblr", 0)
            intent.setData(
                    parse(
                            "http://www.tumblr.com/open/app?referrer=mobActivityCompat.postponeEnterTransition(this);ile_banner&app_args=blog%3FblogName%3D$userId%26page%3Dblog"))
        } catch (e: Exception) {
            intent.setData(parse("http://$userId.tumblr.com"))
        }

        activity.startActivity(intent)
    }

    /**
     * Launch a twitch profile link.
     * @param activity context
     * @param address  http address
     */
    fun launchTwitchIntent(activity: Activity, address: String) {
        val sb = StringBuilder("http:www.twitch.tv/").append(address)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Toast.makeText(activity, "Invalid link", LENGTH_LONG).show()
        }
    }

    /**
     * View Twitter profile
     * @param activity context
     * @param userId   user profile ID
     */
    fun launchTwitterIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("twitter://user?screen_name=").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) == null) {
            intent.setData(parse("https://mobile.twitter.com/" + userId))
        }
        activity.startActivity(intent)
    }

    /**
     * View Venmo profile
     * @param activity context
     * @param userId   venmo user id
     */
    fun launchVenmoIntent(activity: Activity, userId: String) {
        val sb = StringBuilder("https:venmo.com/").append(userId)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View Whats App profile
     * @param activity context
     * @param address  whats app number
     */
    fun launchWhatsAppIntent(activity: Activity, address: String) {
        val intent = Intent()
        intent.setPackage("com.whatsapp")
        intent.setData(parse("smsto:" + address))
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * Launch a general web link.
     * @param activity context
     * @param address  http address
     */
    fun launchWebIntent(activity: Activity, address: String) {
        val sb = StringBuilder("http:").append(address)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Toast.makeText(activity, "Invalid link", LENGTH_LONG).show()
        }
    }

    /**
     * Launch a xbox live profile link.
     * @param activity context
     * @param address  http address
     */
    fun launchXboxLiveIntent(activity: Activity, address: String) {
        val sb = StringBuilder("http:live.xbox.com/en-US/Profile?Gamertag=").append(address)
        val intent = Intent(ACTION_VIEW, parse(sb.toString()))

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Toast.makeText(activity, "Invalid link", LENGTH_LONG).show()
        }
    }

    /**
     * View yo profile
     * @param activity context
     * @param username yo username
     */
    fun launchYoIntent(activity: Activity, username: String) {
        val intent = Intent()
        intent.setData(parse("yo:add:" + username))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    /**
     * View Youtube profile
     * @param activity context
     * @param userId   youtube user id
     */
    fun launchYoutubeIntent(activity: Activity, userId: String) {
        val intent = Intent(ACTION_VIEW)
        intent.setData(parse("http://www.youtube.com/user/" + userId))

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }
}
