package org.limelink.limelink_aos_sdk.service.limelink

import org.limelink.limelink_aos_sdk.request.LimeLinkRequest
import org.limelink.limelink_aos_sdk.response.UniversalLinkResponse
import org.limelink.limelink_aos_sdk.response.DeeplinkResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/api/v1/stats/event")
    suspend fun sendLimeLink(@Body data: LimeLinkRequest)
    
    /**
     * Universal Link API endpoint (새로운 방식)
     * @param linkSuffix Dynamic link suffix
     * @return UniversalLinkResponse URL information for redirection
     */
    @GET("/api/v1/app/dynamic_link/{linkSuffix}")
    suspend fun getUniversalLinkNew(@Path("linkSuffix") linkSuffix: String): UniversalLinkResponse
    
    /**
     * Deeplink API endpoint
     * @param subdomain Subdomain parameter
     * @param path Path parameter
     * @param platform Platform parameter
     * @return DeeplinkResponse URL information for redirection
     */
    @GET("/link")
    suspend fun getDeeplink(
        @Query("subdomain") subdomain: String,
        @Query("path") path: String,
        @Query("platform") platform: String
    ): DeeplinkResponse
    
    /**
     * Universal Link API endpoint (기존 방식 - deprecated)
     * @param suffix Dynamic link suffix
     * @return UniversalLinkResponse URL information for redirection
     */
    @GET("/universal-link/app/dynamic_link/{suffix}")
    suspend fun getUniversalLink(@Path("suffix") suffix: String): UniversalLinkResponse
    
    /**
     * Get deferred deep link by suffix (for Install Referrer)
     * Used when app is launched for the first time after installation
     * Calls the same API as universal link: /api/v1/app/dynamic_link/{suffix}
     * @param suffix Suffix from Install Referrer code parameter
     * @return UniversalLinkResponse URI information for redirection
     */
    @GET("/api/v1/app/dynamic_link/{suffix}")
    suspend fun getDeferredDeepLinkBySuffix(@Path("suffix") suffix: String): UniversalLinkResponse
}