package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.limelink.limelink_aos_sdk.response.UniversalLinkResponse
import org.limelink.limelink_aos_sdk.service.RetrofitClient

/**
 * Main class for LimeLink Android SDK
 * Provides Universal Link handling and other SDK functionality.
 */
object LimeLinkSDK {
    
    private const val TAG = "LimeLinkSDK"
    
    /**
     * Handles Universal Link.
     * Call this from onNewIntent() or onCreate() in your app.
     * 
     * @param context Android context
     * @param intent Universal Link intent
     * @param callback Callback to receive processing result with URI (optional)
     */
    fun handleUniversalLink(
        context: Context, 
        intent: Intent, 
        callback: ((String?) -> Unit)? = null
    ) {
        // Check if it's a Universal Link format
        if (!UniversalLinkHandler.isUniversalLink(intent)) {
            Log.d(TAG, "Not a Universal Link format.")
            callback?.invoke(null)
            return
        }
        
        Log.d(TAG, "Starting Universal Link processing")
        
        // Process in background
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    UniversalLinkHandler.handleUniversalLink(context, intent)
                }
                
                Log.d(TAG, "Universal Link processing completed: $result")
                callback?.invoke(result)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while processing Universal Link", e)
                callback?.invoke(null)
            }
        }
    }
    
    /**
     * Checks if the URL is in Universal Link format.
     * @param intent Intent to check
     * @return Whether it's in Universal Link format
     */
    fun isUniversalLink(intent: Intent): Boolean {
        return UniversalLinkHandler.isUniversalLink(intent)
    }
    
    /**
     * Existing URL handler functionality
     */
    
    /**
     * Extracts original URL from scheme.
     * @param intent Intent
     * @return Extracted URL or null
     */
    fun getSchemeFromIntent(intent: Intent): String? {
        return UrlHandler.getSchemeFromIntent(intent)
    }
    
    /**
     * Parses query parameters.
     * @param intent Intent
     * @return Parsed query parameters map
     */
    fun parseQueryParams(intent: Intent): Map<String, String> {
        return UrlHandler.parseQueryParams(intent)
    }
    
    /**
     * Parses path parameters.
     * @param intent Intent
     * @return Parsed path parameters response
     */
    fun parsePathParams(intent: Intent): org.limelink.limelink_aos_sdk.response.PathParamResponse {
        return UrlHandler.parsePathParams(intent)
    }
    
    /**
     * Deferred Deep Link functionality
     */
    
    /**
     * Handles deferred deep link from Install Referrer.
     * When Install Referrer API provides code={suffix} and full_request_url={full_request_url},
     * this method calls https://limelink.org/api/v1/app/dynamic_link/{suffix}?full_request_url={full_request_url}&event_type=setup
     * and returns the URI for redirection.
     * 
     * @param suffix Suffix from Install Referrer code parameter
     * @param fullRequestUrl Full request URL from Install Referrer (optional)
     * @param callback Callback to receive result with URI (optional)
     */
    fun handleDeferredDeepLink(
        suffix: String,
        fullRequestUrl: String? = null,
        callback: ((String?) -> Unit)? = null
    ) {
        Log.d(TAG, "Starting Deferred Deep Link processing with suffix: $suffix, fullRequestUrl: $fullRequestUrl")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getDeferredDeepLinkBySuffix(
                        suffix = suffix,
                        fullRequestUrl = fullRequestUrl,
                        eventType = "setup"
                    )
                }
                
                Log.d(TAG, "Deferred Deep Link processing completed: ${result.uri}")
                callback?.invoke(result.uri)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while processing Deferred Deep Link", e)
                callback?.invoke(null)
            }
        }
    }
    
} 