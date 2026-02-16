package org.limelink.limelink_aos_sdk.service

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RetrofitClientTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `initialize sets base URL and apiService makes requests to that URL`() = runTest {
        val baseUrl = mockWebServer.url("/").toString()
        RetrofitClient.initialize(baseUrl)

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"uri":"https://example.com"}"""))

        val apiService = RetrofitClient.apiService
        apiService.getUniversalLinkNew("test-suffix")

        val request = mockWebServer.takeRequest()
        assertTrue(request.path!!.startsWith("/api/v1/app/dynamic_link/test-suffix"))
    }

    @Test
    fun `apiService returns same singleton instance`() {
        val baseUrl = mockWebServer.url("/").toString()
        RetrofitClient.initialize(baseUrl)

        val first = RetrofitClient.apiService
        val second = RetrofitClient.apiService

        assertNotNull(first)
        assertSame(first, second)
    }

    @Test
    fun `initialize resets instances and creates new apiService`() {
        val baseUrl = mockWebServer.url("/").toString()
        RetrofitClient.initialize(baseUrl)

        val first = RetrofitClient.apiService

        // Re-initialize resets cached instances
        RetrofitClient.initialize(baseUrl)

        val second = RetrofitClient.apiService

        assertNotNull(first)
        assertNotNull(second)
        assertNotSame(first, second)
    }
}
