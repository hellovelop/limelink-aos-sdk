import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val limeLinkProps = Properties().apply {
    val propsFile = rootProject.file("limelink.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.limelink"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.limelink"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "LIMELINK_API_KEY", "\"${limeLinkProps.getProperty("LIMELINK_API_KEY", "")}\"")
        buildConfigField("String", "LIMELINK_PROJECT_ID", "\"${limeLinkProps.getProperty("LIMELINK_PROJECT_ID", "")}\"")
        buildConfigField("String", "LIMELINK_CUSTOM_DOMAIN", "\"${limeLinkProps.getProperty("LIMELINK_CUSTOM_DOMAIN", "")}\"")
        buildConfigField("String", "LIMELINK_ANDROID_APPLICATION_ID", "\"${limeLinkProps.getProperty("LIMELINK_ANDROID_APPLICATION_ID", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Limelink SDK - from Maven Local
    implementation("org.limelink:limelink_aos_sdk:0.1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
