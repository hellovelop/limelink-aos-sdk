package org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for LimeLink SDK running on a real device/emulator.
 * Verifies SharedPreferences persistence and Universal Link detection.
 */
@RunWith(AndroidJUnit4::class)
class LimeLinkIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // Clear SharedPreferences for clean test state
        context.getSharedPreferences("link_first_launch_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        // Reset LimeLinkSDK singleton state via reflection
        resetSdkState()
    }

    private fun resetSdkState() {
        val sdkClass = LimeLinkSDK::class.java

        val isInitializedField = sdkClass.getDeclaredField("isInitialized")
        isInitializedField.isAccessible = true
        isInitializedField.setBoolean(LimeLinkSDK, false)

        val configField = sdkClass.getDeclaredField("config")
        configField.isAccessible = true
        configField.set(LimeLinkSDK, null)

        val listenersField = sdkClass.getDeclaredField("listeners")
        listenersField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val listeners = listenersField.get(LimeLinkSDK) as MutableList<*>
        listeners.clear()

        val lifecycleField = sdkClass.getDeclaredField("lifecycleHandler")
        lifecycleField.isAccessible = true
        lifecycleField.set(LimeLinkSDK, null)
    }

    // ========== Test 1: isFirstLaunch SharedPreferences ==========

    @Test
    fun isFirstLaunch_returnsTrue_onFirstCall() {
        val result = LinkStats.isFirstLaunch(context)
        assertTrue("First call should return true", result)
    }

    @Test
    fun isFirstLaunch_returnsFalse_onSecondCall() {
        LinkStats.isFirstLaunch(context) // first call
        val result = LinkStats.isFirstLaunch(context) // second call
        assertFalse("Second call should return false", result)
    }

    @Test
    fun isFirstLaunch_persistsAcrossCalls() {
        // First call sets the flag
        assertTrue(LinkStats.isFirstLaunch(context))

        // Verify SharedPreferences actually persisted the value on device
        val prefs = context.getSharedPreferences("link_first_launch_prefs", Context.MODE_PRIVATE)
        val storedValue = prefs.getBoolean("is_first_launch", true)
        assertFalse("SharedPreferences should persist false after first launch", storedValue)

        // Subsequent calls should consistently return false
        assertFalse(LinkStats.isFirstLaunch(context))
        assertFalse(LinkStats.isFirstLaunch(context))
    }

    // ========== Test 2: isUniversalLink detection ==========

    @Test
    fun isUniversalLink_returnsTrue_forSubdomainPattern() {
        val intent = Intent().apply {
            data = Uri.parse("https://abc.limelink.org/link/xyz123")
        }
        assertTrue("Subdomain pattern should be recognized as Universal Link",
            LimeLinkSDK.isUniversalLink(intent))
    }

    @Test
    fun isUniversalLink_returnsTrue_forDeepLinkHost() {
        val intent = Intent().apply {
            data = Uri.parse("https://deep.limelink.org/some/path")
        }
        assertTrue("deep.limelink.org should be recognized as Universal Link",
            LimeLinkSDK.isUniversalLink(intent))
    }

    @Test
    fun isUniversalLink_returnsFalse_forHttpScheme() {
        val intent = Intent().apply {
            data = Uri.parse("http://abc.limelink.org/link/xyz123")
        }
        assertFalse("HTTP scheme should not be recognized as Universal Link",
            LimeLinkSDK.isUniversalLink(intent))
    }

    @Test
    fun isUniversalLink_returnsFalse_forUnrelatedUrl() {
        val intent = Intent().apply {
            data = Uri.parse("https://example.com/some/path")
        }
        assertFalse("Unrelated URL should not be recognized as Universal Link",
            LimeLinkSDK.isUniversalLink(intent))
    }

    @Test
    fun isUniversalLink_returnsFalse_forNullData() {
        val intent = Intent()
        assertFalse("Intent with null data should return false",
            LimeLinkSDK.isUniversalLink(intent))
    }

    @Test
    fun isUniversalLink_returnsFalse_forBareLimeLinkHost() {
        val intent = Intent().apply {
            data = Uri.parse("https://limelink.org/link/xyz123")
        }
        assertFalse("Bare limelink.org (no subdomain) should return false",
            LimeLinkSDK.isUniversalLink(intent))
    }
}
