package org.limelink.limelink_aos_sdk.service.limelink

import org.limelink.limelink_aos_sdk.request.LimeLinkRequest
import org.limelink.limelink_aos_sdk.response.UniversalLinkResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/api/v1/stats/event")
    suspend fun sendLimeLink(@Body data: LimeLinkRequest)
    
    /**
     * Universal Link API endpoint
     * @param suffix Dynamic link suffix
     * @return UniversalLinkResponse URL information for redirection
     */
    @GET("/universal-link/app/dynamic_link/{suffix}")
    suspend fun getUniversalLink(@Path("suffix") suffix: String): UniversalLinkResponse
}