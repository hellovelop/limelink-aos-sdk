package org.limelink.limelink_aos_sdk.service.limelink

import org.limelink.limelink_aos_sdk.request.LimeLinkRequest
import org.limelink.limelink_aos_sdk.request.DeferredDeepLinkRequest
import org.limelink.limelink_aos_sdk.request.DeferredDeepLinksRequest
import org.limelink.limelink_aos_sdk.response.UniversalLinkResponse
import org.limelink.limelink_aos_sdk.response.DeeplinkResponse
import org.limelink.limelink_aos_sdk.response.DeferredDeepLinkResponse
import org.limelink.limelink_aos_sdk.response.DeferredDeepLinksResponse
import org.limelink.limelink_aos_sdk.response.GetDeferredDeepLinkByTokenResponse
import org.limelink.limelink_aos_sdk.response.CheckTokenResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
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
    @GET("/api/v1/dynamic_link/{linkSuffix}")
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
     * Check if deferred deep link token exists
     * @param token Token to check
     * @return CheckTokenResponse Whether the token exists
     */
    @GET("/api/v1/deferred-deep-link/check-token")
    suspend fun checkDeferredDeepLinkToken(@Query("token") token: String): CheckTokenResponse
    
    /**
     * Create a new deferred deep link
     * @param request DeferredDeepLinkRequest Creation request
     * @return DeferredDeepLinkResponse Created deferred deep link
     */
    @POST("/api/v1/deferred-deep-link")
    suspend fun createDeferredDeepLink(@Body request: DeferredDeepLinkRequest): DeferredDeepLinkResponse
    
    /**
     * Get paginated list of deferred deep links
     * @param projectId Project ID
     * @param request DeferredDeepLinksRequest Pagination and filter parameters
     * @return DeferredDeepLinksResponse Paginated list of deferred deep links
     */
    @GET("/api/v1/deferred-deep-link")
    suspend fun getDeferredDeepLinks(
        @Query("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("start_id") startId: String? = null,
        @Query("start_created_at") startCreatedAt: String? = null,
        @Query("keyword") keyword: String? = null
    ): DeferredDeepLinksResponse
    
    /**
     * Get deferred deep link detail by ID
     * @param deferredDeepLinkId Deferred deep link ID
     * @return DeferredDeepLinkResponse Deferred deep link details
     */
    @GET("/api/v1/deferred-deep-link/detail/{deferred_deep_link_id}")
    suspend fun getDeferredDeepLinkById(@Path("deferred_deep_link_id") deferredDeepLinkId: String): DeferredDeepLinkResponse
    
    /**
     * Update deferred deep link
     * @param deferredDeepLinkId Deferred deep link ID
     * @param request DeferredDeepLinkRequest Update request
     * @return DeferredDeepLinkResponse Updated deferred deep link
     */
    @PATCH("/api/v1/deferred-deep-link/{deferred_deep_link_id}")
    suspend fun updateDeferredDeepLink(
        @Path("deferred_deep_link_id") deferredDeepLinkId: String,
        @Body request: DeferredDeepLinkRequest
    ): DeferredDeepLinkResponse
    
    /**
     * Delete deferred deep link
     * @param deferredDeepLinkId Deferred deep link ID
     */
    @DELETE("/api/v1/deferred-deep-link/{deferred_deep_link_id}")
    suspend fun deleteDeferredDeepLink(@Path("deferred_deep_link_id") deferredDeepLinkId: String)
    
    /**
     * Get deferred deep link by token
     * Used when app is launched for the first time after installation
     * @param token Token
     * @return GetDeferredDeepLinkByTokenResponse Deferred deep link information
     */
    @GET("/api/v1/deferred-deep-link/token/{token}")
    suspend fun getDeferredDeepLinkByToken(@Path("token") token: String): GetDeferredDeepLinkByTokenResponse
}