package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.limelink.limelink_aos_sdk.service.RetrofitClient
import org.limelink.limelink_aos_sdk.response.UniversalLinkResponse
import retrofit2.HttpException

/**
 * Class responsible for handling Universal Links
 * Processes URLs in the format https://{suffix}.limelink.org by
 * calling the API https://limelink.org/universal-link/app/dynamic_link/{suffix} and
 * redirecting to the received request_uri.
 */
object UniversalLinkHandler {
    
    private const val TAG = "UniversalLinkHandler"
    private const val LIMELINK_HOST = "limelink.org"
    
    /**
     * Handles Universal Link and performs redirection.
     * @param context Android context
     * @param intent Universal Link intent
     * @return Whether processing was successful
     */
    suspend fun handleUniversalLink(context: Context, intent: Intent): Boolean {
        return try {
            // Extract suffix from URL
            val suffix = extractSuffixFromUrl(intent) ?: run {
                Log.e(TAG, "Cannot extract suffix.")
                return false
            }
            
            Log.d(TAG, "Extracted suffix: $suffix")
            
            // Call API to get request_uri
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getUniversalLink(suffix)
            }
            
            Log.d(TAG, "Received request_uri: ${response.requestUri}")
            
            // Perform redirection
            redirectToUri(context, response.requestUri)
            
            true
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> {
                    Log.e(TAG, "Suffix not found or request URI not found: ${e.message()}")
                }
                else -> {
                    Log.e(TAG, "HTTP error occurred: ${e.code()} - ${e.message()}")
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while processing Universal Link", e)
            false
        }
    }
    
    /**
     * Extracts suffix from URL.
     * @param intent Universal Link intent
     * @return Extracted suffix or null
     */
    private fun extractSuffixFromUrl(intent: Intent): String? {
        val uri = intent.data ?: return null
        
        // Check if it's in the format https://{suffix}.limelink.org
        if (uri.host?.endsWith(LIMELINK_HOST) == true) {
            val host = uri.host ?: return null
            val parts = host.split(".")
            
            // Check if it's in the format {suffix}.limelink.org
            if (parts.size >= 3 && parts.last() == "org" && parts[parts.size - 2] == "limelink") {
                return parts[0] // First part is the suffix
            }
        }
        
        return null
    }
    
    /**
     * Performs redirection to the received URI.
     * @param context Android context
     * @param uri URI to redirect to
     */
    private fun redirectToUri(context: Context, uri: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            context.startActivity(intent)
            Log.d(TAG, "Redirection successful: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "Redirection failed: $uri", e)
        }
    }
    
    /**
     * Checks if the URL is in Universal Link format.
     * @param intent Intent to check
     * @return Whether it's in Universal Link format
     */
    fun isUniversalLink(intent: Intent): Boolean {
        val uri = intent.data ?: return false
        return uri.host?.endsWith(LIMELINK_HOST) == true && 
               uri.scheme == "https"
    }
} 