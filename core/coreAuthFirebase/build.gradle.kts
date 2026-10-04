import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("composeMultiplatformConvention")
}

abstract class GenerateGoogleAuthConfigTask : DefaultTask() {
    @get:Input
    abstract val webClientId: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val escapedWebClientId =
            webClientId
                .get()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("$", "\\$")
        val packageDir = outputDir.get().asFile.resolve("koog/chat/core/auth/firebase")
        packageDir.mkdirs()
        packageDir.resolve("GoogleAuthConfig.kt").writeText(
            """
            package koog.chat.core.auth.firebase

            internal object GoogleAuthConfig {
                const val WEB_CLIENT_ID: String = "$escapedWebClientId"
            }

            """.trimIndent(),
        )
    }
}

val generateGoogleAuthConfig =
    tasks.register<GenerateGoogleAuthConfigTask>("generateGoogleAuthConfig") {
        webClientId.set(
            gradleLocalProperties(rootDir, providers).getProperty("GOOGLE_WEB_CLIENT_ID", ""),
        )
        outputDir.set(layout.buildDirectory.dir("generated/googleAuthConfig/commonMain/kotlin"))
    }

kotlin {
    android.namespace = "koog.chat.core.auth.firebase"
    sourceSets {
        commonMain {
            kotlin.srcDir(generateGoogleAuthConfig.flatMap { it.outputDir })
            dependencies {
                implementation(projects.core.coreAuthApi)
                implementation(libs.kmpauth.google)
            }
        }
    }
}
