plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "koog.chat.core.database.api"
    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.paging.common)
        }
    }
}
