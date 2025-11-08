package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.limelink.limelink_aos_sdk.request.DeferredDeepLinkRequest
import org.limelink.limelink_aos_sdk.request.DeferredDeepLinksRequest
import org.limelink.limelink_aos_sdk.response.CheckTokenResponse
import org.limelink.limelink_aos_sdk.response.DeferredDeepLinkResponse
import org.limelink.limelink_aos_sdk.response.DeferredDeepLinksResponse
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
     * Checks if deferred deep link token exists.
     * @param token Token to check
     * @param callback Callback to receive result (optional)
     */
    fun checkDeferredDeepLinkToken(
        token: String,
        callback: ((Result<CheckTokenResponse>) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.checkDeferredDeepLinkToken(token)
                }
                Log.d(TAG, "Token check completed: ${result.isExist}")
                callback?.invoke(Result.success(result))
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while checking token", e)
                callback?.invoke(Result.failure(e))
            }
        }
    }
    
    /**
     * Creates a new deferred deep link.
     * @param request DeferredDeepLinkRequest Creation request
     * @param callback Callback to receive result (optional)
     */
    fun createDeferredDeepLink(
        request: DeferredDeepLinkRequest,
        callback: ((Result<DeferredDeepLinkResponse>) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.createDeferredDeepLink(request)
                }
                Log.d(TAG, "Deferred deep link created: ${result.id}")
                callback?.invoke(Result.success(result))
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while creating deferred deep link", e)
                callback?.invoke(Result.failure(e))
            }
        }
    }
    
    /**
     * Gets paginated list of deferred deep links.
     * @param projectId Project ID
     * @param request DeferredDeepLinksRequest Pagination and filter parameters (optional)
     * @param callback Callback to receive result (optional)
     */
    fun getDeferredDeepLinks(
        projectId: String,
        request: DeferredDeepLinksRequest? = null,
        callback: ((Result<DeferredDeepLinksResponse>) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getDeferredDeepLinks(
                        projectId = projectId,
                        limit = request?.limit,
                        startId = request?.startId,
                        startCreatedAt = request?.startCreatedAt,
                        keyword = request?.keyword
                    )
                }
                Log.d(TAG, "Deferred deep links retrieved: ${result.items.size} items")
                callback?.invoke(Result.success(result))
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while getting deferred deep links", e)
                callback?.invoke(Result.failure(e))
            }
        }
    }
    
    /**
     * Gets deferred deep link detail by ID.
     * @param deferredDeepLinkId Deferred deep link ID
     * @param callback Callback to receive result (optional)
     */
    fun getDeferredDeepLinkById(
        deferredDeepLinkId: String,
        callback: ((Result<DeferredDeepLinkResponse>) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getDeferredDeepLinkById(deferredDeepLinkId)
                }
                Log.d(TAG, "Deferred deep link retrieved: ${result.id}")
                callback?.invoke(Result.success(result))
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while getting deferred deep link", e)
                callback?.invoke(Result.failure(e))
            }
        }
    }
    
    /**
     * Updates deferred deep link.
     * @param deferredDeepLinkId Deferred deep link ID
     * @param request DeferredDeepLinkRequest Update request
     * @param callback Callback to receive result (optional)
     */
    fun updateDeferredDeepLink(
        deferredDeepLinkId: String,
        request: DeferredDeepLinkRequest,
        callback: ((Result<DeferredDeepLinkResponse>) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.updateDeferredDeepLink(deferredDeepLinkId, request)
                }
                Log.d(TAG, "Deferred deep link updated: ${result.id}")
                callback?.invoke(Result.success(result))
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while updating deferred deep link", e)
                callback?.invoke(Result.failure(e))
            }
        }
    }
    
    /**
     * Deletes deferred deep link.
     * @param deferredDeepLinkId Deferred deep link ID
     * @param callback Callback to receive result (optional)
     */
    fun deleteDeferredDeepLink(
        deferredDeepLinkId: String,
        callback: ((Result<Unit>) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.deleteDeferredDeepLink(deferredDeepLinkId)
                }
                Log.d(TAG, "Deferred deep link deleted: $deferredDeepLinkId")
                callback?.invoke(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while deleting deferred deep link", e)
                callback?.invoke(Result.failure(e))
            }
        }
    }
    
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
} 