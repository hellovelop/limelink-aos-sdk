# LimeLink Android SDK 사용 가이드

> **SDK Version**: 0.1.0
> **Min SDK**: 24 | **Compile SDK**: 35

---

## 1. 설치

### JitPack (권장)

**프로젝트 `settings.gradle`**:
```groovy
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

**모듈 `build.gradle`**:
```groovy
dependencies {
    implementation 'com.github.hellovelope:limelink-aos-sdk:0.1.0'
}
```

### Maven Local (로컬 개발용)

```bash
# SDK 프로젝트 루트에서
./gradlew publishToMavenLocal
```

```kotlin
// 앱 build.gradle.kts
dependencies {
    implementation("org.limelink:limelink_aos_sdk:0.1.0")
}
```

---

## 2. SDK 초기화

`Application` 클래스의 `onCreate()`에서 **한 번만** 호출합니다.

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = LimeLinkConfig.Builder("YOUR_API_KEY")
            .setLogging(true)                    // 디버그 로그 (기본: false)
            .setDeferredDeeplinkEnabled(true)     // Deferred Deeplink 자동 체크 (기본: true)
            // .setBaseUrl("https://custom.api.com/")  // 커스텀 서버 (기본: https://limelink.org/)
            .build()

        LimeLinkSDK.init(this, config)
    }
}
```

### Config 옵션

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| `apiKey` | String | (필수) | LimeLink API 키 |
| `baseUrl` | String | `https://limelink.org/` | API 서버 URL |
| `loggingEnabled` | Boolean | `false` | SDK 내부 디버그 로그 출력 |
| `deferredDeeplinkEnabled` | Boolean | `true` | 앱 첫 설치 시 Deferred Deeplink 자동 확인 |

---

## 3. AndroidManifest 설정

```xml
<uses-permission android:name="android.permission.INTERNET" />

<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop">

    <!-- Universal Link (서브도메인 패턴) -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="*.limelink.org"
            android:pathPrefix="/link/" />
    </intent-filter>

    <!-- Legacy Deeplink -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="deep.limelink.org" />
    </intent-filter>
</activity>
```

> `android:launchMode="singleTop"` 필수 — 이미 실행 중인 Activity에서 새 링크를 `onNewIntent`로 받기 위함.

---

## 4. 딥링크 수신 (Listener 방식 - 권장)

`init()` 호출 후 Lifecycle 자동 감지가 활성화되므로, 리스너만 등록하면 됩니다.

```kotlin
class MainActivity : ComponentActivity() {

    private val linkListener = object : LimeLinkListener {
        override fun onDeeplinkReceived(result: LimeLinkResult) {
            val url = result.resolvedUri       // API가 해석한 최종 URI
            val isDeferred = result.isDeferred // Deferred Deeplink 여부
            val query = result.queryParams     // 쿼리 파라미터 맵
            val mainPath = result.pathParams.mainPath
            val subPath = result.pathParams.subPath

            // 앱 내 화면 이동 등 처리
            navigateTo(mainPath, query)
        }

        override fun onDeeplinkError(error: LimeLinkError) {
            Log.e("Deeplink", "[${error.code}] ${error.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LimeLinkSDK.addLinkListener(linkListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        LimeLinkSDK.removeLinkListener(linkListener)
    }
}
```

### LimeLinkResult 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `originalUrl` | String? | 원본 URL |
| `resolvedUri` | Uri? | API가 해석한 최종 리다이렉트 URI |
| `queryParams` | Map<String, String> | URL 쿼리 파라미터 |
| `pathParams` | PathParamResponse | 경로 파라미터 (`mainPath`, `subPath`) |
| `isDeferred` | Boolean | Deferred Deeplink 여부 (기본 `false`) |
| `referrerInfo` | ReferrerInfo? | Install Referrer 정보 (Deferred일 때) |

### Universal Link vs Deferred Deeplink 구분

```kotlin
override fun onDeeplinkReceived(result: LimeLinkResult) {
    if (result.isDeferred) {
        // 앱 설치 후 첫 실행 - 설치 전 클릭한 링크 복원
    } else {
        // 앱 실행 중 Universal Link 클릭으로 진입
    }
}
```

---

## 5. 수동 Universal Link 처리 (하위호환, Deprecated 예정)

Lifecycle 자동 감지를 사용하지 않고 직접 호출하는 경우:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleIncomingLink(intent)
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIncomingLink(intent)
}

private fun handleIncomingLink(intent: Intent) {
    LimeLinkSDK.handleUniversalLink(this, intent) { resolvedUri ->
        if (resolvedUri != null) {
            // 리다이렉트 처리
        }
    }
}
```

---

## 6. Deferred Deep Link

앱을 설치하기 전에 클릭한 링크를 설치 후 첫 실행 시 복원합니다.

**자동 처리** (기본 동작):
- `LimeLinkConfig.deferredDeeplinkEnabled = true` (기본값)
- `init()` 시 자동으로 Install Referrer를 확인하고 리스너로 전달

**수동 호출**:
```kotlin
// suffix와 fullRequestUrl을 직접 전달
LimeLinkSDK.handleDeferredDeepLink(
    suffix = "campaign-xyz",
    fullRequestUrl = "https://abc.limelink.org/link/campaign-xyz"
) { resolvedUri ->
    resolvedUri?.let { navigateTo(it) }
}
```

**Install Referrer 직접 조회**:
```kotlin
LimeLinkSDK.getInstallReferrer(context) { referrerInfo ->
    referrerInfo?.let {
        Log.d("Referrer", "url: ${it.referrerUrl}")
        Log.d("Referrer", "limelink: ${it.limeLinkUrl}")
        Log.d("Referrer", "click: ${it.clickTimestamp}, install: ${it.installTimestamp}")
    }
}
```

---

## 7. URL 파싱 유틸리티 (Deprecated 예정)

> Listener 방식에서는 `LimeLinkResult`에 모든 파싱 결과가 포함되므로, 아래 메서드들은 별도 호출 불필요.
> `init()` + Listener 방식을 사용하지 않는 하위호환 환경에서만 사용합니다.

```kotlin
val intent = activity.intent

// Universal Link 여부 확인
val isUL = LimeLinkSDK.isUniversalLink(intent)

// Scheme에서 original URL 추출
val originalUrl = LimeLinkSDK.getSchemeFromIntent(intent)

// 쿼리 파라미터
val params = LimeLinkSDK.parseQueryParams(intent)
// 예: {utm_source=kakao, campaign=summer}

// 경로 파라미터
val pathParams = LimeLinkSDK.parsePathParams(intent)
// 예: mainPath="toggle-reward", subPath="detail"
```

---

## 8. ProGuard / R8

SDK에 이미 ProGuard 규칙이 포함되어 있어 별도 설정 불필요합니다.

포함된 규칙:
```
-keep class org.limelink.limelink_aos_sdk.** { *; }
```

---

## 9. 전체 Public API 요약

### 권장 API (v0.1.0+)

| 메서드 | 설명 |
|--------|------|
| `LimeLinkSDK.init(app, config)` | SDK 초기화 (앱 시작 시 1회) |
| `LimeLinkSDK.addLinkListener(listener)` | 딥링크 이벤트 리스너 등록 |
| `LimeLinkSDK.removeLinkListener(listener)` | 리스너 제거 |
| `LimeLinkSDK.handleDeferredDeepLink(suffix, fullRequestUrl?, callback?)` | 수동 Deferred Deep Link 처리 |
| `LimeLinkSDK.checkDeferredDeeplink(context, callback?)` | Deferred Deeplink 확인 (init에서 자동 호출) |
| `LimeLinkSDK.getInstallReferrer(context, callback)` | Install Referrer 정보 조회 |
| `LimeLinkSDK.isUniversalLink(intent)` | Universal Link 여부 확인 |

### Deprecated 예정 API (v1.0.0에서 제거)

| 메서드 | 대체 |
|--------|------|
| `handleUniversalLink(context, intent, callback?)` | `init()` + `addLinkListener()` |
| `getSchemeFromIntent(intent)` | `LimeLinkResult.originalUrl` |
| `parseQueryParams(intent)` | `LimeLinkResult.queryParams` |
| `parsePathParams(intent)` | `LimeLinkResult.pathParams` |
| `saveLimeLinkStatus(context, intent, key)` | `init()` 자동 처리 |

> 상세 마이그레이션 방법은 [DEPRECATED.md](DEPRECATED.md) 참조.

---

## 10. Troubleshooting

### "Unresolved reference: LimeLinkSDK"

JitPack 또는 Maven Local에서 SDK를 찾지 못하는 경우:

```bash
# JitPack 사용 시 — settings.gradle에 JitPack repository 추가 확인
# Maven Local 사용 시 — SDK 퍼블리시 확인
ls ~/.m2/repository/org/limelink/limelink_aos_sdk/0.1.0/

# 의존성 캐시 갱신
./gradlew clean --refresh-dependencies
./gradlew :app:build
```

### 버전 호환성

| 항목 | 요구 버전 |
|------|-----------|
| compileSdk | 35 |
| minSdk | 24 |
| Kotlin | 2.0.0 |
| AGP | 8.7.0 |
