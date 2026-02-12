package org.limelink.limelink_aos_sdk.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.UniversalLinkHandler

internal class LimeLinkLifecycleHandler : Application.ActivityLifecycleCallbacks {

    private var lastIntentUri: String? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val intent = activity.intent ?: return
        if (UniversalLinkHandler.isUniversalLink(intent)) {
            lastIntentUri = intent.dataString
            LimeLinkSDK.handleLinkIntent(activity, intent)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        val intent = activity.intent ?: return
        val currentUri = intent.dataString
        if (currentUri != lastIntentUri && UniversalLinkHandler.isUniversalLink(intent)) {
            lastIntentUri = currentUri
            LimeLinkSDK.handleLinkIntent(activity, intent)
        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
