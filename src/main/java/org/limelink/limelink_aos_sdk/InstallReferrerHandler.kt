package org.limelink.limelink_aos_sdk

import android.content.Context
import android.os.RemoteException
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
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
                                val limeLinkUrl = extractLimeLinkUrl(referrerUrl)

                                val referrerInfo = ReferrerInfo(
                                    referrerUrl = referrerUrl,
                                    clickTimestamp = clickTimestamp,
                                    installTimestamp = installTimestamp,
                                    limeLinkUrl = limeLinkUrl
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
        if (referrerString.isNullOrBlank()) return null

        return try {
            val decoded = URLDecoder.decode(referrerString, "UTF-8")
            // Look for a URL containing limelink.org
            val urlPattern = Regex("""(https?://[^\s&]+$LIMELINK_HOST[^\s&]*)""")
            urlPattern.find(decoded)?.value
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting LimeLink URL from referrer", e)
            null
        }
    }
}
