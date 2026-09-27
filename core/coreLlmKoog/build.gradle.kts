plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "koog.chat.core.llm.koog"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koog.http.client.ktor)
            implementation(libs.koog.prompt.executor.anthropic.client)
            implementation(libs.koog.prompt.executor.google.client)
            implementation(libs.koog.prompt.executor.ollama.client)
            implementation(libs.koog.prompt.executor.openai.client)
            implementation(libs.ktor.client.engine.defaults)
            implementation(project.dependencies.platform(libs.ktor.bom))
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreLlmApi)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}
