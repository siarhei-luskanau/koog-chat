plugins {
    id("composeMultiplatformConvention")
    alias(libs.plugins.androidx.room3)
    alias(libs.plugins.ksp)
}

koinCompiler {
    // KOIN-D002 false positive on Kotlin/Native: the compile-safety checker can't
    // resolve @Single bindings from commonMain's @ComponentScan across the test klib boundary.
    compileSafety = false
}

kotlin {
    android.namespace = "koog.chat.core.database.room"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room3.paging)
            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite)
            implementation(libs.androidx.sqlite.async)
            implementation(projects.core.coreDatabaseApi)
        }
        androidMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
        iosMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
        webMain.dependencies {
            implementation(libs.androidx.sqlite.web)
            implementation(npm("sql-js-worker", layout.projectDirectory.dir("worker").asFile))
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
    add("kspWasmJs", libs.androidx.room3.compiler)
    add("kspJs", libs.androidx.room3.compiler)
}
