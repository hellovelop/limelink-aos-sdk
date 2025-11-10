package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.limelink.limelink_aos_sdk.response.GetDeferredDeepLinkByTokenResponse
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
     * Gets deferred deep link by token.
     * Used when app is launched for the first time after installation.
     * @param token Token
     * @param callback Callback to receive result (optional)
     */
    fun getDeferredDeepLinkByToken(
        token: String,
        callback: ((Result<GetDeferredDeepLinkByTokenResponse>) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getDeferredDeepLinkByToken(token)
                }
                Log.d(TAG, "Deferred deep link retrieved by token: $token")
                callback?.invoke(Result.success(result))
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while getting deferred deep link by token", e)
                callback?.invoke(Result.failure(e))
            }
        }
    }
    
    /**
     * Gets deferred deep link automatically using Play Install Referrer API.
     * This method retrieves the install referrer, extracts the token, and fetches the deferred deep link.
     * Use this when the app is launched for the first time after installation.
     * 
     * @param context Android context
     * @param tokenKey Key name for token parameter in referrer (default: "token")
     * @param callback Callback to receive result (optional)
     */
    fun getDeferredDeepLinkFromInstallReferrer(
        context: Context,
        tokenKey: String = "token",
        callback: ((Result<GetDeferredDeepLinkByTokenResponse>) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Get install referrer
                val referrerUrl = withContext(Dispatchers.IO) {
                    InstallReferrerHelper.getInstallReferrer(context)
                }
                
                if (referrerUrl == null) {
                    Log.d(TAG, "No install referrer found")
                    callback?.invoke(Result.failure(Exception("No install referrer found")))
                    return@launch
                }
                
                // Extract token from referrer
                val token = InstallReferrerHelper.extractTokenFromReferrer(referrerUrl, tokenKey)
                
                if (token == null) {
                    Log.d(TAG, "No token found in install referrer: $referrerUrl")
                    callback?.invoke(Result.failure(Exception("No token found in install referrer")))
                    return@launch
                }
                
                Log.d(TAG, "Token extracted from install referrer: $token")
                
                // Get deferred deep link using the token
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getDeferredDeepLinkByToken(token)
                }
                
                Log.d(TAG, "Deferred deep link retrieved from install referrer")
                callback?.invoke(Result.success(result))
                
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while getting deferred deep link from install referrer", e)
                callback?.invoke(Result.failure(e))
            }
        }
    }
} 