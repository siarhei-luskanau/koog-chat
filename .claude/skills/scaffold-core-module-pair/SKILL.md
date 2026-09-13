---
name: scaffold-core-module-pair
description: Scaffold a new core/*Api + core/*Impl module pair (interface module + implementation module), following this repo's api/impl architecture convention end to end — module creation, settings.gradle.kts registration, diApp wiring, kover aggregation, and the checkModuleBoundaries allowlist. Use when asked to add a new core capability/service module, e.g. "add a core module for analytics" or "add an image-loading core module".
---

# Scaffold a core/*Api + core/*Impl module pair

This is the recurring, mechanical task described in `docs/architecture.md`'s "Adding a
new module" section — read that doc first if you haven't this session. This skill is the
executable checklist version of those rules, not a replacement for them.

## Inputs needed from the user

- **Capability name**, PascalCase, e.g. `Analytics` → Api module `coreAnalyticsApi`.
- **Implementation technology**, short PascalCase name, e.g. `Firebase` → impl module
  `coreAnalyticsFirebase` (mirrors the existing `coreDatabaseRoom` / `coreNetworkKtor` /
  `corePrefDatastore` naming — tech name, not a literal `Impl` suffix).

If either is missing or ambiguous, ask before creating any files — don't guess a
technology or invent a capability the user didn't ask for.

## Steps

1. **Api module** — create `core/core<Capability>Api/build.gradle.kts`:

   ```kotlin
   plugins {
       id("composeMultiplatformConvention")
   }

   kotlin {
       android.namespace = "template.core.<capability-lowercase>.api"
   }
   ```

   and `core/core<Capability>Api/src/commonMain/kotlin/template/core/<capability-lowercase>/<Capability>Service.kt`
   containing an interface (plus any plain data models it needs) — zero implementation
   dependencies (no Room, Ktor, DataStore, or any other backend library). Model it on
   `core/corePrefApi/src/commonMain/kotlin/template/core/pref/PrefService.kt`.

2. **Impl module** — create `core/core<Capability><Tech>/build.gradle.kts`:

   ```kotlin
   plugins {
       id("composeMultiplatformConvention")
   }

   kotlin {
       android.namespace = "template.core.<capability-lowercase>.<tech-lowercase>"
       sourceSets {
           commonMain.dependencies {
               implementation(projects.core.core<Capability>Api)
           }
       }
   }
   ```

   Add whatever third-party dependency the technology needs — check `gradle/libs.versions.toml`
   for an existing alias first, add one under `[libraries]` only if missing. Implement the
   interface in a class annotated `@Single`. Koin's KSP `@ComponentScan` in `diApp` picks
   up `@Single`-annotated classes automatically — never hand-write a
   `single<Api> { Impl() }` binding (see `AGENTS.md` constraint #6).

3. **Register in `settings.gradle.kts`** — add both new module paths to the `include(...)`
   list, alphabetically among the existing `:core:*` entries.

4. **Wire into `diApp`** — add the **Impl module only** (never the Api module directly,
   unless nothing else already pulls it in) as a `commonMain` dependency in
   `diApp/build.gradle.kts`.

5. **Root `build.gradle.kts`**:
   - Add both new modules to the `kover { dependencies { kover(projects...) } }` block,
     alphabetically, so coverage stays aggregated.
   - Add the Impl module's path (`:core:core<Capability><Tech>`) to the
     `checkModuleBoundaries` task's `coreImplModulePaths` set — this is what turns the
     "*Impl* modules are diApp-only" rule into something CI enforces for the new module
     too.

6. **Update `docs/architecture.md`**'s module map table with the two new rows.

## Verification

Run, in order, and require all four green before calling the pair "scaffolded":

```
./gradlew ktlintFormat ktlintCheck detekt
./gradlew :core:core<Capability>Api:build :core:core<Capability><Tech>:build
./gradlew checkModuleBoundaries
./gradlew :diApp:jvmTest --tests "template.di.KoinAppCommonTest"
```

This mirrors the Layer 1/Layer 2 termination criteria in `docs/quality-gates.md` —
"compiles" alone isn't done.

## Non-goals

- Wiring the new capability into any `ui/*` screen is a separate, non-mechanical task —
  this skill stops once the module pair exists and builds clean.
- If the capability has no concrete backend yet (pure computation, no I/O), an Api-only
  module is fine — skip step 2 and the Impl bullet in step 5.
