package com.shareyourproxy.api.rx;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;

import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;

/**
 * Created by Evan on 11/11/15.
 */
public class RxGoogleAnalytics {
    private static RxGoogleAnalytics DEFAULT_INSTANCE;
    private final GoogleAnalytics analytics;
    private final Tracker tracker;

    /**
     * Private constructor.
     */
    private RxGoogleAnalytics(Context context) {
        analytics = GoogleAnalytics.getInstance(context);
        tracker = analytics.newTracker(BuildConfig.GA_TRACKER_ID);
    }

    public static RxGoogleAnalytics getInstance(Context context) {
        if (DEFAULT_INSTANCE == null) {
            DEFAULT_INSTANCE = new RxGoogleAnalytics(context);
        }
        return DEFAULT_INSTANCE;
    }

    public GoogleAnalytics getAnalytics() {
        return analytics;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public Subscription userAdded(final User newUser) {
        return rx.Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Add User")
                            .setAction("Google Plus")
                            .setLabel(newUser.fullName())
                            .build()
                    );
                    singleSubscriber.onSuccess(true);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).compose(RxHelper.<Boolean>subThreadObserveMainSingle()).subscribe();
    }

    public Subscription channelAdded(final ChannelType channelType) {
        return rx.Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Channel Event")
                        .setAction("Add Channel")
                        .setLabel(channelType.getLabel())
                        .build());
                    singleSubscriber.onSuccess(true);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).compose(RxHelper.<Boolean>subThreadObserveMainSingle()).subscribe();
    }

    public Subscription channelEdited(final ChannelType oldChannelType) {
        return rx.Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Channel Event")
                        .setAction("Edit Channel")
                        .setLabel(oldChannelType.getLabel())
                        .build());
                    singleSubscriber.onSuccess(true);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).compose(RxHelper.<Boolean>subThreadObserveMainSingle()).subscribe();
    }

    public Subscription userProfileViewed(final User user) {
        return rx.Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("User Event")
                        .setAction("LoggedInUser Profile View")
                        .setLabel(user.fullName())
                        .build());
                    singleSubscriber.onSuccess(true);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).compose(RxHelper.<Boolean>subThreadObserveMainSingle()).subscribe();
    }

    public Subscription contactProfileViewed(final User user) {
        return rx.Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("User Event")
                        .setAction("Contact Profile View")
                        .setLabel(user.fullName())
                        .build());
                    singleSubscriber.onSuccess(true);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).compose(RxHelper.<Boolean>subThreadObserveMainSingle()).subscribe();
    }

    public Subscription userContactAdded(final User user) {
        return rx.Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("User Event")
                        .setAction("User Contact Added")
                        .setLabel(user.fullName())
                        .build());
                    singleSubscriber.onSuccess(true);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).compose(RxHelper.<Boolean>subThreadObserveMainSingle()).subscribe();
    }

    public Subscription userContactRemoved(final User user) {
        return rx.Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("User Event")
                        .setAction("User Contact Removed")
                        .setLabel(user.fullName())
                        .build());
                    singleSubscriber.onSuccess(true);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).compose(RxHelper.<Boolean>subThreadObserveMainSingle()).subscribe();
    }

    public Subscription shareLinkGenerated(final Group group) {
        return rx.Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Share Link")
                        .setAction("Link Generated")
                        .setLabel(group.label())
                        .build());
                    singleSubscriber.onSuccess(true);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).compose(RxHelper.<Boolean>subThreadObserveMainSingle()).subscribe();
    }

    public Subscription contactGroupButtonHit() {
        return rx.Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("User Event")
                        .setAction("Group Contact Button Hit")
                        .setValue(1)
                        .build());
                    singleSubscriber.onSuccess(true);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).compose(RxHelper.<Boolean>subThreadObserveMainSingle()).subscribe();
    }
}
