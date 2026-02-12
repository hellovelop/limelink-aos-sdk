package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.limelink.limelink_aos_sdk.UrlHandler.parsePathParams
import org.limelink.limelink_aos_sdk.enums.EventType
import org.limelink.limelink_aos_sdk.request.LimeLinkRequest
import org.limelink.limelink_aos_sdk.response.PathParamResponse
import org.limelink.limelink_aos_sdk.service.RetrofitClient

object LinkStats {
    private const val PREFS_NAME = "link_first_launch_prefs"
    private const val KEY_FIRST_LAUNCH = "is_first_launch"

    fun isFirstLaunch(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
        return isFirstLaunch
    }
}

@Deprecated("Use LimeLinkSDK.init() for automatic tracking")
suspend fun saveLimeLinkStatus(context: Context, intent: Intent, privateKey: String) {
    internalSaveLimeLinkStatus(context, intent, privateKey)
}

internal suspend fun internalSaveLimeLinkStatus(context: Context, intent: Intent, apiKey: String) {
    val pathParamResponse: PathParamResponse = parsePathParams(intent)

    if (pathParamResponse.mainPath.isEmpty()) {
        return
    }

    val eventType = if (LinkStats.isFirstLaunch(context)) EventType.FIRST_RUN else EventType.RERUN
    val limeLinkRequest = createLimeLinkRequest(apiKey, pathParamResponse, eventType)

    sendLimeLinkAsync(limeLinkRequest)
}

private fun createLimeLinkRequest(
    privateKey: String,
    pathParamResponse: PathParamResponse,
    eventType: EventType
): LimeLinkRequest {
    return LimeLinkRequest(
        private_key = privateKey,
        suffix = pathParamResponse.mainPath,
        handle = pathParamResponse.subPath,
        event_type = eventType.value
    )
}

private suspend fun sendLimeLinkAsync(limeLinkRequest: LimeLinkRequest) {
    val apiService = RetrofitClient.apiService

    try {
        withContext(Dispatchers.IO) {
            apiService.sendLimeLink(limeLinkRequest)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
