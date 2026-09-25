import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("composeMultiplatformConvention")
    id("io.github.takahirom.roborazzi")
}

kotlin {
    sourceSets {
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.composable.preview.scanner.android)
                implementation(libs.junit)
                implementation(libs.robolectric)
                implementation(libs.roborazzi)
                implementation(libs.roborazzi.compose)
                implementation(libs.roborazzi.compose.preview.scanner.support)
            }
        }

        jvmTest.dependencies {
            implementation(libs.roborazzi.compose.desktop)
        }

        iosTest.dependencies {
            implementation(libs.roborazzi.compose.ios)
        }
    }
}

val sqliteBundledNatives: Configuration by configurations.creating {
    isTransitive = false
}

dependencies {
    sqliteBundledNatives("androidx.sqlite:sqlite-bundled-jvm:${libs.versions.androidx.sqlite.get()}")
}

val extractSqliteBundledNatives by tasks.registering(Sync::class) {
    from(provider { sqliteBundledNatives.map(::zipTree) })
    include("natives/**")
    into(layout.buildDirectory.dir("sqliteBundledNatives"))
}

val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch").lowercase()
val sqliteNativesPlatform =
    when {
        osName.contains("mac") -> "osx_arm64"
        osName.contains("win") -> "windows_x64"
        osArch.contains("aarch64") -> "linux_arm64"
        else -> "linux_x64"
    }
val sqliteNativesLibName =
    when {
        osName.contains("mac") -> "libsqliteJni.dylib"
        osName.contains("win") -> "sqliteJni.dll"
        else -> "libsqliteJni.so"
    }

tasks.withType<Test>().configureEach {
    if (listOf("AndroidHostTest", "IosSimulator").any { name.contains(other = it, ignoreCase = true) }) {
        dependsOn(extractSqliteBundledNatives)
        systemProperty(
            "androidx.sqlite.driver.bundled.path",
            extractSqliteBundledNatives
                .get()
                .destinationDir
                .resolve("natives/$sqliteNativesPlatform")
                .absolutePath,
        )
        systemProperty("androidx.sqlite.driver.bundled.name", sqliteNativesLibName)
    }
}

roborazzi {
    @OptIn(ExperimentalRoborazziApi::class)
    generateComposePreviewRobolectricTests {
        enable = true
        robolectricConfig =
            mapOf(
                "sdk" to "[37]",
                "qualifiers" to "RobolectricDeviceQualifiers.SmallPhone",
            )
        includePrivatePreviews = true
    }

    // Directory for reference images
    outputDir.set(file("src/screenshots"))
}
