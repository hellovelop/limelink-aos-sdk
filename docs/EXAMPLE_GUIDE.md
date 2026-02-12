# LimeLink Example App 사용 가이드

> Example App은 LimeLink SDK의 모든 기능을 테스트할 수 있는 Compose 기반 데모 앱입니다.

---

## 1. 프로젝트 구조

```
example/
├── app/
│   ├── build.gradle.kts          # 앱 빌드 설정 (SDK 의존성, BuildConfig)
│   └── src/main/
│       ├── AndroidManifest.xml    # Universal Link intent-filter 설정
│       └── java/com/example/limelink/
│           ├── LimeLinkDemoApp.kt # Application - SDK 초기화
│           ├── MainActivity.kt    # 메인 화면 (모든 기능 테스트 UI)
│           └── ui/theme/          # Compose 테마
├── limelink.properties.example    # API 키 설정 템플릿
├── build.gradle.kts               # 루트 빌드 설정
└── settings.gradle.kts            # 모듈 설정
```

---

## 2. 환경 설정

### 2.1 API 키 설정

```bash
cd example/
cp limelink.properties.example limelink.properties
```

`limelink.properties` 파일을 열어 실제 값 입력:

```properties
LIMELINK_API_KEY=your_api_key_here
LIMELINK_PROJECT_ID=your_project_id_here
LIMELINK_CUSTOM_DOMAIN=your_subdomain.limelink.org
LIMELINK_ANDROID_APPLICATION_ID=com.example.limelink
```

> `limelink.properties`는 `.gitignore`에 포함되어 있어 저장소에 커밋되지 않습니다.

### 2.2 SDK 빌드 및 설치

Example App은 **Maven Local**에서 SDK를 가져옵니다. SDK를 먼저 빌드해야 합니다.

```bash
# 프로젝트 루트에서 (example/ 상위 디렉토리)
./rebuild-sdk.sh
```

또는 수동으로:

```bash
# 1. SDK를 Maven Local에 퍼블리시
./gradlew clean publishToMavenLocal

# 2. Example App 빌드
cd example/
./gradlew assembleDebug
```

### 2.3 Android Studio에서 열기

`example/` 디렉토리를 별도의 프로젝트로 Android Studio에서 엽니다.

> SDK 프로젝트(`limelink-aos-sdk/`)와 Example App(`example/`)은 별도의 Gradle 프로젝트입니다.

---

## 3. 앱 화면 구성

앱은 6개의 카드로 구성된 단일 화면입니다.

### 3.1 Config Card

현재 SDK에 설정된 값을 표시합니다.

| 항목 | 내용 |
|------|------|
| API Key | 앞 4자리만 표시 (마스킹) |
| Project ID | 프로젝트 ID |
| Custom Domain | 커스텀 도메인 |
| App ID | 앱 패키지명 |

### 3.2 Deeplink Card

Universal Link로 앱에 진입했을 때 수신된 정보를 표시합니다.

| 필드 | 설명 |
|------|------|
| Original URL | 원본 URL |
| Resolved URI | API가 해석한 최종 URI |
| Is Deferred | Deferred Deeplink 여부 |
| Query Params | 쿼리 파라미터 |
| Main Path | 경로 첫 번째 세그먼트 |
| Sub Path | 경로 두 번째 세그먼트 |

### 3.3 Deferred Deeplink Card

Install Referrer 정보를 표시합니다.

- **Check Referrer** 버튼: Install Referrer API를 호출하여 결과 표시
- referrer URL, 클릭/설치 타임스탬프, LimeLink URL 추출 결과

### 3.4 Simulation Card

URL을 직접 입력하여 SDK 파싱 결과를 테스트합니다.

- 기본값: `https://smaxh.limelink.org/link/test-suffix`
- URL을 입력하고 **Simulate** 버튼 클릭
- `isUniversalLink`, `scheme`, `queryParams`, `pathParams` 결과 출력
- Universal Link이면 `handleUniversalLink`까지 자동 호출

### 3.5 Stats Card

현재 intent 데이터로 통계 이벤트를 전송합니다.

- **Send Stats** 버튼: `saveLimeLinkStatus()` (deprecated API) 호출 테스트

### 3.6 Log Card

모든 SDK 이벤트와 동작 로그를 시간순(최신순)으로 표시합니다.

- 최대 50개 엔트리 표시
- **Clear Logs** 버튼으로 초기화

---

## 4. SDK 통합 코드 해설

### 4.1 초기화 (LimeLinkDemoApp.kt)

```kotlin
class LimeLinkDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = LimeLinkConfig.Builder(BuildConfig.LIMELINK_API_KEY)
            .setLogging(true)   // 디버그 로그 활성화
            .build()

        LimeLinkSDK.init(this, config)
    }
}
```

- `BuildConfig.LIMELINK_API_KEY`는 `limelink.properties`에서 읽어와 빌드 시 주입됨
- `init()` 호출 시 Activity lifecycle 자동 감지 시작

### 4.2 리스너 등록 (MainActivity.kt)

```kotlin
private val linkListener = object : LimeLinkListener {
    override fun onDeeplinkReceived(result: LimeLinkResult) {
        // Universal Link 또는 Deferred Deeplink 수신 시
        deeplinkResult.value = result
    }

    override fun onDeeplinkError(error: LimeLinkError) {
        // 에러 발생 시
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
```

### 4.3 onNewIntent 처리

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)  // Activity의 intent 갱신
}
```

- `launchMode="singleTop"`이므로 앱 실행 중 새 링크는 `onNewIntent`로 수신
- `setIntent(intent)` 호출 필수 — 이후 `activity.intent`로 접근 시 최신 intent 반영
- SDK의 `LimeLinkLifecycleHandler`가 `onActivityResumed`에서 자동 감지하므로 별도 SDK 호출 불필요

---

## 5. Universal Link 테스트 방법

### adb로 테스트

```bash
# 서브도메인 패턴
adb shell am start -a android.intent.action.VIEW \
  -d "https://smaxh.limelink.org/link/test-suffix?utm_source=test" \
  com.example.limelink

# 레거시 딥링크
adb shell am start -a android.intent.action.VIEW \
  -d "https://deep.limelink.org/some/path" \
  com.example.limelink
```

### 앱 내 Simulation

1. Simulation Card의 텍스트 필드에 URL 입력
2. **Simulate** 버튼 클릭
3. Log Card에서 파싱 결과 확인

---

## 6. 빌드 설정 참고

### BuildConfig 필드

`build.gradle.kts`에서 `limelink.properties`의 값을 `BuildConfig`에 주입합니다:

```kotlin
buildConfigField("String", "LIMELINK_API_KEY", "\"${limeLinkProps.getProperty("LIMELINK_API_KEY", "")}\"")
buildConfigField("String", "LIMELINK_PROJECT_ID", "\"${limeLinkProps.getProperty("LIMELINK_PROJECT_ID", "")}\"")
buildConfigField("String", "LIMELINK_CUSTOM_DOMAIN", "\"${limeLinkProps.getProperty("LIMELINK_CUSTOM_DOMAIN", "")}\"")
buildConfigField("String", "LIMELINK_ANDROID_APPLICATION_ID", "\"${limeLinkProps.getProperty("LIMELINK_ANDROID_APPLICATION_ID", "")}\"")
```

### SDK 의존성

```kotlin
implementation("org.limelink:limelink_aos_sdk:0.1.0")
```

Maven Local에서 가져오므로, SDK를 수정하면 반드시 `publishToMavenLocal` 후 Example App을 다시 빌드해야 합니다.

### Gradle Repository 설정

`example/settings.gradle.kts`에서 `mavenLocal()`이 선언되어 있어야 합니다:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()  // SDK를 여기서 가져옴
    }
}
```

---

## 7. Troubleshooting

### "Unresolved reference: LimeLinkSDK"

SDK가 Maven Local에 퍼블리시되지 않은 경우 발생합니다.

```bash
# 1. SDK가 퍼블리시되었는지 확인
ls ~/.m2/repository/org/limelink/limelink_aos_sdk/0.1.0/

# 2. 없다면 SDK 퍼블리시
cd /path/to/limelink-aos-sdk
./gradlew clean publishToMavenLocal

# 3. Example App 의존성 캐시 갱신 후 빌드
cd example
./gradlew clean --refresh-dependencies
./gradlew :app:build
```

### Manifest Merger 에러

SDK의 `AndroidManifest.xml`에 `application` 레벨 속성(theme, icon 등)이 정의되어 있으면 Example App과 충돌합니다. SDK manifest에는 application-level 속성을 넣지 않아야 합니다.

### 버전 호환성 문제

Example App과 SDK의 버전이 일치해야 합니다:

| 항목 | SDK | Example App |
|------|-----|-------------|
| compileSdk | 35 | 35 |
| Kotlin | 2.0.0 | 2.0.0 |
| AGP | 8.7.0 | 8.7.0 |

### Composite Build (고급)

SDK 코드를 수정할 때마다 `publishToMavenLocal`을 반복하는 것이 번거롭다면, Gradle Composite Build를 사용하여 SDK 소스를 직접 참조할 수 있습니다. 단, 플러그인 버전 관리에 주의가 필요합니다.

참고: [Gradle Composite Builds Documentation](https://docs.gradle.org/current/userguide/composite_builds.html)
