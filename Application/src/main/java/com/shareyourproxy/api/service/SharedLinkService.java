package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.SharedLink;

import java.util.HashMap;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Add and remove {@link SharedLink}s.
 */
public interface SharedLinkService {

    @GET("/shared.json")
    Observable<HashMap<String, SharedLink>> getSharedLinks();

    /**
     * add a {@link SharedLink}
     *
     * @param sharedId shared link identifier
     */
    @PUT("/shared/{sharedId}.json")
    Observable<SharedLink> addSharedLink(@Path("sharedId") String sharedId, @Body SharedLink link);

    @DELETE("/shared/{sharedId}.json")
    Observable<SharedLink> deleteSharedLink(@Path("sharedId") String sharedId);
}
