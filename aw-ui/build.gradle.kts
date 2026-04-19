plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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

    testOptions {
        unitTests.isReturnDefaultValues = true
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
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    api(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    api("androidx.constraintlayout:constraintlayout:2.1.4")
    api("androidx.recyclerview:recyclerview:1.3.2")
    api("androidx.viewpager2:viewpager2:1.0.0")

    testImplementation(libs.junit)
}

apply(from = "${rootDir}/gradle/publish.gradle.kts")