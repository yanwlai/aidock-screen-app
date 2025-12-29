import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.clevo.recorder"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.clevo.recorder"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val createTime = SimpleDateFormat("yyyyMMddHHmm").format(Date())
        val fileName = "AIdockScreenApp-v${versionName}-${createTime}"
        setProperty("archivesBaseName", fileName)

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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}