# LimeLink Android SDK - Changelog

---

## 0.1.0

> **릴리스일**: 2026-02-18
> **하위호환**: 0.0.x 대비 Breaking Change 없음

### Install Referrer URL 상세 정보 (`LimeLinkUrl`)

Install Referrer에서 추출한 LimeLink URL을 구조화된 형태로 제공합니다.

#### 배경

기존 `ReferrerInfo.limeLinkUrl`은 `String?` 타입으로 URL 문자열만 반환했습니다.
referrer URL에 포함된 쿼리 파라미터(`utm_source`, `campaign` 등)를 활용하려면
소비자 측에서 직접 URL을 파싱해야 했습니다.

```kotlin
// 기존: URL 문자열만 제공
referrerInfo.limeLinkUrl  // "https://abc.limelink.org/link/test?utm_source=kakao&campaign=summer"
// → 쿼리 파라미터를 쓰려면 직접 Uri.parse() 후 추출해야 함
```

#### 변경 내용

##### 1. `LimeLinkUrl` 데이터 클래스 추가

`response/LimeLinkUrl.kt` (신규)

```kotlin
data class LimeLinkUrl(
    val referrer: String,              // 원본 referrer 문자열 전체
    val url: String,                   // 쿼리 제외 URL
    val fullUrl: String,               // 쿼리 포함 전체 URL
    val queryString: String?,          // 쿼리 문자열 (없으면 null)
    val queryParams: Map<String, String>  // 쿼리 파라미터 맵
)
```

**파싱 예시**:

| referrer | 필드 | 값 |
|----------|------|-----|
| `utm_source=google&url=https://abc.limelink.org/link/test?key1=val1&key2=val2` | `url` | `https://abc.limelink.org/link/test` |
| | `fullUrl` | `https://abc.limelink.org/link/test?key1=val1&key2=val2` |
| | `queryString` | `key1=val1&key2=val2` |
| | `queryParams` | `{key1=val1, key2=val2}` |

##### 2. `ReferrerInfo`에 `limeLinkDetail` 필드 추가

`response/ReferrerInfo.kt`

```kotlin
data class ReferrerInfo(
    val referrerUrl: String?,
    val clickTimestamp: Long,
    val installTimestamp: Long,
    val limeLinkUrl: String?,               // 기존 유지 (String?)
    val limeLinkDetail: LimeLinkUrl? = null  // 신규 (기본값 null)
)
```

- `limeLinkUrl`: 기존과 동일한 `String?` 타입 — **하위호환 유지**
- `limeLinkDetail`: 구조화된 `LimeLinkUrl?` — 기본값 `null`이므로 기존 코드에 영향 없음

##### 3. `InstallReferrerHandler` 메서드 추가

`InstallReferrerHandler.kt`

| 메서드 | 반환 타입 | 설명 |
|--------|-----------|------|
| `extractLimeLinkUrl()` | `String?` | 기존 유지 — URL 문자열 반환 |
| `extractLimeLinkUrlDetail()` | `LimeLinkUrl?` | **신규** — 구조화된 URL 정보 반환 |

`extractLimeLinkUrl()`은 내부적으로 `extractLimeLinkUrlDetail()?.fullUrl`에 위임합니다.

##### 4. `LimeLinkSDK.checkDeferredDeeplink()` 업데이트

Deferred Deeplink 처리 시 `limeLinkDetail`의 쿼리 파라미터를 `LimeLinkResult.queryParams`에 반영합니다.

```kotlin
// 내부 동작
val detail = referrerInfo.limeLinkDetail
val result = LimeLinkResult(
    originalUrl = referrerInfo.limeLinkUrl,        // String
    resolvedUri = Uri.parse(referrerInfo.limeLinkUrl),
    queryParams = detail?.queryParams ?: emptyMap(), // LimeLinkUrl에서 추출
    ...
)
```

#### 하위호환성

| 항목 | 호환 여부 | 설명 |
|------|:---------:|------|
| `ReferrerInfo.limeLinkUrl` | **호환** | `String?` 타입 유지 |
| `ReferrerInfo` 생성자 | **호환** | `limeLinkDetail`은 기본값 `null` — 기존 생성자 호출 그대로 동작 |
| `extractLimeLinkUrl()` | **호환** | 반환 타입 `String?` 유지 |
| `LimeLinkResult` | **호환** | 필드 추가/변경 없음 |

**기존 코드 (변경 불필요)**:
```kotlin
LimeLinkSDK.getInstallReferrer(context) { info ->
    info?.limeLinkUrl   // String? — 그대로 동작
}
```

**새 코드 (선택적 활용)**:
```kotlin
LimeLinkSDK.getInstallReferrer(context) { info ->
    info?.limeLinkUrl                       // "https://abc.limelink.org/link/test?k=v"
    info?.limeLinkDetail?.url               // "https://abc.limelink.org/link/test"
    info?.limeLinkDetail?.queryParams       // {k=v}
    info?.limeLinkDetail?.queryString       // "k=v"
}
```

#### 설계 결정: 왜 기존 필드 타입을 변경하지 않았는가

초기에는 `ReferrerInfo.limeLinkUrl` 타입을 `String?`에서 `LimeLinkUrl?`로 변경하는 방식으로 구현했으나, 다음과 같은 하위호환성 문제로 롤백했습니다:

| 문제 유형 | 영향 |
|-----------|------|
| **소스 비호환** | `info.limeLinkUrl ?: "-"` → 컴파일 에러 (`LimeLinkUrl`에 `String` 연산 불가) |
| **바이너리 비호환** | 기존 컴파일된 앱이 `getString` 시그니처를 찾지 못해 `NoSuchMethodError` 발생 |
| **문서 불일치** | 기존 가이드의 `${it.limeLinkUrl}` 코드 예시가 모두 깨짐 |

**채택한 전략**: 기존 필드 보존 + 신규 필드 추가 (additive change)

```
ReferrerInfo.limeLinkUrl    → String?       (기존 유지)
ReferrerInfo.limeLinkDetail → LimeLinkUrl?  (신규, 기본값 null)
```

이 방식은 semver 0.x에서도 안전하며, 소비자가 점진적으로 새 API를 채택할 수 있습니다.

#### 수정 파일

| 파일 | 변경 유형 |
|------|-----------|
| `response/LimeLinkUrl.kt` | 신규 — 데이터 클래스 |
| `response/ReferrerInfo.kt` | 수정 — `limeLinkDetail` 필드 추가 |
| `InstallReferrerHandler.kt` | 수정 — `extractLimeLinkUrlDetail()` 추가, URL 파싱 로직 구현 |
| `LimeLinkSDK.kt` | 수정 — `checkDeferredDeeplink()`에서 `limeLinkDetail` 활용 |
| `InstallReferrerHandlerTest.kt` | 수정 — 양쪽 메서드 테스트 커버리지 |
| `DataClassTest.kt` | 수정 — `ReferrerInfo` 생성자 테스트 업데이트 |

---

### SDK 아키텍처 개편

#### Lifecycle 기반 자동 딥링크 처리

| 항목 | 내용 |
|------|------|
| `LimeLinkSDK.init()` | Application 레벨 초기화 + ActivityLifecycleCallbacks 등록 |
| `LimeLinkListener` | 딥링크 이벤트 콜백 인터페이스 |
| `LimeLinkLifecycleHandler` | `onActivityCreated` / `onActivityResumed`에서 Universal Link 자동 감지 |
| `LimeLinkConfig` | Builder 패턴 설정 (apiKey, baseUrl, logging, deferredDeeplink) |

#### Deferred Deeplink 자동 처리

- `init()` 시 `deferredDeeplinkEnabled = true`이면 Install Referrer 자동 확인
- `LinkStats.isFirstLaunch()`로 첫 실행 판단
- `LimeLinkResult.isDeferred = true`로 일반 딥링크와 구분

#### 결과 데이터 통합

| 클래스 | 용도 |
|--------|------|
| `LimeLinkResult` | 딥링크 처리 결과 (originalUrl, resolvedUri, queryParams, pathParams, isDeferred, referrerInfo) |
| `LimeLinkError` | 오류 정보 (code, message, exception) |

#### Deprecated 처리

기존 public API 5개에 `@Deprecated` 어노테이션 추가. 시그니처 변경 없음.

> 상세 마이그레이션 방법은 [DEPRECATED.md](DEPRECATED.md) 참조.

---

### 테스트 인프라 구축

#### 빌드 환경

- `build.gradle`에서 테스트 비활성화(`all { enabled = false }`) 제거
- 테스트 의존성 추가: Robolectric 4.11.1, MockWebServer 4.12.0, kotlinx-coroutines-test 1.7.3, mockito-kotlin 5.2.1

#### 테스트 커버리지 (93개 테스트 케이스)

| 카테고리 | 테스트 파일 | 케이스 |
|----------|-----------|:------:|
| Pure Logic | LimeLinkConfigTest, EventTypeTest, DataClassTest, InstallReferrerHandlerTest | 45 |
| Robolectric | UniversalLinkHandlerTest, UrlHandlerTest, LinkStatsTest, LimeLinkLifecycleHandlerTest, LimeLinkSDKTest | 27 |
| MockWebServer | ApiServiceTest, RetrofitClientTest, UniversalLinkHandlerNetworkTest, LinkStatsNetworkTest | 21 |
| **합계** | **13개 파일** | **93** |

---

### Example App

Jetpack Compose 기반 데모 앱 추가 (`example/` 디렉토리).

- SDK 초기화, 리스너 등록, Deferred Deeplink, URL 파싱 시뮬레이션
- `adb shell am start`로 Universal Link 테스트 가능

> 상세 사용법은 [EXAMPLE_GUIDE.md](EXAMPLE_GUIDE.md) 참조.

---

### 기타 변경

- `UniversalLinkHandler`: 서브도메인 헤더 fetch 제거, 쿼리 파라미터 직접 전달
- `RetrofitClient`: `lazy` → getter + `initialize()` 메서드 추가
- `ApiService.getUniversalLinkNew()`: `queryParams` 기본값 `null` → `emptyMap()`
- `CoroutineScope`: `Dispatchers.Main` → `SupervisorJob + Dispatchers.Main.immediate`
- `build.gradle`: Install Referrer 2.2, OkHttp 4.12.0 의존성 추가
- 버전: 0.0.1 → 0.1.0
