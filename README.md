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
