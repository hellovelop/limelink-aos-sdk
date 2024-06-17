package org.limelink.limelink_aos_sdk

import android.content.Intent
import android.net.Uri
import org.limelink.limelink_aos_sdk.response.PathParamResponse

object UrlHandler {
    private fun getUrlFromIntent(intent: Intent): String? {
        return if (Intent.ACTION_VIEW == intent.action) {
            intent.data?.toString()
        } else {
            null
        }
    }

    fun parseQueryParams(intent: Intent): Map<String, String> {
        val url = getUrlFromIntent(intent) ?: return emptyMap()

        val uri = Uri.parse(url)
        val queryParameterNames = uri.queryParameterNames
        val queryParams = mutableMapOf<String, String>()

        for (param in queryParameterNames) {
            uri.getQueryParameter(param)?.let {
                queryParams[param] = it
            }
        }
        return queryParams
    }

    fun parsePathParams(intent: Intent): PathParamResponse {
        val url = getUrlFromIntent(intent) ?: return PathParamResponse(mainPath = "", subPath = "")
        val uri = Uri.parse(url)
        val pathSegments = uri.pathSegments
        return PathParamResponse(mainPath = pathSegments[0], subPath = pathSegments[2])
    }
}