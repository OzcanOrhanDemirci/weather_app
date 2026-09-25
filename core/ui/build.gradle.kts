plugins {
    id("hava.android.library")
    id("hava.android.compose")
}

android {
    namespace = "com.ozcanorhandemirci.hava.core.ui"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:designsystem"))
}
