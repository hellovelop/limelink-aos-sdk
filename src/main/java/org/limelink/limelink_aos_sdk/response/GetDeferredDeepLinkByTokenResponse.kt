package org.limelink.limelink_aos_sdk.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for getting deferred deep link by token
 * Used when app is launched for the first time after installation
 * @param parameters Deep link parameters
 * @param iosAppStoreUrl iOS App Store URL
 * @param androidPlayStoreUrl Android Play Store URL
 * @param fallbackUrl Fallback URL when app is not installed
 */
data class GetDeferredDeepLinkByTokenResponse(
    val parameters: Map<String, Any>? = null,
    @SerializedName("ios_app_store_url")
    val iosAppStoreUrl: String? = null,
    @SerializedName("android_play_store_url")
    val androidPlayStoreUrl: String? = null,
    @SerializedName("fallback_url")
    val fallbackUrl: String? = null
)

