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
    implementation 'com.github.hellovelope:limelink-aos-sdk:0.1.0'
}
```
- Please refer to [*here](https://jitpack.io/#hellovelope/limelink-aos-sdk/0.1.0) for **maven**, **sbt**, or **leiningen**.

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

> 상세 가이드는 [docs/SDK_GUIDE.md](docs/SDK_GUIDE.md)를 참조하세요.

## Quick Start

### 1. SDK 초기화

`Application` 클래스에서 한 번만 호출합니다.

```kt
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.config.LimeLinkConfig

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = LimeLinkConfig.Builder("YOUR_API_KEY")
            .setLogging(true)                    // 디버그 로그 (기본: false)
            .setDeferredDeeplinkEnabled(true)     // Deferred Deeplink 자동 체크 (기본: true)
            .build()

        LimeLinkSDK.init(this, config)
    }
}
```

### 2. 딥링크 수신 (Listener)

`init()` 호출 후 Lifecycle 자동 감지가 활성화되므로, 리스너만 등록하면 됩니다.

```kt
import org.limelink.limelink_aos_sdk.LimeLinkListener
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.response.LimeLinkResult
import org.limelink.limelink_aos_sdk.response.LimeLinkError

class MainActivity : ComponentActivity() {

    private val linkListener = object : LimeLinkListener {
        override fun onDeeplinkReceived(result: LimeLinkResult) {
            val uri = result.resolvedUri           // API가 해석한 최종 URI
            val isDeferred = result.isDeferred     // Deferred Deeplink 여부
            val query = result.queryParams         // 쿼리 파라미터 맵
            val mainPath = result.pathParams.mainPath
            val subPath = result.pathParams.subPath

            // 앱 내 화면 이동 등 처리
        }

        override fun onDeeplinkError(error: LimeLinkError) {
            Log.e("Deeplink", "[${error.code}] ${error.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LimeLinkSDK.addLinkListener(linkListener)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)  // Activity의 intent 갱신
    }

    override fun onDestroy() {
        super.onDestroy()
        LimeLinkSDK.removeLinkListener(linkListener)
    }
}
```

### 3. AndroidManifest 설정

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop">

    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="*.limelink.org"
            android:pathPrefix="/link/" />
    </intent-filter>
</activity>
```

## Universal Link Flow

1. User clicks a URL in the format `https://{subdomain}.limelink.org/link/{link_suffix}?key1=val1&key2=val2`
2. SDK extracts the `{subdomain}` and `{link_suffix}` parts from the URL
3. SDK extracts the full original URL (including query parameters) as `full_request_url`
4. Calls the API `https://www.limelink.org/api/v1/app/dynamic_link/{link_suffix}?full_request_url={full_request_url}&key1=val1&key2=val2` with all query parameters
5. Receives `uri` from API response
6. Delivers the result via `LimeLinkListener.onDeeplinkReceived()`

## Deferred Deep Link

앱 설치 전 클릭한 링크를 설치 후 첫 실행 시 복원합니다.

- `LimeLinkConfig.deferredDeeplinkEnabled = true` (기본값)이면 `init()` 시 자동으로 Install Referrer를 확인
- 첫 실행 여부는 SDK가 `SharedPreferences`로 자동 관리
- 결과는 `LimeLinkListener.onDeeplinkReceived()`로 전달 (`result.isDeferred == true`)

```kt
override fun onDeeplinkReceived(result: LimeLinkResult) {
    if (result.isDeferred) {
        // 앱 설치 후 첫 실행 - 설치 전 클릭한 링크 복원
        val referrer = result.referrerInfo
        Log.d("Deferred", "referrer url: ${referrer?.limeLinkUrl}")
        Log.d("Deferred", "query params: ${referrer?.limeLinkDetail?.queryParams}")
    }
}
```

> Install Referrer 상세 정보, 수동 호출 방법 등은 [docs/SDK_GUIDE.md](docs/SDK_GUIDE.md) 참조.
