import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

plugins {
    id("composeMultiplatformConvention")
    id("roborazziConvention")
    alias(libs.plugins.buildConfig)
}

kotlin {
    android.namespace = "koog.chat.di"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room3.runtime)
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabaseApi)
            implementation(projects.core.coreDatabaseRoom)
            implementation(projects.core.coreLlm)
            implementation(projects.core.corePref)
            implementation(projects.navigation)
            implementation(projects.ui.uiChat)
            implementation(projects.ui.uiChatList)
            implementation(projects.ui.uiCommon)
            implementation(projects.ui.uiSplash)
        }
    }
}

buildConfig {
    packageName(kotlin.android.namespace.orEmpty())
    useKotlinOutput {
        topLevelConstants = true
        internalVisibility = true
    }
    val isDataStubEnabled = isDataStubEnabled { gradleLocalProperties(rootDir, providers) }
    buildConfigField("Boolean", "IS_DATA_STUB_ENABLED", "$isDataStubEnabled")
}

@OptIn(ExperimentalRoborazziApi::class)
roborazzi.generateComposePreviewRobolectricTests.packages = listOf("koog.chat.di")
