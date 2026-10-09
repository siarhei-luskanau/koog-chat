import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

plugins {
    id("composeMultiplatformConvention")
    id("roborazziConvention")
}

koinCompiler {
    // KOIN-D002 false positive on JS/WasmJs: the compile-safety checker can't see @Configuration
    // modules pulled in from dependency klibs, though they resolve at runtime (AuthServiceCommonTest).
    compileSafety = false
}

kotlin {
    android.namespace = "koog.chat.di"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabaseRoom)
            implementation(projects.core.coreLlmKoog)
            implementation(projects.core.coreNetworkKtor)
            implementation(projects.core.corePrefDatastore)
            implementation(projects.navigation)
            implementation(projects.ui.uiCommon)
            implementation(projects.ui.uiMain)
            implementation(projects.ui.uiSplash)
            if (isFakeDataEnabled { gradleLocalProperties(rootDir, providers) }) {
                implementation(projects.core.coreAuthFake)
                implementation(projects.core.coreSyncFake)
            } else {
                implementation(projects.core.coreAuthFirebase)
                // implementation(projects.core.coreSyncFirebase)
            }
        }
        if (isFakeDataEnabled { gradleLocalProperties(rootDir, providers) }) {
            commonTest { kotlin.srcDir("src/commonTestFake/kotlin") }
        }
        commonTest.dependencies {
            implementation(libs.koin.compose)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(projects.core.coreAuthApi)
            implementation(projects.core.coreSyncApi)
        }
    }
}

@OptIn(ExperimentalRoborazziApi::class)
roborazzi.generateComposePreviewRobolectricTests.packages = listOfNotNull(kotlin.android.namespace)
