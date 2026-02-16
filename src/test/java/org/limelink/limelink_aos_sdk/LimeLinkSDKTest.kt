package org.limelink.limelink_aos_sdk

import android.content.Intent
import android.net.Uri
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LimeLinkSDKTest {

    @Before
    fun setUp() {
        resetSDKState()
    }

    @After
    fun tearDown() {
        resetSDKState()
    }

    private fun resetSDKState() {
        // Reset listeners list via reflection
        val listenersField = LimeLinkSDK::class.java.getDeclaredField("listeners")
        listenersField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val listeners = listenersField.get(LimeLinkSDK) as MutableList<LimeLinkListener>
        listeners.clear()

        // Reset isInitialized via reflection
        val initField = LimeLinkSDK::class.java.getDeclaredField("isInitialized")
        initField.isAccessible = true
        initField.setBoolean(LimeLinkSDK, false)
    }

    @Test
    fun `addLinkListener registers a listener`() {
        val listener = createTestListener()
        LimeLinkSDK.addLinkListener(listener)

        val listeners = getListeners()
        assertEquals(1, listeners.size)
        assertTrue(listeners.contains(listener))
    }

    @Test
    fun `addLinkListener prevents duplicate registration`() {
        val listener = createTestListener()
        LimeLinkSDK.addLinkListener(listener)
        LimeLinkSDK.addLinkListener(listener) // duplicate

        val listeners = getListeners()
        assertEquals(1, listeners.size)
    }

    @Test
    fun `removeLinkListener removes the listener`() {
        val listener = createTestListener()
        LimeLinkSDK.addLinkListener(listener)
        LimeLinkSDK.removeLinkListener(listener)

        val listeners = getListeners()
        assertTrue(listeners.isEmpty())
    }

    @Test
    fun `isUniversalLink delegates to UniversalLinkHandler`() {
        val universalLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://abc.limelink.org/link/xyz")
        )
        assertTrue(LimeLinkSDK.isUniversalLink(universalLinkIntent))

        val normalIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://other.com/path")
        )
        assertFalse(LimeLinkSDK.isUniversalLink(normalIntent))
    }

    // ===== Helpers =====

    private fun createTestListener(): LimeLinkListener {
        return object : LimeLinkListener {
            override fun onDeeplinkReceived(result: org.limelink.limelink_aos_sdk.response.LimeLinkResult) {}
        }
    }

    private fun getListeners(): List<LimeLinkListener> {
        val field = LimeLinkSDK::class.java.getDeclaredField("listeners")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(LimeLinkSDK) as List<LimeLinkListener>
    }
}
