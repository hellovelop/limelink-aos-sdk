package org.limelink.limelink_aos_sdk.lifecycle

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import android.app.Activity

/**
 * Tests for LimeLinkLifecycleHandler.
 *
 * Since LimeLinkSDK.handleLinkIntent is internal and triggers network calls,
 * we test the handler's core logic: isUniversalLink gating and lastIntentUri deduplication
 * by inspecting the handler's internal state via reflection.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LimeLinkLifecycleHandlerTest {

    private lateinit var handler: LimeLinkLifecycleHandler

    @Before
    fun setUp() {
        handler = LimeLinkLifecycleHandler()
    }

    private fun getLastIntentUri(): String? {
        val field = LimeLinkLifecycleHandler::class.java.getDeclaredField("lastIntentUri")
        field.isAccessible = true
        return field.get(handler) as String?
    }

    private fun setLastIntentUri(value: String?) {
        val field = LimeLinkLifecycleHandler::class.java.getDeclaredField("lastIntentUri")
        field.isAccessible = true
        field.set(handler, value)
    }

    @Test
    fun `onActivityCreated sets lastIntentUri for universal link`() {
        val uri = Uri.parse("https://abc.limelink.org/link/xyz")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val activityController: ActivityController<Activity> = Robolectric.buildActivity(
            Activity::class.java, intent
        )
        val activity = activityController.get()

        // Before: lastIntentUri should be null
        assertNull(getLastIntentUri())

        // Call onActivityCreated - it checks isUniversalLink and sets lastIntentUri
        // Note: handleLinkIntent will also be called but may throw due to uninitialized SDK;
        // we catch that and verify state was set.
        try {
            handler.onActivityCreated(activity, null)
        } catch (_: Exception) {
            // SDK not initialized, but lastIntentUri should be set before handleLinkIntent
        }

        // lastIntentUri should be set to the intent's dataString
        assertEquals(intent.dataString, getLastIntentUri())
    }

    @Test
    fun `onActivityCreated does not set lastIntentUri for non-universal link`() {
        val uri = Uri.parse("https://other.com/path")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val activityController: ActivityController<Activity> = Robolectric.buildActivity(
            Activity::class.java, intent
        )
        val activity = activityController.get()

        handler.onActivityCreated(activity, null)

        // lastIntentUri should remain null for non-universal links
        assertNull(getLastIntentUri())
    }

    @Test
    fun `onActivityResumed updates lastIntentUri for new URI`() {
        // Simulate that onActivityCreated already processed a first link
        val firstDataString = "https://abc.limelink.org/link/first"
        setLastIntentUri(firstDataString)

        val secondUri = Uri.parse("https://abc.limelink.org/link/second")
        val secondIntent = Intent(Intent.ACTION_VIEW, secondUri)

        val activityController: ActivityController<Activity> = Robolectric.buildActivity(
            Activity::class.java, secondIntent
        )
        val activity = activityController.get()

        try {
            handler.onActivityResumed(activity)
        } catch (_: Exception) {
            // SDK not initialized
        }

        // lastIntentUri should be updated to the new URI
        assertEquals(secondIntent.dataString, getLastIntentUri())
    }

    @Test
    fun `onActivityResumed does not update lastIntentUri for same URI (dedup)`() {
        val dataString = "https://abc.limelink.org/link/xyz"
        setLastIntentUri(dataString)

        val uri = Uri.parse(dataString)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val activityController: ActivityController<Activity> = Robolectric.buildActivity(
            Activity::class.java, intent
        )
        val activity = activityController.get()

        handler.onActivityResumed(activity)

        // lastIntentUri should remain the same (dedup prevents re-processing)
        assertEquals(dataString, getLastIntentUri())
    }

    @Test
    fun `onActivityCreated safely returns when intent is null`() {
        val activityController: ActivityController<Activity> = Robolectric.buildActivity(
            Activity::class.java
        )
        val activity = activityController.get()
        // activity.intent will be non-null but have no data by default in Robolectric

        // Should not throw any exception
        handler.onActivityCreated(activity, null)
        assertNull(getLastIntentUri())
    }
}
