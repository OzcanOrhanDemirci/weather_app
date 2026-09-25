plugins {
    id("hava.android.application")
    id("hava.android.compose")
}

android {
    namespace = "com.ozcanorhandemirci.hava"

    defaultConfig {
        applicationId = "com.ozcanorhandemirci.hava"
        versionCode = 1
        versionName = "0.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
