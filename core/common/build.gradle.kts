plugins {
    id("hava.android.library")
    id("hava.android.hilt")
}

android {
    namespace = "com.ozcanorhandemirci.hava.core.common"

    // The test rule this module publishes is used by the feature modules, so
    // it has to leave the module. It leaves as a test fixture rather than as
    // part of the library, because a rule that ships inside the library takes
    // JUnit with it into the application people install.
    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)

    // The libraries the rule is written against are part of the fixture's
    // interface, and of nothing else.
    testFixturesApi(libs.junit)
    testFixturesApi(libs.kotlinx.coroutines.test)
}
