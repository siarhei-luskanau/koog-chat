plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "koog.chat.core.sync.fake"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreSyncApi)
        }
    }
}
