package org.limelink.limelink_aos_sdk

import org.limelink.limelink_aos_sdk.response.LimeLinkError
import org.limelink.limelink_aos_sdk.response.LimeLinkResult

interface LimeLinkListener {
    fun onDeeplinkReceived(result: LimeLinkResult)
    fun onDeeplinkError(error: LimeLinkError) {}
}
