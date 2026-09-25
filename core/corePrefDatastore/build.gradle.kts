plugins {
    id("composeMultiplatformConvention")
    alias(libs.plugins.kotlinx.serialization)
}

koinCompiler {
    // KOIN-D002 false positive on Kotlin/Native: the compile-safety checker can't
    // resolve @Single bindings from commonMain's @ComponentScan across the test klib boundary.
    compileSafety = false
}

kotlin {
    android.namespace = "koog.chat.core.pref.datastore"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.datastore.core.okio)
            implementation(libs.kotlinx.serialization.json)
            implementation(projects.core.coreCommon)
            implementation(projects.core.corePrefApi)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.datastore.tink)
        }
    }
}
