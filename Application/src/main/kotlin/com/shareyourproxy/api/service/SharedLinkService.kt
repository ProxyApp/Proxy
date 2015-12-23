package com.shareyourproxy.api.service

import com.shareyourproxy.api.domain.model.SharedLink
import retrofit.http.*
import rx.Observable
import java.util.*

/**
 * Add and remove [SharedLink]s.
 */
interface SharedLinkService {

    @GET("/shared.json")
    fun sharedLinks(): Observable<HashMap<String, SharedLink>>

    /**
     * add a [SharedLink]
     * @param sharedId shared link identifier
     */
    @PUT("/shared/{sharedId}.json")
    fun addSharedLink(@Path("sharedId") sharedId: String, @Body link: SharedLink): Observable<SharedLink>

    @DELETE("/shared/{sharedId}.json")
    fun deleteSharedLink(@Path("sharedId") sharedId: String): Observable<SharedLink>
}
