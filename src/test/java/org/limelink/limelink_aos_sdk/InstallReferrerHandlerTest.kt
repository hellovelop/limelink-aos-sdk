package org.limelink.limelink_aos_sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder

class InstallReferrerHandlerTest {

    // --- extractLimeLinkUrl (returns String?) - backward compatible ---

    @Test
    fun `extractLimeLinkUrl returns URL string from referrer`() {
        val referrer = "utm_source=google&url=https://abc.limelink.org/abc123&utm_medium=cpc"
        val result = InstallReferrerHandler.extractLimeLinkUrl(referrer)

        assertEquals("https://abc.limelink.org/abc123", result)
    }

    @Test
    fun `extractLimeLinkUrl returns null when no limelink URL`() {
        assertNull(InstallReferrerHandler.extractLimeLinkUrl("utm_source=google&url=https://example.com"))
    }

    @Test
    fun `extractLimeLinkUrl returns null for null input`() {
        assertNull(InstallReferrerHandler.extractLimeLinkUrl(null))
    }

    @Test
    fun `extractLimeLinkUrl returns null for empty input`() {
        assertNull(InstallReferrerHandler.extractLimeLinkUrl(""))
    }

    @Test
    fun `extractLimeLinkUrl returns null for blank input`() {
        assertNull(InstallReferrerHandler.extractLimeLinkUrl("   "))
    }

    // --- extractLimeLinkUrlDetail (returns LimeLinkUrl?) - new ---

    @Test
    fun `extracts subdomain limelink URL from referrer string`() {
        val referrer = "utm_source=google&url=https://abc.limelink.org/abc123&utm_medium=cpc"
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail(referrer)

        assertNotNull(result)
        assertEquals("https://abc.limelink.org/abc123", result!!.fullUrl)
        assertEquals("https://abc.limelink.org/abc123", result.url)
        assertNull(result.queryString)
        assertTrue(result.queryParams.isEmpty())
        assertEquals(referrer, result.referrer)
    }

    @Test
    fun `extracts subdomain URL from URL-encoded referrer`() {
        val rawReferrer = "utm_source=test&url=https://test.limelink.org/link/xyz&ref=campaign"
        val encoded = URLEncoder.encode(rawReferrer, "UTF-8")
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail(encoded)

        assertNotNull(result)
        assertEquals("https://test.limelink.org/link/xyz", result!!.fullUrl)
    }

    @Test
    fun `returns null when no limelink URL present`() {
        val referrer = "utm_source=google&utm_medium=cpc&url=https://example.com/page"
        assertNull(InstallReferrerHandler.extractLimeLinkUrlDetail(referrer))
    }

    @Test
    fun `returns null for null detail input`() {
        assertNull(InstallReferrerHandler.extractLimeLinkUrlDetail(null))
    }

    @Test
    fun `returns null for empty detail input`() {
        assertNull(InstallReferrerHandler.extractLimeLinkUrlDetail(""))
    }

    @Test
    fun `returns null for blank detail input`() {
        assertNull(InstallReferrerHandler.extractLimeLinkUrlDetail("   "))
    }

    @Test
    fun `extracts subdomain limelink URL with path`() {
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail("url=https://abc.limelink.org/path/to/link")

        assertNotNull(result)
        assertEquals("https://abc.limelink.org/path/to/link", result!!.fullUrl)
    }

    @Test
    fun `matches http scheme with subdomain`() {
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail("source=test&url=http://sub.limelink.org/test&other=val")

        assertNotNull(result)
        assertEquals("http://sub.limelink.org/test", result!!.fullUrl)
    }

    @Test
    fun `matches https scheme with subdomain`() {
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail("source=test&url=https://sub.limelink.org/test&other=val")

        assertNotNull(result)
        assertEquals("https://sub.limelink.org/test", result!!.fullUrl)
    }

    @Test
    fun `matches bare limelink domain`() {
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail("url=https://limelink.org/page")

        assertNotNull(result)
        assertEquals("https://limelink.org/page", result!!.fullUrl)
    }

    @Test
    fun `does not match similar but different domain`() {
        assertNull(InstallReferrerHandler.extractLimeLinkUrlDetail("url=https://notlimelink.org/page"))
    }

    // --- Query string preservation ---

    @Test
    fun `preserves query string when URL value is encoded in referrer`() {
        val referrer = "utm_source=google&url=https%3A%2F%2Fabc.limelink.org%2Flink%2Ftest%3Fkey1%3Dval1%26key2%3Dval2&utm_medium=cpc"
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail(referrer)

        assertNotNull(result)
        assertEquals("https://abc.limelink.org/link/test?key1=val1&key2=val2", result!!.fullUrl)
        assertEquals("https://abc.limelink.org/link/test", result.url)
        assertEquals("key1=val1&key2=val2", result.queryString)
        assertEquals(mapOf("key1" to "val1", "key2" to "val2"), result.queryParams)
    }

    @Test
    fun `preserves single query param when URL is not encoded`() {
        val referrer = "utm_source=google&url=https://abc.limelink.org/link/test?key1=val1&utm_medium=cpc"
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail(referrer)

        assertNotNull(result)
        assertEquals("https://abc.limelink.org/link/test?key1=val1", result!!.fullUrl)
        assertEquals("https://abc.limelink.org/link/test", result.url)
        assertEquals(mapOf("key1" to "val1"), result.queryParams)
    }

    @Test
    fun `extracts plain limelink URL without key-value wrapper`() {
        val referrer = "https://abc.limelink.org/link/test?key1=val1"
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail(referrer)

        assertNotNull(result)
        assertEquals("https://abc.limelink.org/link/test?key1=val1", result!!.fullUrl)
        assertEquals("https://abc.limelink.org/link/test", result.url)
        assertEquals("key1=val1", result.queryString)
        assertEquals(referrer, result.referrer)
    }

    // --- LimeLinkUrl structure ---

    @Test
    fun `result contains correct referrer original`() {
        val referrer = "utm_source=google&url=https://abc.limelink.org/link/x"
        val result = InstallReferrerHandler.extractLimeLinkUrlDetail(referrer)

        assertEquals(referrer, result?.referrer)
    }

    // --- Consistency: extractLimeLinkUrl == extractLimeLinkUrlDetail.fullUrl ---

    @Test
    fun `extractLimeLinkUrl is consistent with extractLimeLinkUrlDetail fullUrl`() {
        val referrer = "url=https://abc.limelink.org/link/test?key=val"
        val urlString = InstallReferrerHandler.extractLimeLinkUrl(referrer)
        val detail = InstallReferrerHandler.extractLimeLinkUrlDetail(referrer)

        assertEquals(urlString, detail?.fullUrl)
    }
}
