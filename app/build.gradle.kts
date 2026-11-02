import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    // 코드 폴더 이름이에요. 그대로 두세요.
    namespace = "com.nia.mylog"
    compileSdk = 36

    defaultConfig {
        // 플레이 스토어의 앱 고유 이름(패키지 이름)이에요. 첫 업로드 뒤에는 바꿀 수 없어요.
        // 이미 누가 쓰는 이름이라고 나오면 이 줄만 바꾸면 돼요. 영문 소문자로, 예: "com.nia.mylogapp"
        applicationId = "com.nia.mylog"
        minSdk = 26
        targetSdk = 36
        // 업데이트를 올릴 때마다 versionCode는 1씩 올리고, versionName은 사람이 읽는 버전 이름이에요.
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.webkit:webkit:1.12.1")
}
