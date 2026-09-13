plugins {
    id("composeMultiplatformConvention")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android.namespace = "template.core.network.ktor"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(project.dependencies.platform(libs.ktor.bom))
            implementation(projects.core.coreNetworkApi)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.apache5)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        webMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
