package org.limelink.limelink_aos_sdk.response

data class ReferrerInfo(
    val referrerUrl: String?,
    val clickTimestamp: Long,
    val installTimestamp: Long,
    val limeLinkUrl: LimeLinkUrl?
)
