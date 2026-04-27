plugins {
    id("composeMultiplatformConvention")
    alias(libs.plugins.roborazzi)
}

kotlin {
    android.namespace = "koog.chat.di.app"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.datastore.core.okio)
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabaseApi)
            implementation(projects.core.coreDatabaseRoom)
            implementation(projects.core.corePref)
            implementation(projects.navigation)
            implementation(projects.ui.uiCommon)
            implementation(projects.ui.uiMain)
            implementation(projects.ui.uiSplash)
        }

        jvmTest.dependencies {
            implementation(libs.roborazzi.compose.desktop)
        }

        androidHostTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
        }

        iosTest.dependencies {
            implementation(libs.roborazzi.compose.ios)
        }
    }
}

// Directory for reference images
roborazzi.outputDir.set(file("src/screenshots"))
