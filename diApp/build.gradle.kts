import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("composeMultiplatformConvention")
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.roborazzi)
}

kotlin {
    android.namespace = "koog.chat.di.app"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room3.runtime)
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabaseApi)
            implementation(projects.core.coreDatabaseRoom)
            implementation(projects.core.coreLlm)
            implementation(projects.core.corePref)
            implementation(projects.navigation)
            implementation(projects.ui.uiChat)
            implementation(projects.ui.uiChatList)
            implementation(projects.ui.uiCommon)
            implementation(projects.ui.uiSplash)
        }

        jvmTest.dependencies {
            implementation(libs.roborazzi.compose.desktop)
        }

        androidHostTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
        }

        iosTest.dependencies {
            implementation(libs.roborazzi.compose.ios)
        }
    }
}

// Directory for reference images
roborazzi.outputDir.set(file("src/screenshots"))

buildConfig {
    packageName(kotlin.android.namespace.orEmpty())
    useKotlinOutput {
        topLevelConstants = true
        internalVisibility = true
    }
    val isDataStubEnabled = isDataStubEnabled { gradleLocalProperties(rootDir, providers) }
    buildConfigField("Boolean", "IS_DATA_STUB_ENABLED", "$isDataStubEnabled")
}
