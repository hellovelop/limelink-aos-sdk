package org.limelink.limelink_aos_sdk.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LimeLinkConfigTest {

    @Test
    fun `builder creates config with default values`() {
        val config = LimeLinkConfig.Builder("test-api-key").build()

        assertEquals("test-api-key", config.apiKey)
        assertEquals("https://limelink.org/", config.baseUrl)
        assertFalse(config.loggingEnabled)
        assertTrue(config.deferredDeeplinkEnabled)
    }

    @Test
    fun `builder applies custom values correctly`() {
        val config = LimeLinkConfig.Builder("my-key")
            .setBaseUrl("https://custom.example.com/")
            .setLogging(true)
            .setDeferredDeeplinkEnabled(false)
            .build()

        assertEquals("my-key", config.apiKey)
        assertEquals("https://custom.example.com/", config.baseUrl)
        assertTrue(config.loggingEnabled)
        assertFalse(config.deferredDeeplinkEnabled)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `builder throws on empty apiKey`() {
        LimeLinkConfig.Builder("").build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `builder throws on blank-only apiKey`() {
        LimeLinkConfig.Builder("   ").build()
    }

    @Test
    fun `builder fluent API returns same builder instance`() {
        val builder = LimeLinkConfig.Builder("key")
        val returned = builder
            .setBaseUrl("https://example.com/")
            .setLogging(true)
            .setDeferredDeeplinkEnabled(false)

        assertTrue("Fluent API should return same builder", returned === builder)
    }
}
