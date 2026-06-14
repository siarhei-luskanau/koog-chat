import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

plugins {
    id("composeMultiplatformConvention")
    id("roborazziConvention")
}

kotlin.android.namespace = "koog.chat.ui.common"

compose.resources {
    publicResClass = true
    packageOfResClass = "${kotlin.android.namespace}.resources"
    generateResClass = always
}

@OptIn(ExperimentalRoborazziApi::class)
roborazzi.generateComposePreviewRobolectricTests.packages = listOfNotNull(kotlin.android.namespace)
