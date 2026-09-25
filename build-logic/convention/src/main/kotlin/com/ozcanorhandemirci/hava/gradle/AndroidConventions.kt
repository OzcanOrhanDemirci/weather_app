package com.ozcanorhandemirci.hava.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * The single place where the platform targets live. Every module reads them
 * from here, so raising a level is one edit rather than one edit per module.
 */
internal object AndroidConfig {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
    const val TARGET_SDK = 36
    const val JVM_TOOLCHAIN = 21
    val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_17
}

/** SDK levels, Java level and Kotlin compiler options shared by every Android module. */
internal fun Project.configureAndroid(extension: CommonExtension) {
    extension.compileSdk = AndroidConfig.COMPILE_SDK
    extension.defaultConfig.minSdk = AndroidConfig.MIN_SDK
    extension.compileOptions.sourceCompatibility = AndroidConfig.JAVA_VERSION
    extension.compileOptions.targetCompatibility = AndroidConfig.JAVA_VERSION

    configureJvmToolchain()
    configureKotlinCompiler()
}

/**
 * Pins the JDK that compiles the project instead of inheriting whichever JDK
 * happens to run Gradle. Without this the same source produces different output
 * on the development machine, on the build server and on a second workstation.
 */
internal fun Project.configureJvmToolchain() {
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(AndroidConfig.JVM_TOOLCHAIN))
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }
}

internal fun Project.configureKotlinCompiler() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(
                providers.gradleProperty("hava.warningsAsErrors").map(String::toBoolean).orElse(false),
            )
        }
    }
}

/** Compose setup shared by every module that declares user interface. */
internal fun Project.configureCompose(extension: CommonExtension) {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    extension.buildFeatures.compose = true

    dependencies {
        val bom = platform(libs.findLibrary("androidx-compose-bom").get())
        add("implementation", bom)
        add("androidTestImplementation", bom)
        add("implementation", libs.findLibrary("androidx-compose-ui").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
        add("implementation", libs.findLibrary("androidx-compose-foundation").get())
        add("implementation", libs.findLibrary("androidx-compose-animation").get())
        add("implementation", libs.findLibrary("androidx-compose-material3").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}
