package org.limelink.limelink_aos_sdk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
class LinkStatsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear SharedPreferences before each test
        context.getSharedPreferences("link_first_launch_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `isFirstLaunch returns true on first call`() {
        assertTrue(LinkStats.isFirstLaunch(context))
    }

    @Test
    fun `isFirstLaunch returns false on second call`() {
        LinkStats.isFirstLaunch(context) // first call
        assertFalse(LinkStats.isFirstLaunch(context)) // second call
    }

    @Test
    fun `isFirstLaunch stores false in SharedPreferences after first call`() {
        LinkStats.isFirstLaunch(context)

        val prefs = context.getSharedPreferences("link_first_launch_prefs", Context.MODE_PRIVATE)
        val storedValue = prefs.getBoolean("is_first_launch", true)
        assertEquals(false, storedValue)
    }
}
