package org.limelink.limelink_aos_sdk

import android.net.Uri

object LinkParser {
    fun parseQueryParams(url: String): Map<String, String> {
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
}