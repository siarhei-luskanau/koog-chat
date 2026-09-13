import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

plugins {
    id("composeMultiplatformConvention")
    id("roborazziConvention")
}

kotlin {
    android.namespace = "template.ui.main"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabaseApi)
            implementation(projects.core.corePrefApi)
            implementation(projects.ui.uiCommon)
        }
    }
}

@OptIn(ExperimentalRoborazziApi::class)
roborazzi.generateComposePreviewRobolectricTests.packages = listOfNotNull(kotlin.android.namespace)
