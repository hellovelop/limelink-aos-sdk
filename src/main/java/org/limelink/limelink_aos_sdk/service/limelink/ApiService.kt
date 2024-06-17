package org.limelink.limelink_aos_sdk.service.limelink

import org.limelink.limelink_aos_sdk.request.LimeLinkRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {


    @POST("/api/v1/stats/event")
    suspend fun sendLimeLink(@Body data: LimeLinkRequest)
}