package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.limelink.limelink_aos_sdk.service.RetrofitClient
import org.limelink.limelink_aos_sdk.response.UniversalLinkResponse
import org.limelink.limelink_aos_sdk.response.DeeplinkResponse
import retrofit2.HttpException
import java.util.regex.Pattern

/**
 * Class responsible for handling Universal Links
 * Processes URLs in the format https://{suffix}.limelink.org/link/{link_suffix} by
 * calling the API https://www.limelink.org/api/v1/dynamic_link/{link_suffix} and
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
     * @return URI to redirect to, or null if processing failed
     */
    suspend fun handleUniversalLink(context: Context, intent: Intent): String? {
        return try {
            val uri = intent.data ?: run {
                Log.e(TAG, "No data in intent")
                return null
            }
            
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
        val linkPattern = "^/link/(.+)$"
        
        val pattern = Pattern.compile(linkPattern)
        val matcher = pattern.matcher(path)
        
        if (!matcher.find()) {
            Log.e(TAG, "❌ 서브도메인 패턴이 일치하지 않습니다: $path")
            return null
        }
        
        val linkSuffix = matcher.group(1)
        Log.d(TAG, "Extracted suffix: $suffix, linkSuffix: $linkSuffix")
        
        // 먼저 서브도메인에서 헤더 정보 가져오기
        val headers = fetchSubdomainHeaders(suffix)
        
        // 헤더 정보를 사용하여 Universal Link API 호출
        return fetchUniversalLinkWithHeaders(linkSuffix, headers)
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
     * 서브도메인 헤더 정보 가져오기
     */
    private suspend fun fetchSubdomainHeaders(suffix: String): Map<String, String> {
        return try {
            val urlString = "https://$suffix.$LIMELINK_HOST"
            Log.d(TAG, "Fetching headers from: $urlString")
            
            // 간단한 헤더 정보 반환 (실제 구현에서는 OkHttp의 HEAD 요청 사용)
            mapOf(
                "X-Request-ID" to "android-${System.currentTimeMillis()}",
                "X-User-Agent" to "LimeLink-Android-SDK",
                "X-Referer" to "https://$suffix.$LIMELINK_HOST"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subdomain headers", e)
            emptyMap()
        }
    }
    
    /**
     * 헤더 정보를 포함한 Universal Link API 호출
     */
    private suspend fun fetchUniversalLinkWithHeaders(linkSuffix: String, headers: Map<String, String>): String? {
        try {
            Log.d(TAG, "Calling Universal Link API with linkSuffix: $linkSuffix")
            
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getUniversalLinkNew(linkSuffix)
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