plugins {
    id("composeMultiplatformConvention")
    alias(libs.plugins.ksp)
}

kotlin.android.namespace = "koog.chat.core.database.room"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite)
        }
    }
}

dependencies {
    ksp(libs.androidx.room3.compiler)
}
