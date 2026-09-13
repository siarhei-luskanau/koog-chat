# Architecture

## Module map

```
app/androidApp   Android application entry point (Activity), depends only on diApp
app/desktopApp   Desktop (JVM) application entry point, depends only on diApp
app/webApp       Browser entry point, js + wasmJs targets, depends only on diApp
app/iosApp       Xcode project wrapping the framework diApp produces (no build.gradle.kts)

diApp            Koin DI graph. The ONLY module allowed to depend on *Impl modules.

navigation       Navigation3 + adaptive-navigation-suite graph, depends on ui/*

ui/uiCommon      Shared composables, Compose resources (publicResClass)
ui/uiMain        Main screen, depends on coreDatabaseApi + corePrefApi + uiCommon
ui/uiSplash      Splash screen, depends on coreCommon + uiCommon

core/coreCommon        Dispatchers/platform-service abstractions with per-target impls
core/coreDatabaseApi    Database repository interface only, zero implementation deps
core/coreDatabaseRoom   Room 3 KMP implementation of coreDatabaseApi
core/coreNetworkApi     Network client interface only, zero implementation deps
core/coreNetworkKtor    Ktor implementation of coreNetworkApi
core/corePrefApi        Preferences/storage interface only, zero implementation deps
core/corePrefDatastore  AndroidX DataStore implementation of corePrefApi
```

## The `*Api` / `*Impl` rule

Every core capability is split into two modules:

- `core/*Api` — interfaces and models only. No implementation dependencies (no Room,
  no Ktor, no DataStore). Anything can depend on these.
- `core/*Impl` (`coreDatabaseRoom`, `coreNetworkKtor`, `corePrefDatastore`) — the concrete
  implementation. **Only `diApp` may depend on an `*Impl` module.**

`ui/*` and `navigation` depend on `*Api` modules directly (e.g. `uiMain` depends on
`coreDatabaseApi` and `corePrefApi`) but never on an `*Impl` module. Apps (`androidApp`,
`desktopApp`, `webApp`) don't depend on `core/*` or `ui/*` at all — they depend only on
`diApp`, which is where every implementation is wired together.

```
app/*  →  diApp  →  core/*Impl  →  core/*Api  ←  ui/*, navigation
```

The `*Impl` half of this is enforced by Gradle: the root `build.gradle.kts`
`checkModuleBoundaries` task fails the build if any module other than `diApp` declares a
dependency on `coreDatabaseRoom`, `coreNetworkKtor`, or `corePrefDatastore` — see
`docs/quality-gates.md`. Adding a new `core/*Impl` module means adding its path to that
task's `coreImplModulePaths` set too. Everything else about the module map (`ui/*` and
`navigation` depending on `*Api` modules, apps depending only on `diApp`) is still a
followed convention, not a Gradle-checked one.

## How wiring works (Koin)

`diApp`'s `DiCommonModule` (`diApp/src/commonMain/kotlin/template/di/DiCommonModule.kt`)
is a single `@Module @ComponentScan(["template.di"])` class. Implementation classes across
every module annotate themselves with `@Single` (see `DatabaseRepositoryRoom`,
`PrefServiceDataStore`, and the per-platform providers under
`androidMain`/`iosMain`/`jvmMain`/`webMain`); Koin's KSP compiler plugin generates the
bindings from these annotations. There are no hand-written `single<Api> { Impl() }`
blocks — adding a new `*Impl` module is enough to make `diApp` pick it up, as long as the
module is added as a `commonMain` dependency of `diApp` in its `build.gradle.kts`.

## Adding a new module

1. Create `core/<name>Api` and (if it needs a concrete backend) `core/<name>Impl` under
   `core/`, each applying `id("composeMultiplatformConvention")`.
2. Register both in `settings.gradle.kts`.
3. Add the `*Impl` module (only) as a `commonMain` dependency of `diApp`.
4. Add both modules to the `kover { dependencies { kover(projects...) } }` block in the
   root `build.gradle.kts` so coverage is aggregated.
