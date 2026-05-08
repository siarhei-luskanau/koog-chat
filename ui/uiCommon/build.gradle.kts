plugins {
    id("composeMultiplatformConvention")
    alias(libs.plugins.roborazzi)
}

kotlin {
    android.namespace = "koog.chat.ui.common"
    sourceSets {
        androidHostTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
        }

        jvmTest.dependencies {
            implementation(libs.roborazzi.compose.desktop)
        }

        iosTest.dependencies {
            implementation(libs.roborazzi.compose.ios)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "${kotlin.android.namespace}.resources"
    generateResClass = always
}

// Directory for reference images
roborazzi.outputDir.set(file("src/screenshots"))
