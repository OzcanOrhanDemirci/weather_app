plugins {
    `kotlin-dsl`
}

group = "com.ozcanorhandemirci.hava.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "hava.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "hava.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "hava.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = "hava.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
