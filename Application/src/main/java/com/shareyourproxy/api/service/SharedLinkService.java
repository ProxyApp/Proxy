package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.SharedLink;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Add and remove {@link SharedLink}s.
 */
public interface SharedLinkService {

    /**
     * add a {@link SharedLink}
     *
     * @param groupId shared link identifier
     */
    @PUT("/shared/{groupId}.json")
    Observable<SharedLink> addSharedLink(@Path("groupId") String groupId, @Body SharedLink link);

    @DELETE("/shared/{groupId}.json")
    Observable<SharedLink> deleteSharedLink(@Path("groupId") String groupId);
}
