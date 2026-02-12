package com.example.limelink

import android.app.Application
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.config.LimeLinkConfig

class LimeLinkDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = LimeLinkConfig.Builder(BuildConfig.LIMELINK_API_KEY)
            .setLogging(true)
            .build()

        LimeLinkSDK.init(this, config)
    }
}
