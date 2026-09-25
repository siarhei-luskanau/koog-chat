import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

plugins {
    id("composeMultiplatformConvention")
    id("roborazziConvention")
}

kotlin {
    android.namespace = "koog.chat.ui.main"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabaseApi)
            implementation(projects.core.corePrefApi)
            implementation(projects.ui.uiCommon)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabaseApi)
            implementation(projects.core.corePrefApi)
        }
    }
}

@OptIn(ExperimentalRoborazziApi::class)
roborazzi.generateComposePreviewRobolectricTests.packages = listOfNotNull(kotlin.android.namespace)
