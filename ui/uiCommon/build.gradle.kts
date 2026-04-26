plugins {
    id("composeMultiplatformConvention")
}

kotlin.android.namespace = "koog.chat.ui.common"

compose.resources {
    publicResClass = true
    packageOfResClass = "${kotlin.android.namespace}.resources"
    generateResClass = always
}
