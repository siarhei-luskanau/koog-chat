plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "koog.chat.core.auth.fake"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreAuthApi)
        }
    }
}
