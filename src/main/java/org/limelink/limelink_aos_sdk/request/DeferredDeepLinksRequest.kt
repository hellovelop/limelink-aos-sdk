package org.limelink.limelink_aos_sdk.request

/**
 * Request model for paginated deferred deep link list query
 * @param limit Number of items per page (default: 10)
 * @param startId ID for pagination (optional)
 * @param startCreatedAt Created timestamp for pagination (optional)
 * @param keyword Search keyword for name or token (optional)
 */
data class DeferredDeepLinksRequest(
    val limit: Int = 10,
    val startId: String? = null,
    val startCreatedAt: String? = null,
    val keyword: String? = null
)

