package com.shareyourproxy.api.service;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Twitter OAuthed Service request {https://dev.twitter .com/rest/reference/get/statuses/user_timeline}.
 */
public interface TwitterUserService {

    @GET("/1.1/statuses/user_timeline.json?" +
        "trim_user=true&include_rts=false&contributor_details=false")
    Observable<List<Tweet>> getUserTimeline(
        @Query("user_id") Long userId,
        @Query("count") Long count,
        @Query("since_id") Long sinceId,
        @Query("max_id") Long maxId);

    @GET("/1.1/statuses/user_timeline.json?" +
        "trim_user=true&include_rts=false&contributor_details=false")
    Observable<List<Tweet>> getUserTimeline(
        @Query("user_id") Long userId,
        @Query("count") Long count);
}
