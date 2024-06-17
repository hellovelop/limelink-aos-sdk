package org.limelink.limelink_aos_sdk.request

data class LimeLinkRequest(
    val private_key: String,
    val suffix: String,
    val handle: String? = null,
    val event_type: String,
    val operating_system :String = "android",
)
