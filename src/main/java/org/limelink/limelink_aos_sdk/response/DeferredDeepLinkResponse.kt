package org.limelink.limelink_aos_sdk.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for deferred deep link
 * @param id Deferred deep link ID
 * @param projectId Project ID
 * @param name Deferred deep link name
 * @param token Unique token
 * @param parameters Deep link parameters
 * @param iosAppStoreUrl iOS App Store URL
 * @param androidPlayStoreUrl Android Play Store URL
 * @param fallbackUrl Fallback URL when app is not installed
 * @param createdAt Creation timestamp
 * @param updatedAt Update timestamp (optional)
 */
data class DeferredDeepLinkResponse(
    val id: String,
    @SerializedName("project_id")
    val projectId: String,
    val name: String,
    val token: String?,
    val parameters: Map<String, Any>? = null,
    @SerializedName("ios_app_store_url")
    val iosAppStoreUrl: String? = null,
    @SerializedName("android_play_store_url")
    val androidPlayStoreUrl: String? = null,
    @SerializedName("fallback_url")
    val fallbackUrl: String? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * Response model for paginated deferred deep link list
 * @param items List of deferred deep links
 * @param lastEvaluatedKey Last evaluated key for pagination (optional)
 */
data class DeferredDeepLinksResponse(
    val items: List<DeferredDeepLinkResponse>,
    @SerializedName("last_evaluated_key")
    val lastEvaluatedKey: LastEvaluatedKey? = null
)

/**
 * Last evaluated key for pagination
 * @param id ID
 * @param createdAt Creation timestamp
 */
data class LastEvaluatedKey(
    val id: String,
    @SerializedName("created_at")
    val createdAt: String
)

