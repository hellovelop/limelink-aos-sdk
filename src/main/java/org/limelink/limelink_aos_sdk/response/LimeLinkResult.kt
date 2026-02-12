package org.limelink.limelink_aos_sdk.response

import android.net.Uri

data class LimeLinkResult(
    val originalUrl: String?,
    val resolvedUri: Uri?,
    val queryParams: Map<String, String>,
    val pathParams: PathParamResponse,
    val isDeferred: Boolean = false,
    val referrerInfo: ReferrerInfo? = null
)
