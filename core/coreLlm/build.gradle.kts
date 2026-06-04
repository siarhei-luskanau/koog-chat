plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "koog.chat.core.llm"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koog.agents)
            implementation(project.dependencies.platform(libs.ktor.bom))
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabaseApi)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        webMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
