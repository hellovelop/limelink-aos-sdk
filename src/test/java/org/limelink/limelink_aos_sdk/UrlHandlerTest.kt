package org.limelink.limelink_aos_sdk

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UrlHandlerTest {

    // ===== getSchemeFromIntent =====

    @Test
    fun `getSchemeFromIntent returns original-url query parameter when present`() {
        val originalUrl = "https://example.com/target"
        val uri = Uri.parse("https://abc.limelink.org/link/xyz?original-url=$originalUrl")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UrlHandler.getSchemeFromIntent(intent)
        assertEquals(originalUrl, result)
    }

    @Test
    fun `getSchemeFromIntent returns null when original-url param is missing`() {
        val uri = Uri.parse("https://abc.limelink.org/link/xyz?other=value")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UrlHandler.getSchemeFromIntent(intent)
        assertNull(result)
    }

    @Test
    fun `getSchemeFromIntent returns null when action is not ACTION_VIEW`() {
        val uri = Uri.parse("https://abc.limelink.org/link/xyz?original-url=https://example.com")
        val intent = Intent(Intent.ACTION_MAIN, uri)

        val result = UrlHandler.getSchemeFromIntent(intent)
        assertNull(result)
    }

    // ===== parseQueryParams =====

    @Test
    fun `parseQueryParams returns map of query parameters`() {
        val uri = Uri.parse("https://abc.limelink.org/link/xyz?key1=value1&key2=value2")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UrlHandler.parseQueryParams(intent)
        assertEquals("value1", result["key1"])
        assertEquals("value2", result["key2"])
        assertEquals(2, result.size)
    }

    @Test
    fun `parseQueryParams returns empty map when no query parameters`() {
        val uri = Uri.parse("https://abc.limelink.org/link/xyz")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UrlHandler.parseQueryParams(intent)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseQueryParams returns empty map when intent data is null`() {
        val intent = Intent(Intent.ACTION_VIEW)

        val result = UrlHandler.parseQueryParams(intent)
        assertTrue(result.isEmpty())
    }

    // ===== parsePathParams =====

    @Test
    fun `parsePathParams extracts mainPath and subPath from path segments`() {
        val uri = Uri.parse("https://abc.limelink.org/link/main/sub")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UrlHandler.parsePathParams(intent)
        assertEquals("main", result.mainPath)
        assertEquals("sub", result.subPath)
    }

    @Test
    fun `parsePathParams extracts mainPath with null subPath when only two segments`() {
        val uri = Uri.parse("https://abc.limelink.org/link/main")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UrlHandler.parsePathParams(intent)
        assertEquals("main", result.mainPath)
        assertNull(result.subPath)
    }

    @Test
    fun `parsePathParams returns empty mainPath when intent data is null`() {
        val intent = Intent(Intent.ACTION_VIEW)

        val result = UrlHandler.parsePathParams(intent)
        assertEquals("", result.mainPath)
        assertEquals("", result.subPath)
    }
}
