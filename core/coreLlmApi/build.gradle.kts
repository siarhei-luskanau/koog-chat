plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "koog.chat.core.llm.api"
    sourceSets {
        commonMain.dependencies {
            api(projects.core.coreDatabaseApi)
        }
    }
}
