package org.limelink.limelink_aos_sdk

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.limelink.limelink_aos_sdk.config.LimeLinkConfig
import org.limelink.limelink_aos_sdk.lifecycle.LimeLinkLifecycleHandler
import org.limelink.limelink_aos_sdk.response.LimeLinkError
import org.limelink.limelink_aos_sdk.response.LimeLinkResult
import org.limelink.limelink_aos_sdk.response.PathParamResponse
import org.limelink.limelink_aos_sdk.service.RetrofitClient

/**
 * Main class for LimeLink Android SDK
 * Provides Universal Link handling and other SDK functionality.
 */
object LimeLinkSDK {

    private const val TAG = "LimeLinkSDK"

    private var config: LimeLinkConfig? = null
    private var isInitialized = false
    private var lifecycleHandler: LimeLinkLifecycleHandler? = null
    private val listeners = mutableListOf<LimeLinkListener>()
    private val internalScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Initializes the LimeLink SDK with automatic lifecycle handling.
     *
     * @param application Application instance
     * @param config SDK configuration
     */
    fun init(application: Application, config: LimeLinkConfig) {
        if (isInitialized) {
            log("SDK already initialized")
            return
        }

        this.config = config
        RetrofitClient.initialize(config.baseUrl)

        lifecycleHandler = LimeLinkLifecycleHandler().also {
            application.registerActivityLifecycleCallbacks(it)
        }

        isInitialized = true
        log("SDK initialized with apiKey=${config.apiKey.take(4)}***")

        if (config.deferredDeeplinkEnabled) {
            checkDeferredDeeplink(application)
        }
    }

    /**
     * Registers a listener to receive deeplink events.
     */
    fun addLinkListener(listener: LimeLinkListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /**
     * Removes a previously registered listener.
     */
    fun removeLinkListener(listener: LimeLinkListener) {
        listeners.remove(listener)
    }

    /**
     * Called internally by LimeLinkLifecycleHandler when a link intent is detected.
     */
    internal fun handleLinkIntent(activity: Activity, intent: Intent) {
        log("handleLinkIntent from ${activity.javaClass.simpleName}")

        val uri = intent.data ?: run {
            Log.e(TAG, "Intent data is null in handleLinkIntent")
            return
        }

        internalScope.launch {
            try {
                val resolvedUri = withContext(Dispatchers.IO) {
                    UniversalLinkHandler.handleUniversalLink(activity, intent, uri)
                }

                val queryParams = UrlHandler.parseQueryParams(intent)
                val pathParams = UrlHandler.parsePathParams(intent)

                val result = LimeLinkResult(
                    originalUrl = intent.dataString,
                    resolvedUri = resolvedUri?.let { Uri.parse(it) },
                    queryParams = queryParams,
                    pathParams = pathParams
                )

                notifyListeners(result)

                // Auto-track stats if initialized
                val apiKey = config?.apiKey
                if (apiKey != null) {
                    withContext(Dispatchers.IO) {
                        internalSaveLimeLinkStatus(activity, intent, apiKey)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling link intent", e)
                val error = LimeLinkError(
                    code = -1,
                    message = e.message ?: "Unknown error",
                    exception = e
                )
                listeners.forEach { it.onDeeplinkError(error) }
            }
        }
    }

    /**
     * Retrieves install referrer information.
     *
     * @param context Android context
     * @param callback Callback with ReferrerInfo or null
     */
    fun getInstallReferrer(context: Context, callback: (org.limelink.limelink_aos_sdk.response.ReferrerInfo?) -> Unit) {
        InstallReferrerHandler.getInstallReferrer(context, callback)
    }

    /**
     * Checks for deferred deeplink on first launch.
     */
    fun checkDeferredDeeplink(context: Context, callback: ((LimeLinkResult?) -> Unit)? = null) {
        if (!LinkStats.isFirstLaunch(context)) {
            log("Not first launch, skipping deferred deeplink check")
            callback?.invoke(null)
            return
        }

        log("First launch detected, checking deferred deeplink")
        InstallReferrerHandler.getInstallReferrer(context) { referrerInfo ->
            if (referrerInfo?.limeLinkUrl != null) {
                val result = LimeLinkResult(
                    originalUrl = referrerInfo.limeLinkUrl,
                    resolvedUri = Uri.parse(referrerInfo.limeLinkUrl),
                    queryParams = emptyMap(),
                    pathParams = PathParamResponse(mainPath = "", subPath = null),
                    isDeferred = true,
                    referrerInfo = referrerInfo
                )
                notifyListeners(result)
                callback?.invoke(result)
            } else {
                log("No deferred deeplink found in referrer")
                callback?.invoke(null)
            }
        }
    }

    // ========== Backward-compatible public methods ==========

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
        if (!UniversalLinkHandler.isUniversalLink(intent)) {
            Log.d(TAG, "Not a Universal Link format.")
            callback?.invoke(null)
            return
        }

        // Intent.data를 미리 추출하여 비동기 처리 시 안전하게 전달
        val uri = intent.data
        if (uri == null) {
            Log.e(TAG, "Intent data is null even though isUniversalLink returned true")
            callback?.invoke(null)
            return
        }

        Log.d(TAG, "Starting Universal Link processing with URI: $uri")

        internalScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    UniversalLinkHandler.handleUniversalLink(context, intent, uri)
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
    fun parsePathParams(intent: Intent): PathParamResponse {
        return UrlHandler.parsePathParams(intent)
    }

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

        internalScope.launch {
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

    // ========== Internal helpers ==========

    private fun notifyListeners(result: LimeLinkResult) {
        listeners.forEach { it.onDeeplinkReceived(result) }
    }

    private fun log(message: String) {
        if (config?.loggingEnabled == true) {
            Log.d(TAG, message)
        }
    }
}
