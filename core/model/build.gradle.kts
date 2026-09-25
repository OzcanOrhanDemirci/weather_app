plugins {
    id("hava.jvm.library")
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotest.assertions)
}
