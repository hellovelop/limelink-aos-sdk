# LimeLink Android SDK 설계 목적 및 내용

---

## 1. 설계 목적

### 1.1 SDK의 역할

LimeLink Android SDK는 **Dynamic Link(동적 링크) 처리**를 위한 클라이언트 라이브러리입니다.

핵심 역할:
- 웹 URL(Universal Link)을 앱 내 특정 화면으로 라우팅
- 앱 미설치 시 스토어 이동 후, 설치 완료 시 원래 의도한 화면으로 이동 (Deferred Deep Link)
- 링크 클릭/실행 통계 수집

### 1.2 해결하는 문제

| 문제 | SDK의 해결 방식 |
|------|----------------|
| Universal Link를 받아서 어디로 보낼지 모름 | 서버 API로 URL을 해석하여 최종 목적지 URI 반환 |
| 앱 설치 전 클릭한 링크 유실 | Install Referrer로 원래 링크를 복원 (Deferred Deep Link) |
| 링크 클릭 통계 추적 | 자동으로 stats API에 이벤트 전송 |
| Activity lifecycle마다 수동 호출 번거로움 | Lifecycle 자동 감지로 개발자 코드 최소화 |

### 1.3 설계 원칙

- **최소 침습**: `init()` 한 줄 + 리스너 등록만으로 동작
- **자동화 우선**: Lifecycle 감지, 통계 전송, Deferred Deeplink 모두 자동
- **하위호환**: 기존 API(`handleUniversalLink`, `saveLimeLinkStatus`) 유지
- **단일 진입점**: `LimeLinkSDK` 싱글턴 객체가 모든 기능의 파사드

---

## 2. 아키텍처

### 2.1 모듈 구조

```
LimeLinkSDK (Facade / Singleton)
│
├── LimeLinkConfig              설정 (Builder 패턴)
│
├── LimeLinkLifecycleHandler    Activity lifecycle 자동 감지
│   └── onCreated / onResumed → handleLinkIntent()
│
├── UniversalLinkHandler        Universal Link URL 해석
│   ├── 서브도메인 패턴 처리     {suffix}.limelink.org/link/{linkSuffix}
│   └── 레거시 딥링크 처리       deep.limelink.org/...
│
├── InstallReferrerHandler      Google Play Install Referrer
│   └── referrer에서 LimeLink URL 추출
│
├── UrlHandler                  URL 파싱 유틸리티
│   ├── resolveUri()            scheme original-url 우선 → fallback intent data
│   ├── parseQueryParams()      쿼리 파라미터 맵
│   └── parsePathParams()       경로 세그먼트 (mainPath, subPath)
│
├── LinkStats                   첫 실행 추적 + 통계 이벤트 전송
│
├── LimeLinkListener            콜백 인터페이스
│   ├── onDeeplinkReceived()    성공
│   └── onDeeplinkError()       실패
│
├── Response Models
│   ├── LimeLinkResult          통합 결과 모델
│   ├── LimeLinkError           에러 모델
│   ├── ReferrerInfo            리퍼러 정보
│   ├── PathParamResponse       경로 파라미터
│   ├── UniversalLinkResponse   API 응답 (uri)
│   └── DeeplinkResponse        API 응답 (deeplinkUrl)
│
└── RetrofitClient              네트워크 (Retrofit2 + Gson)
    └── ApiService              API 인터페이스
```

### 2.2 데이터 흐름

#### Universal Link 진입

```
사용자가 https://abc.limelink.org/link/campaign 클릭
    │
    ▼
Android OS가 앱의 Activity 실행 (intent.data = 위 URL)
    │
    ▼
LimeLinkLifecycleHandler.onActivityCreated()
    │  intent에서 Universal Link 감지
    ▼
LimeLinkSDK.handleLinkIntent(activity, intent)
    │
    ├─ UniversalLinkHandler.handleUniversalLink()
    │      서버 API 호출: GET /api/v1/app/dynamic_link/{linkSuffix}
    │      응답: { "uri": "myapp://product/123" }
    │
    ├─ UrlHandler.parseQueryParams() → {utm_source: "kakao"}
    ├─ UrlHandler.parsePathParams() → {mainPath: "campaign", subPath: null}
    │
    ├─ LimeLinkResult 생성
    │      originalUrl, resolvedUri, queryParams, pathParams, isDeferred=false
    │
    ├─ notifyListeners() → 앱의 onDeeplinkReceived() 호출
    │
    └─ internalSaveLimeLinkStatus() → 통계 API 자동 호출
           POST /api/v1/stats/event
```

#### Deferred Deep Link 진입

```
사용자가 링크 클릭 → 앱 미설치 → Play Store → 앱 설치 → 첫 실행
    │
    ▼
LimeLinkSDK.init() → deferredDeeplinkEnabled == true
    │
    ▼
checkDeferredDeeplink()
    │  LinkStats.isFirstLaunch() == true
    ▼
InstallReferrerHandler.getInstallReferrer()
    │  Play Install Referrer API 호출
    │  referrer 문자열에서 limelink.org URL 추출
    ▼
LimeLinkResult 생성 (isDeferred = true, referrerInfo 포함)
    │
    ▼
notifyListeners() → 앱의 onDeeplinkReceived() 호출
```

### 2.3 설계 결정 및 이유

| 결정 | 이유 |
|------|------|
| Singleton (`object`) | SDK 전역 상태 관리, 어디서든 접근 가능 |
| `SupervisorJob` + `Dispatchers.Main.immediate` | 자식 코루틴 실패가 다른 작업에 영향 안 줌, UI 스레드에서 콜백 |
| `ActivityLifecycleCallbacks` | 개발자가 각 Activity마다 코드를 넣을 필요 없음 |
| `lastIntentUri` 중복 방지 | `onResume`이 반복 호출되어도 같은 링크를 두 번 처리하지 않음 |
| Builder 패턴 (Config) | 필수/선택 파라미터 명확, 확장 용이 |
| `internal` 접근 제한 | SDK 내부 구현은 외부에 노출하지 않음 |
| `isDeferred` 필드로 구분 | 별도 콜백 분리 없이 하나의 리스너로 통합 처리 |
| 하위호환 메서드 유지 | 기존 사용자의 마이그레이션 부담 최소화 |

---

## 3. API 통신

### 3.1 엔드포인트

| 용도 | Method | Path | 비고 |
|------|--------|------|------|
| 통계 전송 | POST | `/api/v1/stats/event` | 링크 클릭 시 자동 |
| Universal Link 해석 | GET | `/api/v1/app/dynamic_link/{linkSuffix}` | 서브도메인 패턴 |
| 레거시 딥링크 해석 | GET | `/link` | `subdomain`, `path`, `platform` 파라미터 |
| Deferred Deep Link | GET | `/api/v1/app/dynamic_link/{suffix}` | `event_type=setup` |
| (Deprecated) | GET | `/universal-link/app/dynamic_link/{suffix}` | 사용하지 않음 |

### 3.2 네트워크 구현

- **Retrofit2** + **Gson Converter**
- Lazy initialization: 첫 API 호출 시 인스턴스 생성
- Base URL: `LimeLinkConfig`에서 주입, `RetrofitClient.initialize()`로 변경 가능
- 모든 API 호출은 `Dispatchers.IO`에서 실행

---

## 4. 보안 고려사항

| 항목 | 구현 |
|------|------|
| API Key 노출 방지 | `limelink.properties`를 `.gitignore`에 포함, `BuildConfig`로 주입 |
| 로그 제어 | `loggingEnabled` 설정으로 프로덕션에서 로그 비활성화 |
| apiKey 로그 마스킹 | 초기화 로그에서 앞 4자리만 출력 (`apiKey.take(4)***`) |
| ProGuard | 전체 클래스 keep 규칙 적용 (난독화 시 API 통신 안전) |

---

## 5. 의존성

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| AndroidX Core KTX | 1.13.1 | Kotlin 확장 함수 |
| AndroidX AppCompat | 1.7.0 | 하위 호환 |
| Material | 1.12.0 | UI 컴포넌트 |
| Retrofit2 | 2.11.0 | HTTP 클라이언트 |
| Retrofit2 Gson Converter | 2.11.0 | JSON 직렬화 |
| Kotlinx Coroutines Android | 1.7.3 | 비동기 처리 |
| Install Referrer | 2.2 | Google Play Install Referrer API |

---

## 6. 개발 이력

업계 표준(Branch.io, Adjust) 수준의 자동 초기화 패턴으로 개선한 과정입니다.

### Phase 1: 기반 타입 + 네트워크

| 단계 | 파일 | 내용 |
|------|------|------|
| 1 | `config/LimeLinkConfig.kt` (신규) | Builder 패턴 SDK 설정 클래스 |
| 2 | `service/RetrofitClient.kt` (수정) | `by lazy` → nullable var + `initialize()` 메서드, baseUrl 설정 가능 |
| 3 | 응답/콜백 타입 4개 (신규) | `LimeLinkResult`, `LimeLinkError`, `ReferrerInfo`, `LimeLinkListener` |

### Phase 2: Lifecycle 자동화

| 단계 | 파일 | 내용 |
|------|------|------|
| 4 | `lifecycle/LimeLinkLifecycleHandler.kt` (신규) | `ActivityLifecycleCallbacks` — `onCreated`/`onResumed`에서 자동 감지 |
| 5 | `LimeLinkSDK.kt` (대폭 수정) | `init()`, listener 관리, `handleLinkIntent()`, 하위호환 메서드 유지 |
| 6 | `LinkStats.kt` (수정) | `saveLimeLinkStatus()` deprecated, `internalSaveLimeLinkStatus()` 추가 |

### Phase 3: Install Referrer + Deferred Deep Link

| 단계 | 파일 | 내용 |
|------|------|------|
| 7 | `build.gradle` (수정) | `installreferrer:2.2` 의존성 추가 |
| 8 | `InstallReferrerHandler.kt` (신규) | Install Referrer 연결 → referrer에서 LimeLink URL 추출 |
| 9 | `LimeLinkSDK.kt` (연결) | `checkDeferredDeeplink()` — 첫 실행 + referrer → `LimeLinkResult(isDeferred=true)` |

### Phase 4: Example App

| 단계 | 파일 | 내용 |
|------|------|------|
| 10 | `limelink.properties.example` | API 키 설정 템플릿 |
| 11 | `build.gradle.kts` | BuildConfig 필드 4개 주입 |
| 12 | `AndroidManifest.xml` | intent-filter, INTERNET 권한, Application 클래스 |
| 13 | `LimeLinkDemoApp.kt` (신규) | `LimeLinkSDK.init()` 호출 |
| 14 | `strings.xml` | UI 문자열 리소스 17개 |
| 15 | `MainActivity.kt` (재작성) | 6-카드 데모 UI (Compose + Material3) |

**핵심 원칙**: 기존 public API 5개 메서드 시그니처 100% 하위 호환 유지
