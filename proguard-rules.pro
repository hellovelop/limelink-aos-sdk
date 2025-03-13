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

# 리플렉션(Reflection) 관련 예외 처리
-keepattributes *Annotation*

# Serializable 인터페이스 유지
-keepclassmembers class * implements java.io.Serializable { *; }

-keep class org.limelink.limelink_aos_sdk.service.RetrofitClient { *; }