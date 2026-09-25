plugins {
    id("hava.android.library")
    id("hava.android.hilt")
    alias(libs.plugins.room)
}

android {
    namespace = "com.ozcanorhandemirci.hava.core.database"
}

room {
    // Schemas are committed so a migration can be reviewed as a diff rather
    // than taken on trust.
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(project(":core:model"))

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotlinx.coroutines.test)
}
