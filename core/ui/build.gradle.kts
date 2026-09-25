plugins {
    id("hava.android.library")
    id("hava.android.compose")
}

android {
    namespace = "com.ozcanorhandemirci.hava.core.ui"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:sky"))
    implementation(project(":core:designsystem"))
}
