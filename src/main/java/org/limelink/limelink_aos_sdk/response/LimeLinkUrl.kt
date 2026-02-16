package org.limelink.limelink_aos_sdk.response

data class LimeLinkUrl(
    val referrer: String,
    val url: String,
    val fullUrl: String,
    val queryString: String?,
    val queryParams: Map<String, String>
)
