plugins {
    id("hava.android.application")
    id("hava.android.compose")
    id("hava.android.hilt")
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
    implementation(project(":core:designsystem"))
    implementation(project(":core:sky"))
    implementation(project(":core:data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
