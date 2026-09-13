rootProject.name = "compose-multiplatform-template"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":app:androidApp",
    ":app:desktopApp",
    ":app:webApp",
    ":core:coreCommon",
    ":core:coreDatabaseApi",
    ":core:coreDatabaseRoom",
    ":core:coreNetworkApi",
    ":core:coreNetworkKtor",
    ":core:corePrefApi",
    ":core:corePrefDatastore",
    ":diApp",
    ":navigation",
    ":ui:uiCommon",
    ":ui:uiMain",
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
