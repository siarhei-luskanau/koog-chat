import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

plugins {
    id("composeMultiplatformConvention")
    id("roborazziConvention")
}

kotlin {
    android.namespace = "koog.chat.ui.llmconfig"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabaseApi)
            implementation(projects.ui.uiCommon)
        }
    }
}

@OptIn(ExperimentalRoborazziApi::class)
roborazzi.generateComposePreviewRobolectricTests.packages = listOfNotNull(kotlin.android.namespace)
