plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.answufeng.ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
    }
}

ktlint {
    android.set(true)
    ignoreFailures = false
}

dependencies {
    implementation(libs.core.ktx)
    // 多个公开 View 继承 AppCompat*；viewBinding 等 API 使用 AppCompatActivity / AppCompat* 类型
    api(libs.appcompat)
    implementation(libs.material)
    api(libs.swiperefresh)
    implementation(libs.activity.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    api(libs.recyclerview)
    api(libs.viewpager2)
}

apply(from = "$rootDir/gradle/publish.gradle.kts")
