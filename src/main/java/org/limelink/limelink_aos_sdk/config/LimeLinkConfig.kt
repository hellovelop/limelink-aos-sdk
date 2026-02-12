package org.limelink.limelink_aos_sdk.config

class LimeLinkConfig private constructor(
    val apiKey: String,
    val baseUrl: String,
    val loggingEnabled: Boolean,
    val deferredDeeplinkEnabled: Boolean
) {
    class Builder(private val apiKey: String) {
        private var baseUrl: String = "https://limelink.org/"
        private var loggingEnabled: Boolean = false
        private var deferredDeeplinkEnabled: Boolean = true

        fun setBaseUrl(url: String) = apply { this.baseUrl = url }
        fun setLogging(enabled: Boolean) = apply { this.loggingEnabled = enabled }
        fun setDeferredDeeplinkEnabled(enabled: Boolean) = apply { this.deferredDeeplinkEnabled = enabled }

        fun build(): LimeLinkConfig {
            require(apiKey.isNotBlank()) { "apiKey must not be blank" }
            return LimeLinkConfig(
                apiKey = apiKey,
                baseUrl = baseUrl,
                loggingEnabled = loggingEnabled,
                deferredDeeplinkEnabled = deferredDeeplinkEnabled
            )
        }
    }
}
