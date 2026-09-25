plugins {
    id("hava.android.library")
    id("hava.android.hilt")
}

android {
    namespace = "com.ozcanorhandemirci.hava.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
