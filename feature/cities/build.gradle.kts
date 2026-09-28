plugins {
    id("hava.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ozcanorhandemirci.hava.feature.cities"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)

    // The main-dispatcher rule, taken as a test fixture rather than as part of
    // core:common, so that JUnit reaches this module's tests without reaching
    // the application.
    testImplementation(testFixtures(project(":core:common")))
}
