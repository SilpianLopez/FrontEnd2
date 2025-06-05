plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.frontend2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.frontend2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // 안정적인 버전으로 변경 또는 최신 안정 버전 확인
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // 또는 최신 안정 버전 (예: 2.11.0)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // 또는 최신 안정 버전

    // OkHttp & Logging Interceptor
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // OkHttp (버전은 logging-interceptor와 맞춤)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // ❗️ API 로깅에 매우 유용

    // 차트 라이브러리 (툴바와 직접 관련 없음)
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
}