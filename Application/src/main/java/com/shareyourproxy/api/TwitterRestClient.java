package com.shareyourproxy.api;

import com.shareyourproxy.api.service.TwitterUserService;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterApiClient;

/**
 * Custom twitter user timeline service.
 */
public class TwitterRestClient extends TwitterApiClient {
    public TwitterRestClient(Session session) {
        super(session);

    }

    public static TwitterRestClient newInstance(Session session){
        return new TwitterRestClient(session);
    }

    public TwitterUserService getUserService() {
        return getService(TwitterUserService.class);
    }
}
