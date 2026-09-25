import java.util.Properties

plugins {
    id("hava.android.application")
    id("hava.android.compose")
    id("hava.android.hilt")
}

/**
 * Release signing details, taken from a file that is never committed or, on a
 * build server, from the environment.
 *
 * When neither is present the release build is signed with the debug key so
 * that anyone can clone this repository and produce a working package. What
 * they cannot produce is a package that updates an installation of the real
 * one, which is the only thing the key protects.
 */
val signing = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun signingDetail(property: String, variable: String): String? =
    signing.getProperty(property) ?: System.getenv(variable)

val releaseStore = signingDetail("storeFile", "HAVA_KEYSTORE_FILE")

android {
    namespace = "com.ozcanorhandemirci.hava"

    defaultConfig {
        applicationId = "com.ozcanorhandemirci.hava"
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("release") {
            if (releaseStore != null) {
                storeFile = file(releaseStore)
                storePassword = signingDetail("storePassword", "HAVA_KEYSTORE_PASSWORD")
                keyAlias = signingDetail("keyAlias", "HAVA_KEY_ALIAS")
                keyPassword = signingDetail("keyPassword", "HAVA_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName(if (releaseStore != null) "release" else "debug")
        }
    }

    packaging {
        resources {
            // Dependencies each ship their own copy of these, and none of them
            // is read at runtime.
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/previous-compilation-data.bin"
        }
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:sky"))
    implementation(project(":feature:cities"))
    implementation(project(":feature:detail"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
}
