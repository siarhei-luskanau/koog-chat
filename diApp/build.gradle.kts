import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

plugins {
    id("composeMultiplatformConvention")
    id("roborazziConvention")
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
                // implementation(projects.core.coreAuthFake)
                // implementation(projects.core.coreSyncFake)
            } else {
                // implementation(projects.core.coreAuthFirebase)
                // implementation(projects.core.coreSyncFirebase)
            }
        }
    }
}

@OptIn(ExperimentalRoborazziApi::class)
roborazzi.generateComposePreviewRobolectricTests.packages = listOfNotNull(kotlin.android.namespace)
