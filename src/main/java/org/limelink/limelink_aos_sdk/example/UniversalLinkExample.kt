package org.limelink.limelink_aos_sdk.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.limelink.limelink_aos_sdk.LimeLinkSDK

/**
 * Universal Link usage example class
 * Follow this pattern in your app's MainActivity.
 */
class UniversalLinkExample : Activity() {
    
    private val TAG = "UniversalLinkExample"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle Universal Link in onCreate
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        
        // Handle Universal Link in onNewIntent (when app is already running)
        intent?.let { handleIntent(it) }
    }
    
    /**
     * Processes intent to check if it's a Universal Link and handles it.
     */
    private fun handleIntent(intent: Intent) {
        // Check if it's a Universal Link
        if (LimeLinkSDK.isUniversalLink(intent)) {
            Log.d(TAG, "Universal Link detected")
            
            // Handle Universal Link
            LimeLinkSDK.handleUniversalLink(this, intent) { result ->
                if (result != null) {
                    Log.d(TAG, "Universal Link handled successfully: $result")
                    // Additional processing on success (e.g., notify user)
                } else {
                    Log.e(TAG, "Universal Link handling failed")
                    // Handle failure (e.g., show error page)
                }
            }
        } else {
            Log.d(TAG, "Regular intent.")
            // Handle regular app logic
        }
    }
    
    /**
     * Example of using existing URL handler
     */
    private fun handleCustomScheme(intent: Intent) {
        // Extract original URL from scheme
        val originalUrl = LimeLinkSDK.getSchemeFromIntent(intent)
        originalUrl?.let {
            Log.d(TAG, "Original URL: $it")
            
            // Parse query parameters
            val queryParams = LimeLinkSDK.parseQueryParams(intent)
            Log.d(TAG, "Query params: $queryParams")
            
            // Parse path parameters
            val pathParams = LimeLinkSDK.parsePathParams(intent)
            Log.d(TAG, "Path params: mainPath=${pathParams.mainPath}, subPath=${pathParams.subPath}")
        }
    }
} 