<img src="https://limelink.org/assets/default_lime-C14nNSvc.svg" alt="이미지 설명" style="display: block; margin-left: auto; margin-right: auto; width: 30%;">

# LimeLink Android SDK
This is a dedicated limelink dynamic link library.
The limelink SDK is used to save statistics related to the first run or relaunch of the app, or to control handle values specified for each dynamic link through the https://limelink.org console.

# Getting Started

This section guides you on how to set up and run this project locally.

### Installation and requirements
Add the following items to the ***AndroidManifest.xml*** file
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.example.app">
    
    <!--Example-->
   <uses-permission android:name="android.permission.INTERNET" />

    <!--Example-->
   <application
       android:networkSecurityConfig="@xml/network_security_config">
   </application>
   
</manifest>
```
- Internet Permission: To call external APIs, you need to declare the INTERNET permission in the ***AndroidManifest.xml*** file.
- Network Security Configuration: Starting from API 28 (Android 9.0 Pie), Cleartext (HTTP) is blocked by default. Explicitly configure the network security settings.

Create the ***res/xml/network_security_config.xml*** file:
```xml
<?xml version="1.0" encoding="utf-8"?>
<!--Example-->

<network-security-config>
    <!-- Default configuration: Allow cleartext traffic for all domains -->
    <base-config cleartextTrafficPermitted="true" />

    <!-- Domain-specific configuration: Optional -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">limelink.org</domain>
    </domain-config>
</network-security-config>
```
- In this configuration, base-config allows unencrypted traffic for all domains, whereas domain-config is set to permit HTTPS only for a specific domain (limelink.org).

By adhering to the above guidelines, Android developers should face minimal issues when integrating and using an SDK for calling external APIs.


### Step 1: Add the JitPack repository to your build file
Add it in your root ***build.gradle*** at the end of repositories:
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
### Step 2: Add the dependency
```gradle
dependencies {
    implementation 'com.github.hellovelope:limelink-aos-sdk:main'
}
```
- Please refer to [*here](https://jitpack.io/#hellovelope/limelink-aos-sdk/0.0.3) for **maven**, **sbt**, or **leiningen**.

### Step 3: Manifest file configuration
In the AndroidManifest.xml file, add an intent filter to the MainActivity to handle URLs like schem://example
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.example.app">
    
   <uses-permission android:name="android.permission.INTERNET" />

   <application
       android:networkSecurityConfig="@xml/network_security_config">

       <!--Example-->
       <activity android:name=".MainActivity">
           <intent-filter>
               <action android:name="android.intent.action.VIEW"/>

               <category android:name="android.intent.category.DEFAULT"/>
               <category android:name="android.intent.category.BROWSABLE"/>
          <!--Please enter the domain address you want.-->
               <data android:scheme="https" android:host="customdomain.com"/>
           </intent-filter>
       </activity>
   </application>
   
</manifest>
```
If it's completed, let's refer to the SDK Usage Guide and create it. 
- For more details, please refer to the official document. -> [*official link](https://developer.android.com/training/app-links/verify-android-applinks)


# SDK Usage Guide

## Universal Link Handling

### Universal Link Setup
Add the following configuration to your AndroidManifest.xml to handle Universal Links:

```xml
<activity android:name=".MainActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:scheme="https" android:host="*.limelink.org"/>
    </intent-filter>
</activity>
```

### Universal Link Usage
How to handle Universal Links in MainActivity:

```kt
package com.example.myapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.limelink.limelink_aos_sdk.LimeLinkSDK

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Handle Universal Link in onCreate
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        
        // Handle Universal Link in onNewIntent (when app is already running)
        intent?.let { handleIntent(it) }
    }
    
    private fun handleIntent(intent: Intent) {
        // Check if it's a Universal Link and handle it
        if (LimeLinkSDK.isUniversalLink(intent)) {
            LimeLinkSDK.handleUniversalLink(this, intent) { uri ->
                if (uri != null) {
                    // Handle success - you can now use the URI as needed
                    println("Universal Link URI: $uri")
                    
                    // Example: Navigate to the URI
                    // navigateToUri(uri)
                    
                    // Example: Open in browser
                    // openInBrowser(uri)
                    
                    // Example: Parse and handle internally
                    // handleInternalNavigation(uri)
                } else {
                    // Handle failure
                    println("Universal Link handling failed")
                }
            }
        } else {
            // Handle regular intent
            handleCustomScheme(intent)
        }
    }
    
    private fun handleCustomScheme(intent: Intent) {
        // Handle existing custom scheme
        val originalUrl = LimeLinkSDK.getSchemeFromIntent(intent)
        originalUrl?.let {
            // URL processing logic
        }
    }
}
```

### Universal Link Flow
1. User clicks a URL in the format `https://{suffix}.limelink.org/link/{link_suffix}`
2. SDK extracts the `{suffix}` and `{link_suffix}` parts from the URL
3. Fetches headers from `https://{suffix}.limelink.org` for additional context
4. Calls the API `https://www.limelink.org/api/v1/app/dynamic_link/{link_suffix}` with headers
5. Receives `uri` from API response and automatically redirects to that URL
6. If link_suffix is not found or uri is missing, returns 404 error

### Legacy Deeplink Support
For backward compatibility, the SDK also supports the legacy deeplink format:
1. User clicks a URL in the format `https://deep.limelink.org/link/subdomain={subdomain}&path={path}&platform=android`
2. SDK calls the API `https://deep.limelink.org/link` with query parameters
3. Receives `deeplinkUrl` from API response and redirects accordingly

### Save statistical information
Open ***MainActivity.kt*** and add the following code
```kt
package com.example.myapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.limelink.limelink_aos_sdk.LinkStats

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*Example*/
        val privateKey = "your_private_key"
        saveLimeLinkStatus(this, intent, privateKey)
    }
}
```
- This way, you can save information about the first run or relaunch of the app. You can check the actual metrics on the https://limelink.org console.
- The privateKey value is required. If you don't have it, obtain it from the https://limelink.org console and use it.

### Use handle information superficially
Open ***MainActivity.kt*** and add the following code

```kt
package com.example.myapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.limelink.limelink_aos_sdk.response.PathParamResponse

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        /*Example*/
        val pathParamResponse: PathParamResponse = UrlHandler.parsePathParams(intent)
        val suffix: String = pathParamResponse.getMainPath()

        /*Use the handle values to navigate to the desired screen. */
        val handle: String = pathParamResponse.getSubPath()
        if (handle == "example") {
            //Navigate to the desired screen
        }
    }
}
```
- This way, you can handle the information superficially and navigate to the desired screen based on the handle value.

## Deferred Deep Link

Deferred Deep Link allows you to track and handle deep links even when the app is not installed. When a user clicks a link before installing the app, the link information is stored and can be retrieved after the app is installed and launched for the first time.

### Deferred Deep Link Overview

Deferred Deep Link is useful for:
- Tracking user acquisition sources
- Providing personalized onboarding experiences
- Redirecting users to specific content after app installation
- Measuring marketing campaign effectiveness

> **Note**: Deferred deep links are created and managed through the [LimeLink Console](https://limelink.org). The SDK provides functionality to retrieve deferred deep link information when the app is launched for the first time after installation using Install Referrer API.

### Deferred Deep Link Usage

Use this when the app is launched for the first time after installation. The SDK calls the same API as Universal Link (`/api/v1/app/dynamic_link/{suffix}`) and returns the URI for redirection.

```kt
import org.limelink.limelink_aos_sdk.LimeLinkSDK

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Check if this is the first launch
        if (isFirstLaunch()) {
            // Get the suffix from Install Referrer API
            // Install Referrer provides code={suffix} parameter
            val suffix = getSuffixFromInstallReferrer() // You need to implement this
            
            if (suffix != null) {
                // Handle deferred deep link
                LimeLinkSDK.handleDeferredDeepLink(suffix) { uri ->
                    if (uri != null) {
                        // Handle success - you can now use the URI as needed
                        println("Deferred Deep Link URI: $uri")
                        
                        // Example: Navigate to the URI
                        // navigateToUri(uri)
                        
                        // Example: Open in browser
                        // openInBrowser(uri)
                        
                        // Example: Parse and handle internally
                        // handleInternalNavigation(uri)
                    } else {
                        // Handle failure
                        println("Deferred Deep Link handling failed")
                        // Proceed with normal app launch
                        navigateToDefault()
                    }
                }
            } else {
                // No suffix found in install referrer
                navigateToDefault()
            }
        } else {
            // Normal app launch
            navigateToDefault()
        }
    }
    
    private fun isFirstLaunch(): Boolean {
        // Implement your first launch detection logic
        // You can use SharedPreferences or other methods
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirst = prefs.getBoolean("is_first_launch", true)
        if (isFirst) {
            prefs.edit().putBoolean("is_first_launch", false).apply()
        }
        return isFirst
    }
    
    private fun getSuffixFromInstallReferrer(): String? {
        // Implement logic to get suffix from Install Referrer API
        // Install Referrer provides code={suffix} parameter
        // Example using Play Install Referrer Library:
        /*
        val referrerClient = InstallReferrerClient.newBuilder(this).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        val response = referrerClient.installReferrer
                        val referrerUrl = response.installReferrer
                        // Parse code={suffix} from referrerUrl
                        // Return the suffix value
                    }
                }
            }
            override fun onInstallReferrerServiceDisconnected() {}
        })
        */
        return null // Placeholder - implement your logic
    }
}
```

### Deferred Deep Link Flow

1. **User clicks a link** before installing the app
2. **Link redirects to app store** (Play Store or App Store) with `code={suffix}` parameter
3. **User installs and opens the app**
4. **App retrieves the suffix** from Install Referrer API (`code={suffix}`)
5. **App calls `handleDeferredDeepLink(suffix)`** with the suffix
6. **SDK calls `/api/v1/app/dynamic_link/{suffix}`** (same as Universal Link)
7. **SDK returns URI** from API response
8. **App navigates to the URI** or handles it as needed

### Install Referrer Setup

To use Deferred Deep Link, you need to integrate the Play Install Referrer Library:

1. **Add dependency** to your `build.gradle`:
```gradle
dependencies {
    implementation 'com.android.installreferrer:installreferrer:2.2'
}
```

2. **Get the suffix** from Install Referrer:
```kt
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener

val referrerClient = InstallReferrerClient.newBuilder(context).build()
referrerClient.startConnection(object : InstallReferrerStateListener {
    override fun onInstallReferrerSetupFinished(responseCode: Int) {
        when (responseCode) {
            InstallReferrerClient.InstallReferrerResponse.OK -> {
                val response = referrerClient.installReferrer
                val referrerUrl = response.installReferrer
                // Parse code={suffix} from referrerUrl
                // Example: "code=abc123" -> extract "abc123"
                val suffix = extractSuffixFromReferrer(referrerUrl)
                suffix?.let {
                    LimeLinkSDK.handleDeferredDeepLink(it) { uri ->
                        // Handle the URI
                    }
                }
            }
        }
        referrerClient.endConnection()
    }
    
    override fun onInstallReferrerServiceDisconnected() {
        // Handle disconnection
    }
})

private fun extractSuffixFromReferrer(referrerUrl: String): String? {
    // Parse code={suffix} parameter from referrer URL
    val params = referrerUrl.split("&")
    for (param in params) {
        if (param.startsWith("code=")) {
            return param.substring(5) // Remove "code=" prefix
        }
    }
    return null
}
```

### Best Practices

- **First Launch Detection**: Properly detect first app launch to avoid unnecessary API calls
- **Error Handling**: Always handle the case where suffix is not found or API call fails
- **Install Referrer**: Use Play Install Referrer Library for reliable referrer information
- **URI Handling**: Validate and sanitize the received URI before using it
- **Fallback**: Always provide a fallback navigation path when deferred deep link is not available
