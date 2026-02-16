package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.limelink.limelink_aos_sdk.service.RetrofitClient
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UniversalLinkHandlerNetworkTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var context: Context

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        RetrofitClient.initialize(mockWebServer.url("/").toString())
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `handleUniversalLink with subdomain pattern calls API and returns URI`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"uri":"myapp://product/123"}""")
        )

        val uri = Uri.parse("https://mysuffix.limelink.org/link/abc123?ref=test")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UniversalLinkHandler.handleUniversalLink(context, intent, uri)

        assertEquals("myapp://product/123", result)

        val recorded = mockWebServer.takeRequest()
        assertEquals("GET", recorded.method)
        assertTrue(recorded.path!!.contains("/api/v1/app/dynamic_link/abc123"))
        assertTrue(recorded.path!!.contains("ref=test"))
    }

    @Test
    fun `handleUniversalLink works without query params`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"uri":"myapp://no-query"}""")
        )

        val uri = Uri.parse("https://mysuffix.limelink.org/link/abc123")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UniversalLinkHandler.handleUniversalLink(context, intent, uri)

        assertEquals("myapp://no-query", result)
    }

    @Test
    fun `handleUniversalLink with subdomain pattern returns null on 404`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val uri = Uri.parse("https://mysuffix.limelink.org/link/notfound?ref=test")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UniversalLinkHandler.handleUniversalLink(context, intent, uri)

        assertNull(result)
    }

    @Test
    fun `handleLegacyDeeplink via deep host with link path calls API`() = runTest {
        // Note: deep.limelink.org actually matches the subdomain pattern (endsWith ".limelink.org")
        // so it goes through handleSubdomainPattern, not handleLegacyDeeplink.
        // It needs a /link/{suffix} path to work.
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"uri":"myapp://legacy/page"}""")
        )

        val uri = Uri.parse("https://deep.limelink.org/link/somepath?ref=legacy")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UniversalLinkHandler.handleUniversalLink(context, intent, uri)

        assertEquals("myapp://legacy/page", result)

        val recorded = mockWebServer.takeRequest()
        assertEquals("GET", recorded.method)
        assertTrue(recorded.path!!.contains("/api/v1/app/dynamic_link/somepath"))
    }

    @Test
    fun `handleUniversalLink returns null on server error`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Error"))

        val uri = Uri.parse("https://test.limelink.org/link/fail?ref=x")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UniversalLinkHandler.handleUniversalLink(context, intent, uri)

        assertNull(result)
    }

    @Test
    fun `handleUniversalLink returns null when path does not start with link`() = runTest {
        val uri = Uri.parse("https://mysuffix.limelink.org/other/path")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UniversalLinkHandler.handleUniversalLink(context, intent, uri)

        assertNull(result)
        assertEquals(0, mockWebServer.requestCount)
    }

    @Test
    fun `handleUniversalLink returns null when host is null`() = runTest {
        val uri = Uri.parse("mailto:test@example.com")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val result = UniversalLinkHandler.handleUniversalLink(context, intent, uri)

        assertNull(result)
        assertEquals(0, mockWebServer.requestCount)
    }
}
