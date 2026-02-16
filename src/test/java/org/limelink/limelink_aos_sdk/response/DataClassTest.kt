package org.limelink.limelink_aos_sdk.response

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.limelink.limelink_aos_sdk.request.LimeLinkRequest

class DataClassTest {

    // --- PathParamResponse ---

    @Test
    fun `PathParamResponse creation with defaults`() {
        val response = PathParamResponse(mainPath = "/main")

        assertEquals("/main", response.mainPath)
        assertNull(response.subPath)
    }

    @Test
    fun `PathParamResponse creation with subPath`() {
        val response = PathParamResponse(mainPath = "/main", subPath = "/sub")

        assertEquals("/main", response.mainPath)
        assertEquals("/sub", response.subPath)
    }

    // --- LimeLinkRequest ---

    @Test
    fun `LimeLinkRequest creation with defaults`() {
        val request = LimeLinkRequest(
            private_key = "pk_123",
            suffix = "abc",
            event_type = "first_run"
        )

        assertEquals("pk_123", request.private_key)
        assertEquals("abc", request.suffix)
        assertNull(request.handle)
        assertEquals("first_run", request.event_type)
        assertEquals("android", request.operating_system)
    }

    @Test
    fun `LimeLinkRequest creation with all fields`() {
        val request = LimeLinkRequest(
            private_key = "pk_456",
            suffix = "xyz",
            handle = "my-handle",
            event_type = "rerun",
            operating_system = "android"
        )

        assertEquals("pk_456", request.private_key)
        assertEquals("xyz", request.suffix)
        assertEquals("my-handle", request.handle)
        assertEquals("rerun", request.event_type)
        assertEquals("android", request.operating_system)
    }

    // --- LimeLinkUrl ---

    @Test
    fun `LimeLinkUrl creation with query params`() {
        val limeLinkUrl = LimeLinkUrl(
            referrer = "utm_source=google&url=https://abc.limelink.org/link/test?k=v",
            url = "https://abc.limelink.org/link/test",
            fullUrl = "https://abc.limelink.org/link/test?k=v",
            queryString = "k=v",
            queryParams = mapOf("k" to "v")
        )

        assertEquals("https://abc.limelink.org/link/test", limeLinkUrl.url)
        assertEquals("https://abc.limelink.org/link/test?k=v", limeLinkUrl.fullUrl)
        assertEquals("k=v", limeLinkUrl.queryString)
        assertEquals(mapOf("k" to "v"), limeLinkUrl.queryParams)
    }

    @Test
    fun `LimeLinkUrl creation without query params`() {
        val limeLinkUrl = LimeLinkUrl(
            referrer = "url=https://abc.limelink.org/link/test",
            url = "https://abc.limelink.org/link/test",
            fullUrl = "https://abc.limelink.org/link/test",
            queryString = null,
            queryParams = emptyMap()
        )

        assertEquals(limeLinkUrl.url, limeLinkUrl.fullUrl)
        assertNull(limeLinkUrl.queryString)
        assertEquals(emptyMap<String, String>(), limeLinkUrl.queryParams)
    }

    // --- ReferrerInfo ---

    @Test
    fun `ReferrerInfo creation with all fields`() {
        val limeLinkUrl = LimeLinkUrl(
            referrer = "ref",
            url = "https://limelink.org/abc",
            fullUrl = "https://limelink.org/abc",
            queryString = null,
            queryParams = emptyMap()
        )
        val info = ReferrerInfo(
            referrerUrl = "https://example.com",
            clickTimestamp = 1000L,
            installTimestamp = 2000L,
            limeLinkUrl = limeLinkUrl
        )

        assertEquals("https://example.com", info.referrerUrl)
        assertEquals(1000L, info.clickTimestamp)
        assertEquals(2000L, info.installTimestamp)
        assertEquals("https://limelink.org/abc", info.limeLinkUrl?.fullUrl)
    }

    @Test
    fun `ReferrerInfo nullable fields accept null`() {
        val info = ReferrerInfo(
            referrerUrl = null,
            clickTimestamp = 0L,
            installTimestamp = 0L,
            limeLinkUrl = null
        )

        assertNull(info.referrerUrl)
        assertNull(info.limeLinkUrl)
    }

    // --- LimeLinkError ---

    @Test
    fun `LimeLinkError creation with defaults`() {
        val error = LimeLinkError(code = 404, message = "Not Found")

        assertEquals(404, error.code)
        assertEquals("Not Found", error.message)
        assertNull(error.exception)
    }

    @Test
    fun `LimeLinkError creation with exception`() {
        val cause = RuntimeException("test error")
        val error = LimeLinkError(code = 500, message = "Internal Error", exception = cause)

        assertEquals(500, error.code)
        assertEquals("Internal Error", error.message)
        assertEquals(cause, error.exception)
    }

    // --- UniversalLinkResponse & DeeplinkResponse ---

    @Test
    fun `UniversalLinkResponse stores uri`() {
        val response = UniversalLinkResponse(uri = "https://example.com/path")
        assertEquals("https://example.com/path", response.uri)
    }

    @Test
    fun `DeeplinkResponse stores deeplinkUrl`() {
        val response = DeeplinkResponse(deeplinkUrl = "myapp://path/to/content")
        assertEquals("myapp://path/to/content", response.deeplinkUrl)
    }

    // --- data class equals / hashCode / copy ---

    @Test
    fun `data class equals works correctly`() {
        val a = PathParamResponse(mainPath = "/main", subPath = "/sub")
        val b = PathParamResponse(mainPath = "/main", subPath = "/sub")
        val c = PathParamResponse(mainPath = "/other", subPath = "/sub")

        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `data class hashCode is consistent with equals`() {
        val a = LimeLinkError(code = 400, message = "Bad Request")
        val b = LimeLinkError(code = 400, message = "Bad Request")

        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `data class copy produces modified instance`() {
        val original = PathParamResponse(mainPath = "/main", subPath = "/sub")
        val copied = original.copy(mainPath = "/updated")

        assertEquals("/updated", copied.mainPath)
        assertEquals("/sub", copied.subPath)
        assertNotEquals(original, copied)
    }
}
