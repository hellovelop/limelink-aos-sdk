# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 모든 코드 난독화 및 최적화
-dontwarn **  # 경고 무시
#-dontoptimize  # 최적화 방지 (테스트용)
#-dontobfuscate # 난독화 방지 (테스트용, 실제 릴리즈 시 제거)

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

# 5️⃣ (필요 시) JSON 직렬화 관련 필드 유지 (Gson / Moshi)
-keepclassmembers class org.limelink.limelink_aos_sdk.service.RetrofitClient {
    @com.google.gson.annotations.SerializedName <fields>;
}