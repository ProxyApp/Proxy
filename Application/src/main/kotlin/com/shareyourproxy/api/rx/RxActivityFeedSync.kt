package com.shareyourproxy.api.rx

import android.content.Context
import android.util.Log
import com.shareyourproxy.R
import com.shareyourproxy.api.TwitterRestClient
import com.shareyourproxy.api.domain.model.ActivityFeedItem
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.rx.command.eventcallback.ActivityFeedDownloadedEvent
import com.shareyourproxy.util.ObjectUtils.getTwitterDateFormat
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.models.Tweet
import rx.Observable
import rx.functions.Func1
import timber.log.Timber
import java.text.ParseException
import java.util.*

/**
 * Created by Evan on 10/12/15.
 */
object RxActivityFeedSync {
    private val _dateFormat = getTwitterDateFormat()
    private var _sinceId = 0L
    private var _maxId = 0L
    private val ITEM_COUNT = 10L

    fun getChannelFeed(
            context: Context, session: TwitterSession,
            channels: HashMap<String, Channel>): Observable<ActivityFeedDownloadedEvent> {
        return Observable.defer { Observable.from(channels.values).map(getServiceObservable(context, session)).filter(RxHelper.filterNullObject()).map(syncronizeData()).map(createEvent()) }
    }

    private fun getServiceObservable(
            context: Context, twitterSession: TwitterSession?): Func1<Channel, Observable<List<ActivityFeedItem>>> {
        return Func1 { channel ->
            var observe: Observable<List<ActivityFeedItem>>? = null
            when (channel.channelType()) {
                ChannelType.Twitter -> if (twitterSession != null) {
                    observe = getTwitterActivity(context, channel, twitterSession)
                } else {
                    observe = Observable.just(ActivityFeedItem.createEmpty(channel.channelType())).toList()
                }
                ChannelType.Youtube -> {
                }
                ChannelType.Custom -> {
                }
                ChannelType.Phone -> {
                }
                ChannelType.SMS -> {
                }
                ChannelType.Email -> {
                }
                ChannelType.Web -> {
                }
                ChannelType.URL -> {
                }
                ChannelType.Facebook -> {
                }
                ChannelType.Meerkat -> {
                }
                ChannelType.Snapchat -> {
                }
                ChannelType.Spotify -> {
                }
                ChannelType.Reddit -> {
                }
                ChannelType.Linkedin -> {
                }
                ChannelType.FBMessenger -> {
                }
                ChannelType.Hangouts -> {
                }
                ChannelType.Whatsapp -> {
                }
                ChannelType.Yo -> {
                }
                ChannelType.Googleplus -> {
                }
                ChannelType.Github -> {
                }
                ChannelType.Address -> {
                }
                ChannelType.Slack -> {
                }
                ChannelType.Instagram -> {
                }
                ChannelType.Tumblr -> {
                }
                ChannelType.Ello -> {
                }
                ChannelType.Venmo -> {
                }
                ChannelType.Periscope -> {
                }
                ChannelType.Medium -> {
                }
                ChannelType.Soundcloud -> {
                }
                ChannelType.Skype -> {
                }
            }
            observe
        }
    }

    private fun createEvent(): Func1<List<ActivityFeedItem>, ActivityFeedDownloadedEvent> {
        return Func1 { activityFeedItems -> ActivityFeedDownloadedEvent(activityFeedItems) }
    }

    private fun syncronizeData(): Func1<Observable<List<ActivityFeedItem>>, List<ActivityFeedItem>> {
        return Func1 { observer -> observer.toBlocking().single() }
    }

    private fun getTwitterActivity(
            context: Context,
            channel: Channel, session: TwitterSession): Observable<List<ActivityFeedItem>> {
        if (_sinceId.equals(0) || _maxId.equals(0)) {
            return TwitterRestClient.newInstance(session).userService.getUserTimeline(java.lang.Long.valueOf(channel.id()), ITEM_COUNT).map(timelineToFeedItem(context, channel))
        } else {
            return TwitterRestClient.newInstance(session).userService.getUserTimeline(java.lang.Long.valueOf(channel.id()), ITEM_COUNT, _sinceId, _maxId).map(timelineToFeedItem(context, channel))
        }
    }

    /**
     * https://dev.twitter.com/rest/public/timelines.

     * @param channel twitter channel data
     * *
     * @return activity feed items
     */
    private fun timelineToFeedItem(
            context: Context, channel: Channel): Func1<List<Tweet>, List<ActivityFeedItem>> {
        return Func1 { tweets ->
            val channelType = channel.channelType()
            val label = channel.actionAddress()
            val feedItems = ArrayList<ActivityFeedItem>(tweets.size)
            for (tweet in tweets) {
                val idVal = tweet.id
                _sinceId = if (idVal.compareTo(_sinceId) > 0) idVal else _sinceId
                _maxId = if (idVal.compareTo(_maxId) < 0) (idVal - 1) else _maxId
                var date = Date()
                try {
                    date = _dateFormat.parse(tweet.createdAt)
                } catch (e: ParseException) {
                    Timber.e("Parse Exception: ${Log.getStackTraceString(e)}")
                }

                val subtext = context.getString(R.string.posted_on, channelType)
                feedItems.add(ActivityFeedItem.create(label, subtext, channelType,
                        getTwitterLink(tweet), date))
            }
            feedItems
        }
    }

    private fun getTwitterLink(tweet: Tweet): String {
        return StringBuilder("twitter.com/").append(tweet.user.id).append("/status/").append(tweet.id).toString()
    }
}
