# 1️⃣ 모든 클래스 난독화 방지 (즉, 기본적으로 모든 코드를 유지)
-keep class * { *; }

# 2️⃣ 리플렉션(Reflection) 관련 예외 처리 (어노테이션 유지)
-keepattributes *Annotation*

# 3️⃣ Serializable을 구현하는 클래스의 멤버 유지
-keepclassmembers class * implements java.io.Serializable { *; }

# 4️⃣ RetrofitClient 클래스만 난독화 (클래스명 변경 가능, 내부 로직 유지)
-keepclassmembers,allowobfuscation class org.limelink.limelink_aos_sdk.service.RetrofitClient {
    public <init>(...); # 생성자 유지
    public static *; # 정적 메서드 유지
}

# 5️⃣ Gson / Moshi JSON 직렬화 관련 필드 유지
-keepclassmembers class org.limelink.limelink_aos_sdk.service.RetrofitClient {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 6️⃣ BASE_URL을 포함하는 모든 const val 제거 (최적화)
-assumenosideeffects class org.limelink.limelink_aos_sdk.service.RetrofitClient {
    private static final java.lang.String BASE_URL;
}

# 7️⃣ Retrofit 인터페이스는 난독화 방지 (Retrofit이 동작하도록)
-keep interface org.limelink.limelink_aos_sdk.api.** { *; }

# 8️⃣ Retrofit의 API 호출 메서드 유지 (Reflection을 통해 접근하기 때문)
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# 9️⃣ Retrofit의 동적 프록시 내부 코드 유지 (Retrofit 동작 보장)
-keepattributes Signature
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.squareup.moshi.** { *; }

# 🔟 로그 관련 클래스는 제거하여 보안 강화 (선택 사항)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
