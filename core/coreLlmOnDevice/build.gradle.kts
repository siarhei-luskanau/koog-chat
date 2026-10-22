plugins {
    id("composeMultiplatformConvention")
}

koinCompiler {
    // KOIN-D002 false positive on Kotlin/Native: the compile-safety checker can't
    // resolve @Single bindings from commonMain's @ComponentScan across the test klib boundary.
    compileSafety = false
}

kotlin {
    android.namespace = "koog.chat.core.llm.ondevice"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreLlmApi)
        }
        androidMain.dependencies {
            implementation(libs.mlkit.genai.prompt)
        }
    }
}
