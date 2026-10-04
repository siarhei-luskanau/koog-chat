plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "koog.chat.core.auth.firebase"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreAuthApi)
            implementation(libs.kmpauth.google)
        }
    }
}
