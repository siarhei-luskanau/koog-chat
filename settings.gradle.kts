rootProject.name = "koog-chat"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":app:androidApp",
    ":app:desktopApp",
    ":app:webApp",
    ":core:coreCommon",
    ":core:coreDatabaseApi",
    ":core:coreDatabaseRoom",
    ":core:coreLlm",
    ":core:corePref",
    ":diApp",
    ":navigation",
    ":ui:uiChat",
    ":ui:uiChatList",
    ":ui:uiCommon",
    ":ui:uiLlmConfig",
    ":ui:uiSplash",
)

pluginManagement {
    includeBuild("convention-plugin-multiplatform")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        mavenCentral()
    }
}
