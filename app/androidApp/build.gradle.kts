import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "koog.chat.compose.multiplatform"
    compileSdk =
        libs.versions.build.android.compileSdk
            .get()
            .toInt()
    defaultConfig {
        minSdk =
            libs.versions.build.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.build.android.targetSdk
                .get()
                .toInt()
        applicationId = "koog.chat.compose.multiplatform"
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility =
            JavaVersion.toVersion(
                libs.versions.javaVersion
                    .get()
                    .toInt(),
            )
        targetCompatibility =
            JavaVersion.toVersion(
                libs.versions.javaVersion
                    .get()
                    .toInt(),
            )
    }
    buildFeatures.compose = true
    packaging.resources.excludes.add("META-INF/**")
    testOptions {
        unitTests {
            all { test: Test ->
                test.testLogging.events = TestLogEvent.entries.toSet()
                test.testLogging.exceptionFormat =
                    org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }
        animationsDisabled = true
        managedDevices.localDevices.create("managedVirtualDevice") {
            device = "Pixel 2"
            apiLevel = 35
        }
    }
}

kotlin {
    android {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(libs.versions.javaVersion.get())
        }
    }
}

dependencies {
    androidTestImplementation(kotlin("test-junit"))
    androidTestImplementation(libs.androidx.uitest.junit4)
    debugImplementation(libs.androidx.uitest.testManifest)
    debugImplementation(libs.leakcanary.android)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.core.okio)
    implementation(projects.diApp)
}
