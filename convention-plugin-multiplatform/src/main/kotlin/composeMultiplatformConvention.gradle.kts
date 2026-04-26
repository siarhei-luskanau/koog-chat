import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("io.insert-koin.compiler.plugin")
    id("org.jetbrains.compose")
    kotlin("multiplatform")
    kotlin("plugin.compose")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    android {
        compilerOptions { jvmTarget = JvmTarget.fromTarget(libs.versions.javaVersion.get()) }
        compileSdk =
            libs.versions.build.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.build.android.minSdk
                .get()
                .toInt()
        androidResources.enable = true
        withHostTestBuilder {}.configure {
            isIncludeAndroidResources = true
            enableCoverage = true
        }
        withDeviceTestBuilder {
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            animationsDisabled = true
            managedDevices.localDevices.create("managedVirtualDevice") {
                device = "Pixel 2"
                apiLevel = 35
            }
        }
        packaging.resources.excludes.add("META-INF/**")
    }

    jvm {
        compilerOptions { jvmTarget = JvmTarget.fromTarget(libs.versions.javaVersion.get()) }
    }

    js {
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrains.compose.animation)
            implementation(libs.jetbrains.compose.animation.graphics)
            implementation(libs.jetbrains.compose.components.resources)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.ui.tooling.preview)
            implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            implementation(libs.jetbrains.savedstate.compose)
            implementation(libs.jetbrains.window.core)
            implementation(libs.koin.annotations)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.koin.bom))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.jetbrains.compose.ui.test)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.androidx.uitest.junit4)
                implementation(libs.androidx.uitest.testManifest)
            }
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }

        iosMain.dependencies {
        }

        webMain.dependencies {
        }
    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries { framework { baseName = "ComposeApp" } }
        }
}

koinCompiler {
    compileSafety = false
}

tasks.withType<Test>().matching { it.name.contains("AndroidHostTest") }.configureEach {
    exclude("**/*CommonTest*")
}

tasks.withType<AbstractTestTask>().configureEach {
    failOnNoDiscoveredTests = false
}
