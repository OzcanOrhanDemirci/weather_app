plugins {
    id("hava.android.library")
    id("hava.android.hilt")
}

android {
    namespace = "com.ozcanorhandemirci.hava.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)

    // The test rule in this module is used by the feature modules, so the
    // libraries it is written against are part of its own interface.
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
