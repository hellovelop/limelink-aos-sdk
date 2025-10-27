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

    fun parseQueryParams(intent: Intent): Map<String, String> {
        // 먼저 original-url 파라미터에서 URL을 추출 시도
        var url = UrlHandler.getSchemeFromIntent(intent)
        
        // original-url이 없으면 Intent에서 직접 URL 추출 시도
        if (url.isNullOrEmpty()) {
            url = getUrlFromIntent(intent)
        }
        
        if (url.isNullOrEmpty()) {
            return emptyMap()
        }

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
        // 먼저 original-url 파라미터에서 URL을 추출 시도
        var url = UrlHandler.getSchemeFromIntent(intent)
        
        // original-url이 없으면 Intent에서 직접 URL 추출 시도
        if (url.isNullOrEmpty()) {
            url = getUrlFromIntent(intent)
        }
        
        if (url.isNullOrEmpty()) {
            return PathParamResponse(mainPath = "", subPath = "")
        }
        
        val uri = Uri.parse(url)
        val pathSegments = uri.pathSegments
        
        // pathSegments[0] = "link", pathSegments[1] = "toggle-reward" 등
        // mainPath는 pathSegments[1] (두 번째 요소), subPath는 pathSegments[2] (세 번째 요소, 있을 경우)
        val mainPath = pathSegments.getOrNull(1) ?: ""
        val subPath = pathSegments.getOrNull(2)
        
        return PathParamResponse(mainPath = mainPath, subPath = subPath)
    }
}