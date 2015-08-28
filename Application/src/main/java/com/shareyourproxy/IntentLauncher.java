package com.shareyourproxy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.AddChannelListActivity;
import com.shareyourproxy.app.LoginActivity;
import com.shareyourproxy.app.MainActivity;
import com.shareyourproxy.app.SearchActivity;
import com.shareyourproxy.app.UserProfileActivity;

import static com.shareyourproxy.Constants.ARG_ADD_OR_EDIT;
import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.Intents.getUserProfileIntent;


/**
 * Utility for launching Activities.
 */
public final class IntentLauncher {

    /**
     * Private constructor.
     */
    private IntentLauncher() {
    }

    public static void launchAboutActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_VIEW_ABOUT);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * View Address in maps
     *
     * @param activity      context
     * @param actionAddress location
     */
    public static void launchAddressIntent(Activity activity, String actionAddress) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + actionAddress));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * Launch the {@link AddChannelListActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchChannelListActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_ADD_CHANNEL_LIST_VIEW);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * View Ello profile
     *
     * @param activity context
     * @param userId   ello user id
     */
    public static void launchElloIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https:ello.co/" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * Launch Email Intent.
     *
     * @param activity context
     * @param address  to send to
     */
    public static void launchEmailIntent(Activity activity, String address) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ address });
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View facebook user page.
     *
     * @param activity context
     * @param userId   user profile ID
     */
    public static void launchFacebookIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
//        String mobileURI = "https://www.facebook.com/app_scoped_user_id/"+userId;
        intent.setData(Uri.parse("https:www.facebook.com/" + userId));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View facebook help page.
     *
     * @param activity context
     */
    public static void launchFacebookHelpIntent(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http:www.facebook.com/help/211813265517027"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View facebook messenger conversation
     *
     * @param activity context
     * @param userId   fb messenger user id
     */
    public static void launchFBMessengerIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("fb://messaging/" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View Github profile
     *
     * @param activity context
     * @param userId   github user id
     */
    public static void launchGithubIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https:github.com/" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View google plus profile
     *
     * @param activity context
     * @param userId   plus user id
     */
    public static void launchGooglePlusIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://plus.google.com/" + userId + "/posts"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    public static void launchGroupEditChannelActivity(Activity activity, Group group, int addOrEdit) {
        Intent intent = new Intent(Intents.ACTION_EDIT_GROUP_CHANNEL);
        intent.putExtra(ARG_SELECTED_GROUP, group);
        intent.putExtra(ARG_ADD_OR_EDIT, addOrEdit);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * Send SMS to phone number on Hangouts.
     *
     * @param activity      context
     * @param actionAddress to contactId
     */
    public static void launchHangoutsIntent(Activity activity, String actionAddress) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setPackage("com.google.android.talk");
        intent.setData(Uri.parse("content://com.android.contacts/data/" + actionAddress));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View Invite friends
     *
     * @param activity context
     */
    public static void launchInviteFriendIntent(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
            activity.getString(R.string.share_your_proxy));
        intent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.invite_friend_content));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(Intent.createChooser(intent,
                activity.getString(R.string.share_with)));
        }
    }

    /**
     * View Instagram profile
     *
     * @param activity context
     * @param userId   instagram user id
     */
    public static void launchInstagramIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https:instagram.com/_u/" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View Linked In profile
     *
     * @param activity context
     * @param userId   linkedin user id
     */
    public static void launchLinkedInIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https:linkedin.com/in/" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * Launch the {@link LoginActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchLoginActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_LOGIN).addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    /**
     * Launch the {@link MainActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchMainActivity(
        Activity activity, int selectTab, boolean groupDeleted, Group group) {
        Intent intent = new Intent(Intents.ACTION_MAIN_VIEW).addFlags(Intent
            .FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.ARG_SELECTED_MAINFRAGMENT_TAB, selectTab);
        intent.putExtra(Constants.ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED, groupDeleted);
        intent.putExtra(Constants.ARG_MAINGROUPFRAGMENT_DELETED_GROUP, group);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * View Medium profile
     *
     * @param activity context
     * @param userId   medium user id
     */
    public static void launchMediumIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https:medium.com/@" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View Meerkat profile
     *
     * @param activity context
     * @param userId   user profile ID
     */
    public static void launchMeerkatIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://meerkatapp.co/" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * Launch the Dialer App.
     *
     * @param activity    context
     * @param phoneNumber to dial
     */
    public static void launchPhoneIntent(Activity activity, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View Reddit profile or subreddit
     *
     * @param activity      context
     * @param actionAddress endpoint
     */
    public static void launchRedditIntent(Activity activity, String actionAddress) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://reddit.com" + actionAddress));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * Launch the {@link SearchActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchSearchActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_SEARCH_VIEW);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * View skype profile
     *
     * @param activity context
     * @param username skype username
     */
    public static void launchSkypeIntent(Activity activity, String username) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("skype:" + username));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * Send SMS to phone number.
     *
     * @param activity    context
     * @param phoneNumber to sms
     */
    public static void launchSMSIntent(Activity activity, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View SnapChant profile
     *
     * @param activity      context
     * @param actionAddress location
     */
    public static void launchSnapChatIntent(Activity activity, String actionAddress) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http:snapchat.com/add/" + actionAddress));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View SoundCloud profile
     *
     * @param activity context
     * @param userId   soundcloud user id
     */
    public static void launchSoundCloudIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https:m.soundcloud.com/" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View Spotify profile
     *
     * @param activity      context
     * @param actionAddress user endpoint
     */
    public static void launchSpotifyIntent(Activity activity, String actionAddress) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("spotify:user:" + actionAddress));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View Tumblr profile
     *
     * @param activity context
     * @param userId   tumblr user id
     */
    public static void launchTumblrIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.getPackageManager().getPackageInfo("com.tumblr", 0);
            intent.setData(
                Uri.parse(
                    "http://www.tumblr" +
                        ".com/open/app?referrer=mobile_banner&app_args=blog%3FblogName%3D"
                        + userId + "%26page%3Dblog"));
        } catch (Exception e) {
            intent.setData(Uri.parse("http://" + userId + ".tumblr.com"));
        }
        activity.startActivity(intent);
    }

    /**
     * View Twitter profile
     *
     * @param activity context
     * @param userId   user profile ID
     */
    public static void launchTwitterIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("twitter://user?screen_name=" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) == null) {
            intent.setData(Uri.parse("https://mobile.twitter.com/" + userId));
        }
        activity.startActivity(intent);
    }

    /**
     * Launch the {@link UserProfileActivity}.
     *
     * @param activity The context used to start this intent
     * @param user     that was selected
     */
    public static void launchUserProfileActivity(
        Activity activity, User user, String loggedInUserId) {
        Intent intent = getUserProfileIntent(user, loggedInUserId);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * View Venmo profile
     *
     * @param activity context
     * @param userId   venmo user id
     */
    public static void launchVenmoIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https:venmo.com/" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * Launch the {@link SearchActivity}.
     *
     * @param activity The context used to start this intent
     * @param group    group data
     */
    public static void launchViewGroupUsersActivity(Activity activity, Group group) {
        Intent intent = new Intent(Intents.ACTION_VIEW_GROUP_USERS);
        intent.putExtra(ARG_SELECTED_GROUP, group);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * View Whats App profile
     *
     * @param activity context
     * @param address  whats app number
     */
    public static void launchWhatsAppIntent(Activity activity, String address) {
        Intent intent = new Intent();
        intent.setPackage("com.whatsapp");
        intent.setData(Uri.parse("smsto:" + address));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    public static void launchWebIntent(Activity activity, String address) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http:" + address));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, "Invalid link", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * View yo profile
     *
     * @param activity context
     * @param username yo username
     */
    public static void launchYoIntent(Activity activity, String username) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("yo:add:" + username));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View Youtube profile
     *
     * @param activity context
     * @param userId   youtube user id
     */
    public static void launchYoutubeIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://www.youtube.com/user/" + userId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }
}

