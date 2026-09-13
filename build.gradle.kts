import groovy.json.JsonSlurper
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.artifacts.ProjectDependency
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose.multiplatform).apply(false)
    alias(libs.plugins.detekt)
    alias(libs.plugins.koin.compiler).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.kover)
}

kover {
    currentProject {
        createVariant("coverage") {}
    }
    reports {
        filters {
            excludes {
                classes("*ComposableSingletons*")
                classes("*_Impl", "*_Impl$*")
                packages("org.koin.ksp.generated")
                packages("*.generated.resources")
            }
        }
        variant("coverage") {
            verify {
                rule {
                    minBound(70)
                }
            }
        }
    }
}

dependencies {
    kover(projects.core.coreCommon)
    kover(projects.core.coreDatabaseApi)
    kover(projects.core.coreDatabaseRoom)
    kover(projects.core.coreNetworkApi)
    kover(projects.core.coreNetworkKtor)
    kover(projects.core.corePrefApi)
    kover(projects.core.corePrefDatastore)
    kover(projects.diApp)
    kover(projects.navigation)
    kover(projects.ui.uiCommon)
    kover(projects.ui.uiMain)
    kover(projects.ui.uiSplash)
}

allprojects {
    apply(from = "$rootDir/ktlint.gradle")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        parallel = true
        ignoreFailures = false
    }
}

tasks.register("checkModuleBoundaries") {
    group = "verification"
    description =
        "Fails the build if a core/*Impl module is depended on by anything other than " +
        ":diApp (see the *Api/*Impl rule in docs/architecture.md)."
}

// Dependency declarations in subproject build scripts aren't visible until every project
// has been configured, and a config-cache-compatible task action can't capture live
// Project/Configuration references — so the check runs here, once all projects are
// configured, and only the resulting List<String> (plain values) is captured by doLast.
gradle.projectsEvaluated {
    val coreImplModulePaths =
        setOf(
            ":core:coreDatabaseRoom",
            ":core:coreNetworkKtor",
            ":core:corePrefDatastore",
        )
    val violations =
        subprojects
            .filter { it.path != ":diApp" }
            .flatMap { subproject ->
                subproject.configurations
                    .flatMap { it.dependencies.withType<ProjectDependency>() }
                    .map { it.path }
                    .filter { it in coreImplModulePaths && it != subproject.path }
                    .map { implPath -> "${subproject.path} -> $implPath" }
            }.distinct()
            .sorted()
    tasks.named("checkModuleBoundaries") {
        doLast {
            if (violations.isNotEmpty()) {
                throw GradleException(
                    "Architectural constraint violated: only :diApp may depend on a " +
                        "core/*Impl module (docs/architecture.md). Violations:\n" +
                        violations.joinToString("\n") { "  - $it" },
                )
            }
        }
    }
}

tasks.register("ciVerifyScreenshotJobsMatrixSetup") {
    val matrixJson =
        getScreenshotMatrixJson(
            rootProject = rootProject,
            roborazziTask = "verifyRoborazzi",
            includeMacOS = false,
        )
    val outputFile = layout.buildDirectory.file("verify_screenshot_jobs_matrix.json")
    doLast {
        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(matrixJson)
        }
        println("screenshot_jobs_matrix: $matrixJson")
    }
}

tasks.register("ciRecordScreenshotJobsMatrixSetup") {
    val matrixJson =
        getScreenshotMatrixJson(
            rootProject = rootProject,
            roborazziTask = "recordRoborazzi",
            includeMacOS = true,
        )
    val outputFile = layout.buildDirectory.file("record_screenshot_jobs_matrix.json")
    doLast {
        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(matrixJson)
        }
        println("record_screenshot_jobs_matrix: $matrixJson")
    }
}

tasks.register("ciIos") {
    val injected = project.objects.newInstance<Injected>()
    doLast {
        if (Os.isFamily(Os.FAMILY_MAC)) {
            injected.runExec(listOf("brew", "install", "kdoctor"))
            injected.runExec(listOf("kdoctor"))
            val devicesJson =
                injected.runExec(
                    listOf(
                        "xcrun",
                        "simctl",
                        "list",
                        "devices",
                        "available",
                        "-j",
                    ),
                )

            @Suppress("UNCHECKED_CAST")
            val devicesList =
                (JsonSlurper().parseText(devicesJson) as Map<String, *>)
                    .let { it["devices"] as Map<String, *> }
                    .let { devicesMap ->
                        devicesMap.keys
                            .filter { it.startsWith("com.apple.CoreSimulator.SimRuntime.iOS") }
                            .map { devicesMap[it] as List<*> }
                    }.map { jsonArray -> jsonArray.map { it as Map<String, *> } }
                    .flatten()
                    .filter { it["isAvailable"] as Boolean }
                    .filter {
                        listOf("iphone 1").any { device ->
                            (it["name"] as String).contains(device, true)
                        }
                    }
            println("Devices:${devicesList.joinToString { "\n" + it["udid"] + ": " + it["name"] }}")
            val device = devicesList.firstOrNull()
            println("Selected:\n${device?.get("udid")}: ${device?.get("name")}")
            val rootDirPath = injected.projectLayout.projectDirectory.asFile.path
            injected.runExec(
                listOf(
                    "xcodebuild",
                    "-project",
                    "$rootDirPath/app/iosApp/iosApp.xcodeproj",
                    "-scheme",
                    "iosApp",
                    "-configuration",
                    "Debug",
                    "OBJROOT=$rootDirPath/build/ios",
                    "SYMROOT=$rootDirPath/build/ios",
                    "-destination",
                    "id=${device?.get("udid")}",
                    "-allowProvisioningDeviceRegistration",
                    "-allowProvisioningUpdates",
                ),
            )
        }
    }
}

abstract class Injected {
    @get:Inject abstract val execOperations: ExecOperations

    @get:Inject abstract val projectLayout: ProjectLayout

    fun runExec(commands: List<String>): String =
        object : ByteArrayOutputStream() {
            override fun write(
                p0: ByteArray,
                p1: Int,
                p2: Int,
            ) {
                print(String(p0, p1, p2))
                super.write(p0, p1, p2)
            }
        }.let { resultOutputStream ->
            execOperations
                .exec {
                    if (System.getenv("JAVA_HOME") == null) {
                        System.getProperty("java.home")?.let { javaHome ->
                            environment =
                                environment.toMutableMap().apply {
                                    put("JAVA_HOME", javaHome)
                                }
                        }
                    }
                    commandLine = commands
                    standardOutput = resultOutputStream
                    println("commandLine: ${this.commandLine.joinToString(separator = " ")}")
                }.apply { println("ExecResult: $this") }
            String(resultOutputStream.toByteArray())
        }
}
