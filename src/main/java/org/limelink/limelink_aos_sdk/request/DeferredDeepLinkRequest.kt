package org.limelink.limelink_aos_sdk.request

import com.google.gson.annotations.SerializedName

/**
 * Request model for creating or updating a deferred deep link
 * @param projectId Project ID
 * @param name Deferred deep link name (max 100 characters)
 * @param token Unique token (optional, max 100 characters, must be unique)
 * @param parameters Deep link parameters (JSON format)
 * @param iosAppStoreUrl iOS App Store URL (optional, max 500 characters)
 * @param androidPlayStoreUrl Android Play Store URL (optional, max 500 characters)
 * @param fallbackUrl Fallback URL when app is not installed (optional, max 500 characters)
 */
data class DeferredDeepLinkRequest(
    @SerializedName("project_id")
    val projectId: String,
    val name: String,
    val token: String? = null,
    val parameters: Map<String, Any>? = null,
    @SerializedName("ios_app_store_url")
    val iosAppStoreUrl: String? = null,
    @SerializedName("android_play_store_url")
    val androidPlayStoreUrl: String? = null,
    @SerializedName("fallback_url")
    val fallbackUrl: String? = null
)

