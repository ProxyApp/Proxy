package com.shareyourproxy.api.rx;

import android.content.Context;
import android.util.Log;

import com.shareyourproxy.R;
import com.shareyourproxy.api.TwitterRestClient;
import com.shareyourproxy.api.domain.model.ActivityFeedItem;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.rx.command.eventcallback.ActivityFeedDownloadedEvent;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

import static com.shareyourproxy.api.rx.RxHelper.filterNullObject;
import static com.shareyourproxy.util.ObjectUtils.getTwitterDateFormat;

/**
 * Created by Evan on 10/12/15.
 */
public class RxActivityFeedSync {

    private static final long ITEM_COUNT = 10L;
    private static RxActivityFeedSync INSTANCE;
    private final SimpleDateFormat _dateFormat = getTwitterDateFormat();
    private long _sinceId = 0L;
    private long _maxId = 0L;

    private RxActivityFeedSync() {
    }

    public static RxActivityFeedSync getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RxActivityFeedSync();
        }
        return INSTANCE;
    }

    public Observable<ActivityFeedDownloadedEvent> getChannelFeed(
        final Context context, final TwitterSession session,
        final HashMap<String, Channel> channels) {
        return Observable.defer(new Func0<Observable<ActivityFeedDownloadedEvent>>() {
            @Override
            public Observable<ActivityFeedDownloadedEvent> call() {
                return Observable.from(channels.values())
                    .map(getServiceObservable(context, session))
                    .filter(filterNullObject())
                    .map(syncronizeData())
                    .map(createEvent());
            }
        });
    }

    private Func1<Channel, Observable<List<ActivityFeedItem>>> getServiceObservable(
        final Context context, final TwitterSession twitterSession) {
        return new Func1<Channel, Observable<List<ActivityFeedItem>>>() {
            @Override
            public Observable<List<ActivityFeedItem>> call(Channel channel) {
                Observable<List<ActivityFeedItem>> observe = null;
                switch (channel.channelType()) {
                    case Twitter:
                        if(twitterSession != null) {
                            observe = getTwitterActivity(context, channel, twitterSession);
                        }else{
                            observe = Observable.just(ActivityFeedItem
                                .createEmpty(channel.channelType()))
                                .toList();
                        }
                        break;
                    case Youtube:
                        break;
                    case Custom:
                        break;
                    case Phone:
                        break;
                    case SMS:
                        break;
                    case Email:
                        break;
                    case Web:
                        break;
                    case URL:
                        break;
                    case Facebook:
                        break;
                    case Meerkat:
                        break;
                    case Snapchat:
                        break;
                    case Spotify:
                        break;
                    case Reddit:
                        break;
                    case Linkedin:
                        break;
                    case FBMessenger:
                        break;
                    case Hangouts:
                        break;
                    case Whatsapp:
                        break;
                    case Yo:
                        break;
                    case Googleplus:
                        break;
                    case Github:
                        break;
                    case Address:
                        break;
                    case Slack:
                        break;
                    case Instagram:
                        break;
                    case Tumblr:
                        break;
                    case Ello:
                        break;
                    case Venmo:
                        break;
                    case Periscope:
                        break;
                    case Medium:
                        break;
                    case Soundcloud:
                        break;
                    case Skype:
                        break;
                }
                return observe;
            }
        };
    }

    private Func1<List<ActivityFeedItem>, ActivityFeedDownloadedEvent> createEvent() {
        return new Func1<List<ActivityFeedItem>, ActivityFeedDownloadedEvent>() {
            @Override
            public ActivityFeedDownloadedEvent call(List<ActivityFeedItem> activityFeedItems) {
                return new ActivityFeedDownloadedEvent(activityFeedItems);
            }
        };
    }

    private Func1<Observable<List<ActivityFeedItem>>, List<ActivityFeedItem>> syncronizeData() {
        return new Func1<Observable<List<ActivityFeedItem>>, List<ActivityFeedItem>>() {
            @Override
            public List<ActivityFeedItem> call(Observable<List<ActivityFeedItem>> observer) {
                return observer.toBlocking().single();
            }
        };
    }

    private Observable<List<ActivityFeedItem>> getTwitterActivity(Context context,
        final Channel channel, TwitterSession session) {
        if (_sinceId == 0 || _maxId == 0) {
            return TwitterRestClient.newInstance(session)
                .getUserService()
                .getUserTimeline(Long.valueOf(channel.id()), ITEM_COUNT)
                .map(timelineToFeedItem(context, channel));
        } else {
            return TwitterRestClient.newInstance(session)
                .getUserService()
                .getUserTimeline(Long.valueOf(channel.id()), ITEM_COUNT, _sinceId, _maxId)
                .map(timelineToFeedItem(context, channel));
        }
    }

    /**
     * https://dev.twitter.com/rest/public/timelines.
     *
     * @param channel twitter channel data
     * @return activity feed items
     */
    private Func1<List<Tweet>, List<ActivityFeedItem>> timelineToFeedItem(
        final Context context, final Channel channel) {
        return new Func1<List<Tweet>, List<ActivityFeedItem>>() {
            @Override
            public List<ActivityFeedItem> call(List<Tweet> tweets) {
                ChannelType channelType = channel.channelType();
                String label = channel.actionAddress();
                List<ActivityFeedItem> feedItems = new ArrayList<>(tweets.size());
                for (Tweet tweet : tweets) {
                    Long idVal = tweet.id;
                    _sinceId = idVal.compareTo(_sinceId) > 0 ? idVal : _sinceId;
                    _maxId = idVal.compareTo(_maxId) < 0 ? (idVal - 1) : _maxId;
                    Date date = new Date();
                    try {
                        date = _dateFormat.parse(tweet.createdAt);
                    } catch (ParseException e) {
                        Timber.e("Parse Exception: %1$s", Log.getStackTraceString(e));
                    }
                    String subtext = context.getString(R.string.posted_on, channelType);
                    feedItems.add(ActivityFeedItem.create(label, subtext, channelType,
                        getTwitterLink(tweet), date));
                }
                return feedItems;
            }
        };
    }

    private String getTwitterLink(Tweet tweet) {
        return new StringBuilder("twitter.com/")
            .append(tweet.user.id)
            .append("/status/")
            .append(tweet.id).toString();
    }
}
