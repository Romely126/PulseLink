plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.phone"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.phone"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // SAP SDK 의 metadata XML 을 res/xml/ 에 넣었다면 packagingOptions 로 충돌 방지
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // libs 폴더(.jar, .aar) 안에 있는 모든 파일을 classpath 에 포함
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0") // Gson 컨버터
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3") // (선택적) API 통신 로깅용
}
