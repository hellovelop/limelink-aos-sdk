package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
} 