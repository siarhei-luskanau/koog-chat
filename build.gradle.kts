import groovy.json.JsonSlurper
import org.apache.tools.ant.taskdefs.condition.Os
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose.multiplatform).apply(false)
    alias(libs.plugins.detekt)
    alias(libs.plugins.koin.compiler).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
}

allprojects {
    apply(from = "$rootDir/ktlint.gradle")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        parallel = true
        ignoreFailures = false
    }
}

tasks.register("ciVerifyScreenshotJobsMatrixSetup") {
    val matrixJson = getScreenshotMatrixJson(rootProject = rootProject, roborazziTask = "verifyRoborazzi")
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
    val matrixJson = getScreenshotMatrixJson(rootProject = rootProject, roborazziTask = "recordRoborazzi")
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
