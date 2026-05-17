plugins {
    id("composeMultiplatformConvention")
    alias(libs.plugins.androidx.room3)
    alias(libs.plugins.ksp)
}

kotlin {
    android.namespace = "koog.chat.core.database.room"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room3.migration)
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
        jvmMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
        webMain.dependencies {
            implementation(libs.androidx.sqlite.web)
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
