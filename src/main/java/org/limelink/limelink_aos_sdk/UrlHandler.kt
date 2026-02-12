package org.limelink.limelink_aos_sdk

import android.content.Intent
import android.net.Uri
import org.limelink.limelink_aos_sdk.response.PathParamResponse

object UrlHandler {
    private const val MAIN_URL_KEY = "original-url"

    /*기본 url 추출 */
    private fun getUrlFromIntent(intent: Intent): String? {
        return if (Intent.ACTION_VIEW == intent.action) {
            intent.data?.toString()
        } else {
            null
        }
    }

    /*Schem에서 original url 추출*/
    fun getSchemeFromIntent(intent: Intent): String? {
        return if (Intent.ACTION_VIEW == intent.action) {
            intent.data?.getQueryParameter(MAIN_URL_KEY)
        } else {
            null
        }
    }

    private fun resolveUri(intent: Intent): Uri? {
        val url = getSchemeFromIntent(intent) ?: getUrlFromIntent(intent)
        if (url.isNullOrEmpty()) return null
        return Uri.parse(url)
    }

    fun parseQueryParams(intent: Intent): Map<String, String> {
        val uri = resolveUri(intent) ?: return emptyMap()
        return uri.queryParameterNames.associateWith { uri.getQueryParameter(it) ?: "" }
    }

    fun parsePathParams(intent: Intent): PathParamResponse {
        val uri = resolveUri(intent) ?: return PathParamResponse(mainPath = "", subPath = "")
        val pathSegments = uri.pathSegments

        // pathSegments[0] = "link", pathSegments[1] = "toggle-reward" 등
        // mainPath는 pathSegments[1] (두 번째 요소), subPath는 pathSegments[2] (세 번째 요소, 있을 경우)
        val mainPath = pathSegments.getOrNull(1) ?: ""
        val subPath = pathSegments.getOrNull(2)

        return PathParamResponse(mainPath = mainPath, subPath = subPath)
    }
}
