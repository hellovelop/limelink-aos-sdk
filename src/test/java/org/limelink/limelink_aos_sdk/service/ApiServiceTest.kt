package org.limelink.limelink_aos_sdk.service

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class ApiServiceTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        RetrofitClient.initialize(mockWebServer.url("/").toString())
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `sendLimeLink sends POST to correct endpoint with JSON body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = org.limelink.limelink_aos_sdk.request.LimeLinkRequest(
            private_key = "test-key",
            suffix = "test-suffix",
            handle = "test-handle",
            event_type = "first_run"
        )
        RetrofitClient.apiService.sendLimeLink(request)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/api/v1/stats/event", recorded.path)

        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"private_key\":\"test-key\""))
        assertTrue(body.contains("\"suffix\":\"test-suffix\""))
        assertTrue(body.contains("\"handle\":\"test-handle\""))
        assertTrue(body.contains("\"event_type\":\"first_run\""))
        assertTrue(body.contains("\"operating_system\":\"android\""))
    }

    @Test
    fun `getUniversalLinkNew sends GET to correct path`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"uri":"https://target.com/page"}""")
        )

        val response = RetrofitClient.apiService.getUniversalLinkNew(
            linkSuffix = "abc123"
        )

        val recorded = mockWebServer.takeRequest()
        assertEquals("GET", recorded.method)
        assertTrue(recorded.path!!.startsWith("/api/v1/app/dynamic_link/abc123"))
        assertEquals("https://target.com/page", response.uri)
    }

    @Test
    fun `getUniversalLinkNew with queryParams includes them in URL`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"uri":"https://target.com"}""")
        )

        val queryParams = mapOf("utm_source" to "test", "ref" to "campaign")
        RetrofitClient.apiService.getUniversalLinkNew(
            linkSuffix = "xyz",
            fullRequestUrl = "https://example.limelink.org/link/xyz",
            queryParams = queryParams
        )

        val recorded = mockWebServer.takeRequest()
        val path = recorded.path!!
        assertTrue(path.startsWith("/api/v1/app/dynamic_link/xyz"))
        assertTrue(path.contains("full_request_url="))
        assertTrue(path.contains("utm_source=test"))
        assertTrue(path.contains("ref=campaign"))
    }

    @Test
    fun `getDeeplink sends GET with correct query parameters`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"deeplinkUrl":"myapp://home"}""")
        )

        val response = RetrofitClient.apiService.getDeeplink(
            subdomain = "deep",
            path = "/some/path",
            platform = "android"
        )

        val recorded = mockWebServer.takeRequest()
        assertEquals("GET", recorded.method)
        val path = recorded.path!!
        assertTrue(path.startsWith("/link"))
        assertTrue(path.contains("subdomain=deep"))
        assertTrue(path.contains("platform=android"))
        assertEquals("myapp://home", response.deeplinkUrl)
    }

    @Test
    fun `getDeferredDeepLinkBySuffix includes event_type setup by default`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"uri":"https://deferred.com"}""")
        )

        val response = RetrofitClient.apiService.getDeferredDeepLinkBySuffix("def456")

        val recorded = mockWebServer.takeRequest()
        assertEquals("GET", recorded.method)
        val path = recorded.path!!
        assertTrue(path.startsWith("/api/v1/app/dynamic_link/def456"))
        assertTrue(path.contains("event_type=setup"))
        assertEquals("https://deferred.com", response.uri)
    }

    @Test
    fun `404 response throws HttpException`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        try {
            RetrofitClient.apiService.getUniversalLinkNew(
                linkSuffix = "nonexistent"
            )
            fail("Expected HttpException")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
    }

    @Test
    fun `500 response throws HttpException`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        try {
            RetrofitClient.apiService.getUniversalLinkNew(
                linkSuffix = "server-error"
            )
            fail("Expected HttpException")
        } catch (e: HttpException) {
            assertEquals(500, e.code())
        }
    }
}
