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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.limelink.limelink_aos_sdk.service.RetrofitClient
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LinkStatsNetworkTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var context: Context

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        RetrofitClient.initialize(mockWebServer.url("/").toString())
        context = ApplicationProvider.getApplicationContext()

        // Clear SharedPreferences to ensure clean state for first launch detection
        context.getSharedPreferences("link_first_launch_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createIntentWithPath(path: String): Intent {
        val uri = Uri.parse("https://example.com/link/$path")
        return Intent(Intent.ACTION_VIEW, uri)
    }

    @Test
    fun `internalSaveLimeLinkStatus sends POST when mainPath is present`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val intent = createIntentWithPath("test-suffix")
        internalSaveLimeLinkStatus(context, intent, "api-key-123")

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/api/v1/stats/event", recorded.path)

        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"private_key\":\"api-key-123\""))
        assertTrue(body.contains("\"suffix\":\"test-suffix\""))
    }

    @Test
    fun `internalSaveLimeLinkStatus does NOT call API when mainPath is empty`() = runTest {
        // Intent with no path segments that would yield a mainPath
        val uri = Uri.parse("https://example.com")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        internalSaveLimeLinkStatus(context, intent, "api-key-123")

        // No request should have been sent
        assertEquals(0, mockWebServer.requestCount)
    }

    @Test
    fun `first launch sends event_type first_run`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val intent = createIntentWithPath("launch-suffix")

        // First call - should be first_run
        internalSaveLimeLinkStatus(context, intent, "api-key")

        val recorded = mockWebServer.takeRequest()
        val body = recorded.body.readUtf8()
        assertTrue("Expected first_run event type", body.contains("\"event_type\":\"first_run\""))
    }

    @Test
    fun `subsequent launch sends event_type rerun`() = runTest {
        // First call consumes the first launch flag
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val intent = createIntentWithPath("suffix-a")
        internalSaveLimeLinkStatus(context, intent, "api-key")
        mockWebServer.takeRequest() // consume first request

        // Second call should be rerun
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val intent2 = createIntentWithPath("suffix-b")
        internalSaveLimeLinkStatus(context, intent2, "api-key")

        val recorded = mockWebServer.takeRequest()
        val body = recorded.body.readUtf8()
        assertTrue("Expected rerun event type", body.contains("\"event_type\":\"rerun\""))
    }
}
