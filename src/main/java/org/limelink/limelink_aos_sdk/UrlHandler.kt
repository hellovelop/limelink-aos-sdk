package src.main.java.org.limelink.limelink_aos_sdk

import android.content.Intent
import android.net.Uri

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

    fun parsePathParams(intent: Intent): PathParams {
        val url = getUrlFromIntent(intent) ?: return PathParams()
        val uri = Uri.parse(url)
        val pathSegments = uri.pathSegments
        return PathParams(mainPath = pathSegments[0], subPath = pathSegments[2])
    }
}