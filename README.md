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
4. Calls the API `https://www.limelink.org/api/v1/dynamic_link/{link_suffix}` with headers
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

### Check Token Availability

Before creating a deferred deep link, you can check if a token is already in use:

```kt
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.response.CheckTokenResponse

// Check if token exists
LimeLinkSDK.checkDeferredDeepLinkToken("your-token") { result ->
    result.onSuccess { response ->
        if (response.isExist) {
            println("Token is already in use")
        } else {
            println("Token is available")
        }
    }.onFailure { error ->
        println("Error checking token: ${error.message}")
    }
}
```

### Create Deferred Deep Link

Create a new deferred deep link with the following parameters:

```kt
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.request.DeferredDeepLinkRequest
import org.limelink.limelink_aos_sdk.response.DeferredDeepLinkResponse

val request = DeferredDeepLinkRequest(
    projectId = "your-project-id",
    name = "Promotion Campaign",
    token = "promo-2024", // Optional: unique token (auto-generated if not provided)
    parameters = mapOf(
        "campaign" to "summer-sale",
        "discount" to "20",
        "page" to "product-detail"
    ),
    iosAppStoreUrl = "https://apps.apple.com/app/your-app-id",
    androidPlayStoreUrl = "https://play.google.com/store/apps/details?id=com.yourapp",
    fallbackUrl = "https://yourwebsite.com/promo"
)

LimeLinkSDK.createDeferredDeepLink(request) { result ->
    result.onSuccess { response ->
        println("Deferred deep link created: ${response.id}")
        println("Token: ${response.token}")
    }.onFailure { error ->
        println("Error creating deferred deep link: ${error.message}")
    }
}
```

### Get Deferred Deep Links List

Retrieve a paginated list of deferred deep links for a project:

```kt
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.request.DeferredDeepLinksRequest
import org.limelink.limelink_aos_sdk.response.DeferredDeepLinksResponse

val request = DeferredDeepLinksRequest(
    limit = 10,
    keyword = "promo", // Optional: search keyword
    startId = null, // Optional: for pagination
    startCreatedAt = null // Optional: for pagination
)

LimeLinkSDK.getDeferredDeepLinks("your-project-id", request) { result ->
    result.onSuccess { response ->
        response.items.forEach { link ->
            println("Link: ${link.name} - Token: ${link.token}")
        }
        
        // Check if there are more items
        response.lastEvaluatedKey?.let { lastKey ->
            // Use lastKey.id and lastKey.createdAt for next page
            println("More items available. Last ID: ${lastKey.id}")
        }
    }.onFailure { error ->
        println("Error getting deferred deep links: ${error.message}")
    }
}
```

### Get Deferred Deep Link by ID

Retrieve detailed information about a specific deferred deep link:

```kt
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.response.DeferredDeepLinkResponse

LimeLinkSDK.getDeferredDeepLinkById("deferred-deep-link-id") { result ->
    result.onSuccess { response ->
        println("Name: ${response.name}")
        println("Token: ${response.token}")
        println("Parameters: ${response.parameters}")
        println("Created at: ${response.createdAt}")
    }.onFailure { error ->
        println("Error getting deferred deep link: ${error.message}")
    }
}
```

### Update Deferred Deep Link

Update an existing deferred deep link:

```kt
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.request.DeferredDeepLinkRequest

val updateRequest = DeferredDeepLinkRequest(
    projectId = "your-project-id",
    name = "Updated Campaign Name",
    token = "promo-2024",
    parameters = mapOf(
        "campaign" to "winter-sale",
        "discount" to "30"
    ),
    androidPlayStoreUrl = "https://play.google.com/store/apps/details?id=com.yourapp"
)

LimeLinkSDK.updateDeferredDeepLink("deferred-deep-link-id", updateRequest) { result ->
    result.onSuccess { response ->
        println("Deferred deep link updated: ${response.id}")
    }.onFailure { error ->
        println("Error updating deferred deep link: ${error.message}")
    }
}
```

### Delete Deferred Deep Link

Delete a deferred deep link:

```kt
import org.limelink.limelink_aos_sdk.LimeLinkSDK

LimeLinkSDK.deleteDeferredDeepLink("deferred-deep-link-id") { result ->
    result.onSuccess {
        println("Deferred deep link deleted successfully")
    }.onFailure { error ->
        println("Error deleting deferred deep link: ${error.message}")
    }
}
```

### Get Deferred Deep Link by Token (First Launch)

This is the most important method for deferred deep links. Use it when the app is launched for the first time after installation to retrieve the stored link information:

```kt
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.response.GetDeferredDeepLinkByTokenResponse

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Check if this is the first launch
        if (isFirstLaunch()) {
            // Get the token from your tracking system or URL
            val token = getTokenFromInstallSource() // You need to implement this
            
            // Retrieve deferred deep link information
            LimeLinkSDK.getDeferredDeepLinkByToken(token) { result ->
                result.onSuccess { response ->
                    // Handle the deferred deep link
                    response.parameters?.let { params ->
                        // Navigate to specific screen based on parameters
                        val campaign = params["campaign"] as? String
                        val page = params["page"] as? String
                        
                        when (page) {
                            "product-detail" -> {
                                // Navigate to product detail page
                                navigateToProductDetail(params)
                            }
                            "home" -> {
                                // Navigate to home with campaign info
                                navigateToHome(campaign)
                            }
                            else -> {
                                // Default navigation
                                navigateToDefault()
                            }
                        }
                    }
                    
                    // You can also use the store URLs if needed
                    response.androidPlayStoreUrl?.let { playStoreUrl ->
                        // Handle Play Store URL if needed
                    }
                    
                    response.fallbackUrl?.let { fallbackUrl ->
                        // Handle fallback URL if needed
                    }
                }.onFailure { error ->
                    // Token not found or error occurred
                    println("No deferred deep link found or error: ${error.message}")
                    // Proceed with normal app launch
                    navigateToDefault()
                }
            }
        } else {
            // Normal app launch
            navigateToDefault()
        }
    }
    
    private fun isFirstLaunch(): Boolean {
        // Implement your first launch detection logic
        // You can use SharedPreferences or other methods
        return true // Placeholder
    }
    
    private fun getTokenFromInstallSource(): String {
        // Implement logic to get token from:
        // - Install referrer
        // - URL parameters
        // - Tracking system
        // - etc.
        return "your-token" // Placeholder
    }
}
```

### Deferred Deep Link Flow

1. **User clicks a link** before installing the app
2. **Link redirects to app store** (Play Store or App Store)
3. **User installs and opens the app**
4. **App retrieves the token** from install referrer or tracking system
5. **App calls `getDeferredDeepLinkByToken()`** with the token
6. **SDK returns stored parameters** and URLs
7. **App navigates to the appropriate screen** based on the parameters

### Best Practices

- **Token Management**: Use unique, meaningful tokens for each campaign
- **Error Handling**: Always handle the case where a token is not found
- **First Launch Detection**: Properly detect first app launch to avoid unnecessary API calls
- **Parameter Validation**: Validate and sanitize parameters before using them
- **Fallback URLs**: Always provide fallback URLs for better user experience
