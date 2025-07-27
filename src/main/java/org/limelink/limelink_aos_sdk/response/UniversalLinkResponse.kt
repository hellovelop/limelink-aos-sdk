package org.limelink.limelink_aos_sdk.response

import com.google.gson.annotations.SerializedName

/**
 * Data class for handling Universal Link API responses
 * @param requestUri URL to redirect to
 */
data class UniversalLinkResponse(
    @SerializedName("request_uri")
    val requestUri: String
) 