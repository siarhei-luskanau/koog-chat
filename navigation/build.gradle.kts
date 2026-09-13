plugins {
    id("composeMultiplatformConvention")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android.namespace = "template.navigation"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrains.compose.material3.adaptive.navigation.suite)
            implementation(libs.jetbrains.compose.material3.adaptive.navigation3)
            implementation(libs.jetbrains.compose.ui.backhandler)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.koin.compose.navigation3)
            implementation(libs.kotlinx.serialization.json)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(projects.ui.uiCommon)
            implementation(projects.ui.uiMain)
            implementation(projects.ui.uiSplash)
        }
    }
}
