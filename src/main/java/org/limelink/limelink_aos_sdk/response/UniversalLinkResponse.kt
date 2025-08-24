package org.limelink.limelink_aos_sdk.response

import com.google.gson.annotations.SerializedName

/**
 * Data class for handling Universal Link API responses
 * @param uri URL to redirect to
 */
data class UniversalLinkResponse(
    @SerializedName("uri")
    val uri: String
)

/**
 * Data class for handling Deeplink API responses
 * @param deeplinkUrl URL to redirect to
 */
data class DeeplinkResponse(
    @SerializedName("deeplinkUrl")
    val deeplinkUrl: String
) 