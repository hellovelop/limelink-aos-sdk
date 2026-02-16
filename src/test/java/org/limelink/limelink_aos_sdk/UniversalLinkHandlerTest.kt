package org.limelink.limelink_aos_sdk

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UniversalLinkHandlerTest {

    @Test
    fun `isUniversalLink returns true for subdomain limelink https URL`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://abc.limelink.org/link/xyz"))
        assertTrue(UniversalLinkHandler.isUniversalLink(intent))
    }

    @Test
    fun `isUniversalLink returns true for deep limelink org https URL`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://deep.limelink.org/path"))
        assertTrue(UniversalLinkHandler.isUniversalLink(intent))
    }

    @Test
    fun `isUniversalLink returns false for non-limelink host`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://other.com/link/xyz"))
        assertFalse(UniversalLinkHandler.isUniversalLink(intent))
    }

    @Test
    fun `isUniversalLink returns false for http scheme`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://abc.limelink.org/link/xyz"))
        assertFalse(UniversalLinkHandler.isUniversalLink(intent))
    }

    @Test
    fun `isUniversalLink returns false when intent data is null`() {
        val intent = Intent(Intent.ACTION_VIEW)
        assertFalse(UniversalLinkHandler.isUniversalLink(intent))
    }

    @Test
    fun `isUniversalLink returns false for bare limelink org without subdomain`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://limelink.org/link/xyz"))
        assertFalse(UniversalLinkHandler.isUniversalLink(intent))
    }
}
