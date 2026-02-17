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
        google()
        mavenCentral()
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

> JitPack은 git 태그 기반으로 버전을 배포합니다. 최신 버전은 [JitPack 페이지](https://jitpack.io/#hellovelope/limelink-aos-sdk)에서 확인하세요.

### Maven Local (로컬 개발용)

```bash
# SDK 프로젝트 루트에서
./gradlew publishToMavenLocal
```

```groovy
// 앱 build.gradle
repositories {
    mavenLocal()
}
dependencies {
    implementation 'org.limelink:limelink_aos_sdk:0.1.0'
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
        val referrer = result.referrerInfo
        Log.d("Deferred", "referrer url: ${referrer?.limeLinkUrl}")
        Log.d("Deferred", "query params: ${referrer?.limeLinkDetail?.queryParams}")
    } else {
        // 앱 실행 중 Universal Link 클릭으로 진입
    }
}
```

---

## 5. Deferred Deep Link

앱을 설치하기 전에 클릭한 링크를 설치 후 첫 실행 시 복원합니다.

### 자동 처리 (기본 동작)

- `LimeLinkConfig.deferredDeeplinkEnabled = true` (기본값)
- `init()` 시 자동으로 Install Referrer를 확인하고 리스너로 전달
- 첫 실행 여부는 SDK가 `SharedPreferences`로 자동 관리

```kotlin
// Application.onCreate()에서 init() 호출만으로 자동 동작
LimeLinkSDK.init(this, config)

// 리스너에서 isDeferred로 구분
LimeLinkSDK.addLinkListener(object : LimeLinkListener {
    override fun onDeeplinkReceived(result: LimeLinkResult) {
        if (result.isDeferred) {
            // Install Referrer에서 복원된 딥링크
            navigateToContent(result.resolvedUri)
        }
    }
})
```

### 수동 호출

자동 처리와 별도로 직접 시점을 제어할 수 있습니다.

```kotlin
LimeLinkSDK.checkDeferredDeeplink(context) { result ->
    if (result != null) {
        Log.d("Deferred", "Found: ${result.originalUrl}")
    }
}
```

### 수동 Deferred Deep Link (suffix 직접 전달)

```kotlin
LimeLinkSDK.handleDeferredDeepLink(
    suffix = "campaign-xyz",
    fullRequestUrl = "https://abc.limelink.org/link/campaign-xyz"
) { resolvedUri ->
    resolvedUri?.let { navigateTo(it) }
}
```

---

## 6. Install Referrer

Install Referrer를 통해 앱 설치 경로 정보를 조회합니다.

### 기본 사용

```kotlin
LimeLinkSDK.getInstallReferrer(context) { referrerInfo ->
    if (referrerInfo != null) {
        Log.d("Referrer", "referrerUrl: ${referrerInfo.referrerUrl}")
        Log.d("Referrer", "limeLinkUrl: ${referrerInfo.limeLinkUrl}")
        Log.d("Referrer", "clickTimestamp: ${referrerInfo.clickTimestamp}")
        Log.d("Referrer", "installTimestamp: ${referrerInfo.installTimestamp}")
    }
}
```

### ReferrerInfo 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `referrerUrl` | String? | Play Store에서 제공하는 원본 referrer 문자열 |
| `clickTimestamp` | Long | 광고 클릭 시각 (초 단위) |
| `installTimestamp` | Long | 앱 설치 시작 시각 (초 단위) |
| `limeLinkUrl` | String? | referrer에서 추출한 LimeLink URL 문자열 |
| `limeLinkDetail` | LimeLinkUrl? | LimeLink URL의 상세 구조 (v0.1.0+) |

### LimeLinkUrl 상세 정보 활용

`limeLinkDetail` 필드를 통해 referrer URL의 구조화된 정보에 접근할 수 있습니다.

```kotlin
LimeLinkSDK.getInstallReferrer(context) { referrerInfo ->
    val detail = referrerInfo?.limeLinkDetail
    if (detail != null) {
        Log.d("Detail", "url: ${detail.url}")               // 쿼리 제외 URL
        Log.d("Detail", "fullUrl: ${detail.fullUrl}")        // 쿼리 포함 전체 URL
        Log.d("Detail", "queryString: ${detail.queryString}")// 원본 쿼리 문자열
        Log.d("Detail", "queryParams: ${detail.queryParams}")// 쿼리 파라미터 맵
        Log.d("Detail", "referrer: ${detail.referrer}")      // 원본 referrer 문자열

        // 예: https://abc.limelink.org/link/test?utm_source=kakao&campaign=summer
        // detail.url         = "https://abc.limelink.org/link/test"
        // detail.fullUrl     = "https://abc.limelink.org/link/test?utm_source=kakao&campaign=summer"
        // detail.queryString = "utm_source=kakao&campaign=summer"
        // detail.queryParams = {utm_source=kakao, campaign=summer}
    }
}
```

### LimeLinkUrl 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `referrer` | String | 원본 referrer 문자열 전체 |
| `url` | String | 쿼리 스트링 제외 URL |
| `fullUrl` | String | 쿼리 스트링 포함 전체 URL |
| `queryString` | String? | 쿼리 문자열 (`?` 이후 부분, 없으면 null) |
| `queryParams` | Map<String, String> | 쿼리 파라미터 맵 |

---

## 7. URL 파싱 유틸리티

### Universal Link 확인

```kotlin
val isUL = LimeLinkSDK.isUniversalLink(intent)
```

### Deprecated 메서드

> Listener 방식에서는 `LimeLinkResult`에 모든 파싱 결과가 포함되므로, 아래 메서드들은 별도 호출 불필요.
> `init()` + Listener 방식을 사용하지 않는 하위호환 환경에서만 사용합니다.

```kotlin
val intent = activity.intent

// Scheme에서 original URL 추출 (Deprecated)
val originalUrl = LimeLinkSDK.getSchemeFromIntent(intent)

// 쿼리 파라미터 (Deprecated)
val params = LimeLinkSDK.parseQueryParams(intent)

// 경로 파라미터 (Deprecated)
val pathParams = LimeLinkSDK.parsePathParams(intent)
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
| `LimeLinkSDK.checkDeferredDeeplink(context, callback?)` | Deferred Deeplink 확인 (init에서 자동 호출) |
| `LimeLinkSDK.handleDeferredDeepLink(suffix, fullRequestUrl?, callback?)` | 수동 Deferred Deep Link 처리 |
| `LimeLinkSDK.getInstallReferrer(context, callback)` | Install Referrer 정보 조회 |
| `LimeLinkSDK.isUniversalLink(intent)` | Universal Link 여부 확인 |

### 데이터 클래스

| 클래스 | 설명 |
|--------|------|
| `LimeLinkConfig` | SDK 설정 (Builder 패턴) |
| `LimeLinkResult` | 딥링크 처리 결과 |
| `LimeLinkError` | 오류 정보 (`code`, `message`, `exception?`) |
| `ReferrerInfo` | Install Referrer 정보 |
| `LimeLinkUrl` | referrer URL 상세 구조 (v0.1.0+) |
| `PathParamResponse` | 경로 파라미터 (`mainPath`, `subPath?`) |

### Deprecated 예정 API (v1.0.0에서 제거)

| 메서드 | 대체 |
|--------|------|
| `handleUniversalLink(context, intent, callback?)` | `init()` + `addLinkListener()` |
| `getSchemeFromIntent(intent)` | `LimeLinkResult.originalUrl` |
| `parseQueryParams(intent)` | `LimeLinkResult.queryParams` |
| `parsePathParams(intent)` | `LimeLinkResult.pathParams` |

> 상세 마이그레이션 방법은 [DEPRECATED.md](DEPRECATED.md) 참조.

---

## 10. 지원 URL 패턴

### 서브도메인 패턴 (Primary)

```
https://{subdomain}.limelink.org/link/{linkSuffix}
https://{subdomain}.limelink.org/link/{linkSuffix}?key1=val1&key2=val2
```

### Legacy Deeplink

```
https://deep.limelink.org/{path}
```

### Custom Scheme

```
myapp://url?original-url={encoded_url}
```

---

## 11. Troubleshooting

### "Unresolved reference: LimeLinkSDK"

```bash
# JitPack 사용 시 — settings.gradle에 JitPack repository 추가 확인
# Maven Local 사용 시 — SDK 퍼블리시 확인
ls ~/.m2/repository/org/limelink/limelink_aos_sdk/0.1.0/

# 의존성 캐시 갱신
./gradlew clean --refresh-dependencies
```

### 딥링크가 수신되지 않음

1. `AndroidManifest.xml`에 intent-filter 설정 확인
2. `android:launchMode="singleTop"` 확인
3. `LimeLinkSDK.init()` 호출 여부 확인
4. `addLinkListener()` 등록 여부 확인
5. `setLogging(true)`로 SDK 로그 확인

### Deferred Deeplink가 동작하지 않음

1. `deferredDeeplinkEnabled = true` 확인 (기본값)
2. Play Store 설치 경로인지 확인 (직접 설치 시 Install Referrer 없음)
3. 에뮬레이터에서는 Install Referrer API가 제한될 수 있음

### 버전 호환성

| 항목 | 요구 버전 |
|------|-----------|
| compileSdk | 35 |
| minSdk | 24 |
| Kotlin | 2.0.0 |
| AGP | 8.7.0 |
| JDK | 17 |

---

## 12. 배포 (JitPack)

SDK는 JitPack을 통해 배포되며, **git 태그**가 버전 역할을 합니다.

### 릴리스 절차

```bash
# 1. 변경사항 커밋 & push
git add . && git commit -m "release: v0.1.0" && git push

# 2. 태그 생성 & push
git tag -a 0.1.0 -m "Release 0.1.0"
git push origin 0.1.0

# 3. JitPack 빌드 확인
# https://jitpack.io/#hellovelope/limelink-aos-sdk/0.1.0
```

### 소비자 설정

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

// build.gradle
dependencies {
    implementation 'com.github.hellovelope:limelink-aos-sdk:0.1.0'
}
```

> 태그 push 후 JitPack이 자동으로 빌드합니다. 빌드 상태는 `https://jitpack.io/#hellovelope/limelink-aos-sdk/{tag}` 에서 확인 가능합니다.
