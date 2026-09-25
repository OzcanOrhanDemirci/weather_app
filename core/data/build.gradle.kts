plugins {
    id("hava.android.library")
    id("hava.android.hilt")
}

android {
    namespace = "com.ozcanorhandemirci.hava.core.data"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
