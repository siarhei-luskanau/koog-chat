# Quality Gates

The exact commands CI runs, restated so you don't have to parse `.github/workflows/ci.yml`
to find your own Definition of Done. See `docs/testing.md` for what each test target
covers.

## Layer 0 — feature list integrity (planned, not yet wired)

`docs/features.json` is the machine-checkable Definition of Done for user-facing
features (see `docs/agent-workflow.md`). A `checkFeatureList` Gradle task is planned
(`docs/TASKS.md` row 13) to fail the build if: the file isn't valid JSON, any entry is
missing one of the fields documented in `features.json`'s own header (`id`, `behavior`,
`verificationCommand`, `state`, `relatedTasks`), any entry is `passing` with an empty
`verificationCommand`, or — only for entries whose `verificationCommand` begins with
`./gradlew` — the referenced task doesn't exist. Entries whose `verificationCommand`
begins with `manual:` are exempt from that last check by design; there is no
"schema" file beyond this convention. Until the task exists, treat pass-state gating as
a manual discipline: never flip a `docs/features.json` or `docs/TASKS.md` row to
`passing` without having actually carried out its verification procedure.

## Layer 1 — static analysis (`Lint` job)

```
./gradlew ktlintFormat                                  # auto-fixes style, run this first
./gradlew ktlintCheck detekt lint checkModuleBoundaries  # fails the build if anything is left unfixed
```

CI runs `ktlintFormat` and auto-commits the result on the PR branch, then fails the
`Lint` job if `ktlintCheck detekt lint checkModuleBoundaries` still finds anything. Run
these locally before pushing so CI doesn't do the formatting for you.

`checkModuleBoundaries` (root `build.gradle.kts`) is the executable form of the
`*Api`/`*Impl`/`*Fake` rule in `docs/architecture.md`: it inspects every subproject's
declared dependencies and fails with a listed violation (`<module> -> <core/*Impl
module>`) if anything other than `:diApp` depends on `coreDatabaseRoom`,
`coreNetworkKtor`, or `corePrefDatastore`. As `coreLlmKoog`, `coreAuthFirebase`,
`coreAuthFake`, `coreSyncFirestore`, and `coreSyncFake` are built out (see
`docs/TASKS.md`), each one's path goes into `coreImplModulePaths` too — that list has no
special case for `*Fake` vs `*Impl`, both are backend modules `diApp` alone may depend
on.

## Layer 2 — tests (`Tests` job, matrix)

```
./gradlew jsBrowserTest
./gradlew jvmTest
./gradlew testAndroidHostTest
./gradlew wasmJsBrowserTest
./gradlew managedVirtualDeviceDebugAndroidTest managedVirtualDeviceAndroidDeviceTest \
    -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
```

(macOS runner only) `./gradlew iosSimulatorArm64Test`

You don't need to run every target for every change — run the ones for the
target(s)/module(s) you touched. Reports land in `**/build/reports/`.

## Layer 2b — coverage (`Coverage` job)

```
./gradlew koverXmlReportCoverage koverHtmlReportCoverage koverVerifyCoverage
```

- `koverVerifyCoverage` fails the build if the aggregated `coverage` variant drops below
  the **70% minBound** rule in the root `build.gradle.kts`.
- HTML report: `build/reports/kover/htmlCoverage/index.html`
- XML report: `build/reports/kover/reportCoverage.xml`
- Excluded from coverage: `*ComposableSingletons*`, generated `*_Impl` classes, Koin KSP
  codegen (`org.koin.ksp.generated`), generated Compose resources.
- Each library module merges its `jvm` + `android` coverage into a custom `coverage`
  report variant (the AGP KMP library plugin exposes an `android` variant, not
  `debug`/`release` — see `composeMultiplatformConvention.gradle.kts`), aggregated at the
  root.

## Layer 3 — the app actually runs

Not currently CI-enforced as a gate beyond building the artifact, but this is what
"feature complete" requires before calling a change done: the app launched on at least
one affected target, not just "it compiles."

```
./gradlew :app:desktopApp:run              # cheapest — no emulator/simulator needed
./gradlew verifyRoborazzi                  # if you touched any @Preview composable
```

CI's `Android`, `Desktop`, `WebJs`, `WebWasmJs`, and `iOS` jobs build (not run) each
target's artifact — `assembleDebug`/`assembleRelease`, `:app:desktopApp:jar`,
`jsBrowserDistribution`, `wasmJsBrowserDistribution`, `ciIos`. A green build on these is
necessary but not sufficient — it doesn't prove the app actually launches.

## Screenshot verification (`VerifyScreenshotMatrixSetup` + `VerifyScreenshot` jobs)

```
./gradlew recordRoborazzi   # record/update reference images after a UI change
./gradlew verifyRoborazzi   # compare against committed references (CI-enforced)
```

Reference images live under each module's `src/screenshots/`. CI builds the job matrix
dynamically (`ciVerifyScreenshotJobsMatrixSetup`) and runs `verifyRoborazzi` per module;
diffs on failure are uploaded as `roborazzi-diff-<module>-<os>` artifacts.

## Where to look when a gate fails

| Gate | Artifact / report |
|---|---|
| `ktlintCheck` / `detekt` / `lint` | Console output; detekt HTML at `**/build/reports/detekt/` |
| `checkModuleBoundaries` | Console output — the failure message lists every `<module> -> <core/*Impl module>` violation |
| Test targets | `**/build/reports/` (uploaded as `reports-<task>-<os>` on CI) |
| `koverVerifyCoverage` | `build/reports/kover/htmlCoverage/index.html`, uploaded as `coverage-report` on CI |
| `verifyRoborazzi` | Diff images under the failing module's `build/outputs/roborazzi/`, uploaded as `roborazzi-diff-<module>-<os>` on CI (failed jobs only) |
