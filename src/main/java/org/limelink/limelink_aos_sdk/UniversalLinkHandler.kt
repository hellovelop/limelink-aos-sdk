package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.limelink.limelink_aos_sdk.service.RetrofitClient
import retrofit2.HttpException

/**
 * Class responsible for handling Universal Links
 * Processes URLs in the format https://{suffix}.limelink.org/link/{link_suffix} by
 * calling the API https://www.limelink.org/api/v1/app/dynamic_link/{link_suffix} and
 * redirecting to the received uri.
 * Also supports legacy deeplink format for backward compatibility.
 */
object UniversalLinkHandler {
    
    private const val TAG = "UniversalLinkHandler"
    private const val LIMELINK_HOST = "limelink.org"
    private const val DEEP_LIMELINK_HOST = "deep.limelink.org"
    
    /**
     * Handles Universal Link and returns the URI for redirection.
     * @param context Android context
     * @param intent Universal Link intent
     * @param uri URI from intent (pre-extracted for safety)
     * @return URI to redirect to, or null if processing failed
     */
    suspend fun handleUniversalLink(_context: Context, intent: Intent, uri: Uri): String? {
        return try {
            val host = uri.host ?: run {
                Log.e(TAG, "No host in URI")
                return null
            }
            
            // {suffix}.limelink.org/link/{link_suffix} 패턴 처리
            if (host.endsWith(".$LIMELINK_HOST")) {
                handleSubdomainPattern(uri)
            } else {
                // 기존 deeplink 처리 로직
                handleLegacyDeeplink(uri)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while processing Universal Link", e)
            null
        }
    }
    
    /**
     * 서브도메인 패턴 처리 ({suffix}.limelink.org/link/{link_suffix})
     */
    private suspend fun handleSubdomainPattern(uri: Uri): String? {
        val host = uri.host ?: return null
        
        // {suffix}.limelink.org에서 suffix 추출
        val suffix = host.replace(".$LIMELINK_HOST", "")
        
        // URL 경로에서 link_suffix 추출 (/link/{link_suffix} 패턴)
        val path = uri.path ?: return null
        if (!path.startsWith("/link/")) {
            Log.e(TAG, "서브도메인 패턴이 일치하지 않습니다: $path")
            return null
        }
        val linkSuffix = path.removePrefix("/link/")
        Log.d(TAG, "Extracted suffix: $suffix, linkSuffix: $linkSuffix")
        
        // 원본 URL의 full URL 추출 (쿼리스트링 포함)
        val fullRequestUrl = uri.toString()
        
        // 원본 URL의 쿼리 파라미터 추출
        val queryParams = uri.queryParameterNames.associateWith { uri.getQueryParameter(it) ?: "" }

        // Universal Link API 호출 (fullRequestUrl과 쿼리 파라미터 포함)
        return fetchUniversalLink(linkSuffix, fullRequestUrl, queryParams)
    }
    
    /**
     * 기존 deeplink 처리 로직
     */
    private suspend fun handleLegacyDeeplink(uri: Uri): String? {
        val host = uri.host ?: return null
        
        if (host != DEEP_LIMELINK_HOST) {
            Log.d(TAG, "Not a deeplink host: $host")
            return null
        }
        
        val path = uri.path ?: return null
        val subdomain = host.split(".").firstOrNull() ?: ""
        val platform = "android"
        
        Log.d(TAG, "Processing legacy deeplink - subdomain: $subdomain, path: $path")
        
        return try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getDeeplink(subdomain, path, platform)
            }
            
            Log.d(TAG, "Received deeplink URL: ${response.deeplinkUrl}")
            response.deeplinkUrl
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching deeplink", e)
            null
        }
    }
    
    /**
     * Universal Link API 호출
     */
    private suspend fun fetchUniversalLink(
        linkSuffix: String,
        fullRequestUrl: String? = null,
        queryParams: Map<String, String> = emptyMap()
    ): String? {
        try {
            Log.d(TAG, "Calling Universal Link API with linkSuffix: $linkSuffix, fullRequestUrl: $fullRequestUrl, queryParams: $queryParams")
            
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getUniversalLinkNew(
                    linkSuffix = linkSuffix,
                    fullRequestUrl = fullRequestUrl,
                    queryParams = if (queryParams.isNotEmpty()) queryParams else null
                )
            }
            
            Log.d(TAG, "Received URI: ${response.uri}")
            
            // 리다이렉트 수행
            return response.uri
            
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> {
                    Log.e(TAG, "Link suffix not found: ${e.message()}")
                }
                else -> {
                    Log.e(TAG, "HTTP error occurred: ${e.code()} - ${e.message()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while calling Universal Link API", e)
        }
        return null
    }
    
    /**
     * Checks if the URL is in Universal Link format.
     * @param intent Intent to check
     * @return Whether it's in Universal Link format
     */
    fun isUniversalLink(intent: Intent): Boolean {
        val uri = intent.data ?: return false
        val host = uri.host ?: return false
        
        // {suffix}.limelink.org 패턴 확인
        if (host.endsWith(".$LIMELINK_HOST")) {
            return uri.scheme == "https"
        }
        
        // deep.limelink.org 패턴 확인 (기존 deeplink)
        if (host == DEEP_LIMELINK_HOST) {
            return uri.scheme == "https"
        }
        
        return false
    }
} 