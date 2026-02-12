package org.limelink.limelink_aos_sdk.response

data class LimeLinkError(
    val code: Int,
    val message: String,
    val exception: Throwable? = null
)
