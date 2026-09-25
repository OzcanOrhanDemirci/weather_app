plugins {
    id("hava.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ozcanorhandemirci.hava.feature.detail"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
