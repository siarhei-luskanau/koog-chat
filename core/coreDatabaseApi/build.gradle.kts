plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "koog.chat.core.database.api"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.paging.compose)
        }
    }
}
