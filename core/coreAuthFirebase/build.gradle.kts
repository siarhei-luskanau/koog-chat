plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "koog.chat.core.auth.firebase"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kmpauth.google)
            implementation(libs.kmpauth.uihelper)
            implementation(projects.core.coreAuthApi)
        }
    }
}
