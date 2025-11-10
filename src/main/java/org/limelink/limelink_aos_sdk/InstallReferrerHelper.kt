package org.limelink.limelink_aos_sdk

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Helper class for retrieving install referrer information using Play Install Referrer API
 * Used to extract tokens from install referrer for deferred deep links
 */
object InstallReferrerHelper {
    
    private const val TAG = "InstallReferrerHelper"
    
    /**
     * Gets install referrer string from Play Install Referrer API
     * @param context Android context
     * @return Install referrer string or null if not available
     */
    suspend fun getInstallReferrer(context: Context): String? = suspendCancellableCoroutine { continuation ->
        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            val response: ReferrerDetails = referrerClient.installReferrer
                            val referrerUrl = response.installReferrer
                            val clickTimestamp = response.referrerClickTimestampSeconds
                            val installTimestamp = response.installBeginTimestampSeconds
                            
                            Log.d(TAG, "Install referrer retrieved: $referrerUrl")
                            Log.d(TAG, "Click timestamp: $clickTimestamp, Install timestamp: $installTimestamp")
                            
                            referrerClient.endConnection()
                            continuation.resume(referrerUrl)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting install referrer", e)
                            referrerClient.endConnection()
                            continuation.resume(null)
                        }
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        Log.w(TAG, "Install referrer feature not supported")
                        referrerClient.endConnection()
                        continuation.resume(null)
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        Log.w(TAG, "Install referrer service unavailable")
                        referrerClient.endConnection()
                        continuation.resume(null)
                    }
                    else -> {
                        Log.w(TAG, "Install referrer setup failed with code: $responseCode")
                        referrerClient.endConnection()
                        continuation.resume(null)
                    }
                }
            }
            
            override fun onInstallReferrerServiceDisconnected() {
                Log.w(TAG, "Install referrer service disconnected")
                continuation.resume(null)
            }
        })
        
        continuation.invokeOnCancellation {
            try {
                referrerClient.endConnection()
            } catch (e: Exception) {
                Log.e(TAG, "Error ending connection", e)
            }
        }
    }
    
    /**
     * Extracts token from install referrer URL
     * Supports multiple formats:
     * - Query parameter: ?token=xxx
     * - Query parameter: ?utm_source=xxx&token=yyy
     * - Direct token in referrer string
     * 
     * @param referrerUrl Install referrer URL string
     * @param tokenKey Key name for token parameter (default: "token")
     * @return Extracted token or null if not found
     */
    fun extractTokenFromReferrer(referrerUrl: String?, tokenKey: String = "token"): String? {
        if (referrerUrl.isNullOrBlank()) {
            return null
        }
        
        return try {
            // Try to parse as URI first
            val uri = Uri.parse(referrerUrl)
            val token = uri.getQueryParameter(tokenKey)
            if (!token.isNullOrBlank()) {
                Log.d(TAG, "Token extracted from referrer: $token")
                return token
            }
            
            // If not found in query parameters, try to find it in the referrer string directly
            // Format: token=xxx or token:xxx
            val patterns = listOf(
                "$tokenKey=([^&\\s]+)",
                "$tokenKey:([^&\\s]+)"
            )
            
            for (pattern in patterns) {
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                val match = regex.find(referrerUrl)
                if (match != null && match.groupValues.size > 1) {
                    val extractedToken = match.groupValues[1]
                    Log.d(TAG, "Token extracted from referrer using pattern: $extractedToken")
                    return extractedToken
                }
            }
            
            Log.d(TAG, "No token found in referrer: $referrerUrl")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting token from referrer", e)
            null
        }
    }
}

