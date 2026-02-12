# LimeLink Android SDK - Deprecated 예정 항목

> 아래 항목들은 새 아키텍처(`LimeLinkSDK.init()` + Listener 방식)로 대체되었습니다.
> 현재 하위호환을 위해 유지 중이며, **향후 메이저 버전(1.0.0)에서 제거 예정**입니다.

---

## 1. Deprecated 함수

### `saveLimeLinkStatus()`

| 항목 | 내용 |
|------|------|
| **위치** | `LinkStats.kt` (top-level function) |
| **시그니처** | `suspend fun saveLimeLinkStatus(context: Context, intent: Intent, privateKey: String)` |
| **이유** | `LimeLinkSDK.init()` 후 링크 수신 시 자동으로 통계 전송됨 |
| **대체** | `LimeLinkSDK.init(app, config)` — `handleLinkIntent()` 내부에서 자동 호출 |
| **상태** | `@Deprecated` 어노테이션 적용 완료 |

**마이그레이션 방법**:
```kotlin
// Before (deprecated)
saveLimeLinkStatus(context, intent, "your_api_key")

// After
// init()만 하면 자동 처리됨, 별도 호출 불필요
LimeLinkSDK.init(application, config)
```

---

### `LimeLinkSDK.handleUniversalLink()`

| 항목 | 내용 |
|------|------|
| **위치** | `LimeLinkSDK.kt` |
| **시그니처** | `fun handleUniversalLink(context: Context, intent: Intent, callback: ((String?) -> Unit)?)` |
| **이유** | `LimeLinkLifecycleHandler`가 Activity lifecycle에서 자동 감지하여 처리 |
| **대체** | `LimeLinkSDK.init()` + `addLinkListener()` |
| **상태** | Deprecated 예정 (어노테이션 추가됨) |

**마이그레이션 방법**:
```kotlin
// Before (deprecated) — 직접 호출 방식
override fun onCreate(savedInstanceState: Bundle?) {
    LimeLinkSDK.handleUniversalLink(this, intent) { uri ->
        // 처리
    }
}
override fun onNewIntent(intent: Intent) {
    LimeLinkSDK.handleUniversalLink(this, intent) { uri ->
        // 처리
    }
}

// After — 리스너 방식 (init 후 자동)
LimeLinkSDK.addLinkListener(object : LimeLinkListener {
    override fun onDeeplinkReceived(result: LimeLinkResult) {
        val uri = result.resolvedUri  // 해석된 URI
    }
})
```

---

### `LimeLinkSDK.getSchemeFromIntent()`

| 항목 | 내용 |
|------|------|
| **위치** | `LimeLinkSDK.kt` |
| **시그니처** | `fun getSchemeFromIntent(intent: Intent): String?` |
| **이유** | Listener의 `LimeLinkResult.originalUrl`로 동일 정보 제공 |
| **대체** | `LimeLinkResult.originalUrl` |
| **상태** | Deprecated 예정 (어노테이션 추가됨) |

---

### `LimeLinkSDK.parseQueryParams()`

| 항목 | 내용 |
|------|------|
| **위치** | `LimeLinkSDK.kt` |
| **시그니처** | `fun parseQueryParams(intent: Intent): Map<String, String>` |
| **이유** | Listener의 `LimeLinkResult.queryParams`로 동일 정보 제공 |
| **대체** | `LimeLinkResult.queryParams` |
| **상태** | Deprecated 예정 (어노테이션 추가됨) |

---

### `LimeLinkSDK.parsePathParams()`

| 항목 | 내용 |
|------|------|
| **위치** | `LimeLinkSDK.kt` |
| **시그니처** | `fun parsePathParams(intent: Intent): PathParamResponse` |
| **이유** | Listener의 `LimeLinkResult.pathParams`로 동일 정보 제공 |
| **대체** | `LimeLinkResult.pathParams` |
| **상태** | Deprecated 예정 (어노테이션 추가됨) |

---

## 2. Deprecated API 엔드포인트

### `getUniversalLink()` (ApiService)

| 항목 | 내용 |
|------|------|
| **위치** | `ApiService.kt` |
| **엔드포인트** | `GET /universal-link/app/dynamic_link/{suffix}` |
| **이유** | 새 엔드포인트 `/api/v1/app/dynamic_link/{linkSuffix}`로 대체 |
| **대체** | `getUniversalLinkNew()` |
| **상태** | Deprecated 예정 (어노테이션 추가됨) |

---

## 3. 요약 테이블

| 항목 | 유형 | 대체 | 제거 예정 |
|------|------|------|-----------|
| `saveLimeLinkStatus()` | Top-level 함수 | `init()` 자동 처리 | v1.0.0 |
| `handleUniversalLink()` | SDK 메서드 | `init()` + Listener | v1.0.0 |
| `getSchemeFromIntent()` | SDK 메서드 | `LimeLinkResult.originalUrl` | v1.0.0 |
| `parseQueryParams()` | SDK 메서드 | `LimeLinkResult.queryParams` | v1.0.0 |
| `parsePathParams()` | SDK 메서드 | `LimeLinkResult.pathParams` | v1.0.0 |
| `getUniversalLink()` | API 엔드포인트 | `getUniversalLinkNew()` | v1.0.0 |

---

## 4. 마이그레이션 가이드 (요약)

### Before (v0.1.0 이전 방식)

```kotlin
// Application에서 초기화 없음

// 각 Activity에서 수동 호출
class MyActivity : Activity() {
    override fun onCreate(...) {
        LimeLinkSDK.handleUniversalLink(this, intent) { uri -> ... }
        saveLimeLinkStatus(this, intent, "api_key")
    }
    override fun onNewIntent(intent: Intent) {
        LimeLinkSDK.handleUniversalLink(this, intent) { uri -> ... }
    }
    // 파싱도 직접
    val params = LimeLinkSDK.parseQueryParams(intent)
}
```

### After (v0.1.0 권장 방식)

```kotlin
// Application에서 1회 초기화
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = LimeLinkConfig.Builder("api_key").build()
        LimeLinkSDK.init(this, config)
    }
}

// Activity에서 리스너만 등록
class MyActivity : Activity() {
    private val listener = object : LimeLinkListener {
        override fun onDeeplinkReceived(result: LimeLinkResult) {
            // result에 모든 정보 포함
            // originalUrl, resolvedUri, queryParams, pathParams, isDeferred
        }
    }

    override fun onCreate(...) {
        LimeLinkSDK.addLinkListener(listener)
    }

    override fun onDestroy() {
        LimeLinkSDK.removeLinkListener(listener)
    }
}
```
