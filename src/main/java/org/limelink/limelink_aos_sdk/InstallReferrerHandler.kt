package org.limelink.limelink_aos_sdk

import android.content.Context
import android.os.RemoteException
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.limelink.limelink_aos_sdk.response.LimeLinkUrl
import org.limelink.limelink_aos_sdk.response.ReferrerInfo
import java.net.URLDecoder

internal object InstallReferrerHandler {

    private const val TAG = "InstallReferrerHandler"
    private const val LIMELINK_HOST = "limelink.org"

    fun getInstallReferrer(context: Context, callback: (ReferrerInfo?) -> Unit) {
        val referrerClient = InstallReferrerClient.newBuilder(context).build()

        try {
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            try {
                                val response = referrerClient.installReferrer
                                val referrerUrl = response.installReferrer
                                val clickTimestamp = response.referrerClickTimestampSeconds
                                val installTimestamp = response.installBeginTimestampSeconds
                                val limeLinkDetail = extractLimeLinkUrlDetail(referrerUrl)

                                val referrerInfo = ReferrerInfo(
                                    referrerUrl = referrerUrl,
                                    clickTimestamp = clickTimestamp,
                                    installTimestamp = installTimestamp,
                                    limeLinkUrl = limeLinkDetail?.fullUrl,
                                    limeLinkDetail = limeLinkDetail
                                )

                                callback(referrerInfo)
                            } catch (e: RemoteException) {
                                Log.e(TAG, "Error getting install referrer", e)
                                callback(null)
                            } finally {
                                referrerClient.endConnection()
                            }
                        }
                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            Log.w(TAG, "Install Referrer API not supported")
                            callback(null)
                        }
                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            Log.w(TAG, "Install Referrer service unavailable")
                            callback(null)
                        }
                        else -> {
                            Log.w(TAG, "Unknown response code: $responseCode")
                            callback(null)
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    Log.w(TAG, "Install Referrer service disconnected")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Install Referrer service", e)
            callback(null)
        }
    }

    internal fun extractLimeLinkUrl(referrerString: String?): String? {
        return extractLimeLinkUrlDetail(referrerString)?.fullUrl
    }

    internal fun extractLimeLinkUrlDetail(referrerString: String?): LimeLinkUrl? {
        if (referrerString.isNullOrBlank()) return null

        return try {
            val matchedUrl = findLimeLinkUrl(referrerString) ?: return null
            buildLimeLinkUrl(referrerString, matchedUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting LimeLink URL from referrer", e)
            null
        }
    }

    private fun findLimeLinkUrl(referrerString: String): String? {
        // Strategy 1: Split raw referrer by & first, then decode each value individually.
        // This preserves query strings because encoded %26 stays intact during split.
        for (param in referrerString.split("&")) {
            val candidates = if ("=" in param) listOf(param, param.substringAfter("=")) else listOf(param)
            for (candidate in candidates) {
                if (candidate.isBlank()) continue
                try {
                    val decoded = URLDecoder.decode(candidate, "UTF-8")
                    val httpUrl = decoded.toHttpUrlOrNull()
                    if (httpUrl != null && isLimeLinkHost(httpUrl.host)) return decoded
                } catch (_: Exception) { /* skip invalid candidates */ }
            }
        }
        // Strategy 2: Full decode + regex fallback for non-standard formats
        val decoded = URLDecoder.decode(referrerString, "UTF-8")
        val urlPattern = Regex("""https?://[^\s&]+""")
        return urlPattern.findAll(decoded)
            .mapNotNull { match ->
                val candidate = match.value
                val httpUrl = candidate.toHttpUrlOrNull()
                if (httpUrl != null && isLimeLinkHost(httpUrl.host)) candidate else null
            }
            .firstOrNull()
    }

    private fun buildLimeLinkUrl(referrer: String, matchedUrl: String): LimeLinkUrl? {
        val httpUrl = matchedUrl.toHttpUrlOrNull() ?: return null
        return LimeLinkUrl(
            referrer = referrer,
            url = httpUrl.newBuilder().query(null).build().toString(),
            fullUrl = matchedUrl,
            queryString = httpUrl.query,
            queryParams = httpUrl.queryParameterNames.associateWith { httpUrl.queryParameter(it) ?: "" }
        )
    }

    private fun isLimeLinkHost(host: String): Boolean {
        return host == LIMELINK_HOST || host.endsWith(".$LIMELINK_HOST")
    }
}
