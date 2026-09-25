plugins {
    id("hava.android.library")
    id("hava.android.compose")
}

android {
    namespace = "com.ozcanorhandemirci.hava.core.sky"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:designsystem"))

    testImplementation(libs.junit)
    testImplementation(libs.kotest.assertions)
}
