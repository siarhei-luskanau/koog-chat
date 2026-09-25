# Testing

Six test mechanisms coexist in this repo. This is what each is for and when to reach
for it.

## `commonTest` — default choice

Kotlin Multiplatform common test source set; compiles and runs on every target that
includes it (`jvm`, Android via `androidHostTest`, `js`, `wasmJs`, iOS via
`iosSimulatorArm64Test`). Use this for anything that doesn't need a platform-only API:
pure logic, repository/ktor-client tests (see `coreNetworkKtor`'s
`TestCoreNetworkKtorModule` + `ktor-client-mock`), and Compose UI logic via
`runComposeUiTest`.

`diApp`'s `KoinAppCommonTest` is the true end-to-end UI-flow test: it boots the real
`KoinApp()` composable with the real Koin graph and real navigation graph, pauses
`mainClock` to assert the initial `Splash:` text is on screen, then resumes the clock and
asserts the real `SplashNavigationCallback` → `AppNavigation` → `NavDisplay` round trip
lands on the `Main` screen. This is what catches interface mismatches and navigation
misconfiguration that isolated screen tests (`SplashScreenCommonTest`,
`MainScreenCommonTest`) structurally can't see.

**Default here unless you have a specific reason to use one of the source sets below.**

## `jvmTest` — Roborazzi screenshot capture

JVM-only tests. Every module with `id("roborazziConvention")` applied
(`uiCommon`, `uiSplash`, `diApp`, and — per `docs/TASKS.md` rows 9/11 — `uiChatList`,
`uiChat`, `uiLlmConfig`, `uiAuth`, `uiSettings` once they exist; `uiMain` is removed) gets
a screenshot test auto-generated for
every `@Preview` composable (via `roborazzi.generateComposePreviewRobolectricTests`), run
under `jvmTest` using the desktop Compose renderer. You don't write these by hand — add a
`@Preview`, then:

- `./gradlew recordRoborazzi` — record/update the reference images (commit them under
  `src/screenshots/`)
- `./gradlew verifyRoborazzi` — compare against the recorded reference (CI-enforced)

## `androidHostTest` — Robolectric (Android APIs on the JVM)

Use when a test needs real Android framework classes but not a device/emulator. CI runs
this as `testAndroidHostTest`. The convention plugin excludes `*CommonTest*` here so the
common suite isn't executed twice.

Gotcha: `BundledSQLiteDriver` (used by `coreDatabaseRoom`) resolves the Android artifact
under Robolectric, whose native loader calls `System.loadLibrary` and fails with
`UnsatisfiedLinkError` on a bare host JVM. `roborazziConvention.gradle.kts` works around
this by extracting `sqlite-bundled-jvm`'s natives and pointing
`androidx.sqlite.driver.bundled.path`/`.name` system properties at them for any
`*AndroidHostTest*`/`*IosSimulator*` test task. If you add a module with Room tests
outside modules that already apply `roborazziConvention`, you need this wiring too.

## `androidTest` — instrumented, real device/emulator

Only `app:androidApp` has this today (`AppAndroidTest` launches the real `AppActivity`
via `ActivityScenario`). Runs against a managed device in CI:
`managedVirtualDeviceDebugAndroidTest` (unit-style) and
`managedVirtualDeviceAndroidDeviceTest` (full instrumentation), both using
`-Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect`. Reach for this
only when you need real activity lifecycle/permissions/hardware behavior — everything
else belongs in `androidHostTest` or `commonTest`.

## `jsBrowserTest` / `wasmJsBrowserTest` — browser targets

Run `commonTest` plus any `js`/`wasmJs`-specific tests in headless Chrome via Karma.
Both `jsBrowserTest` and `wasmJsBrowserTest` work with no extra Karma setup — skiko's
runtime resources are picked up automatically by the default `js { browser() }` /
`wasmJs { browser() }` configuration in `composeMultiplatformConvention.gradle.kts`.

`coreDatabaseRoom`'s `webMain` uses a sql.js-backed Web Worker (`worker/worker.js`,
npm package `sql-js-worker`) as the SQLite driver for both `js` and `wasmJs`. If you
touch that worker or add an npm dependency there, run
`./gradlew kotlinWasmUpgradeYarnLock` afterward. The worker must be constructed as a
single `new Worker(new URL(...))` expression (see `WebRoomDatabaseProvider.kt`) — webpack
only detects and bundles the worker as a separate chunk when it sees that exact pattern
statically.

## `iosSimulatorArm64Test` — iOS simulator

Runs `commonTest` + `iosTest` on a macOS CI runner. Same sqlite-bundled native workaround
as `androidHostTest` applies here (see above).

Gotcha: Compose Multiplatform's `ui-uikit` cinterop klib hardcodes
`-L/Applications/Xcode_26.4.app/.../usr/lib/swift/iphonesimulator` in its `linkerOpts`,
so linking fails with `library 'swiftCompatibility51' not found` wherever Xcode lives
elsewhere. `composeMultiplatformConvention` adds the local toolchain's Swift lib dir
(from `xcode-select -p`) to every Apple binary's linker opts. Keep it until a CMP release
drops the hardcoded path.

Gotcha: test binaries run via `simctl spawn` without an app sandbox, so
`NSDocumentDirectory` is the simulator-wide `data/Documents`, shared with every other
project's iOS tests. Storage file names must stay unique to this app
(`koog_chat_app.*`); see `docs/DECISIONS.md`.

## Choosing a test type — quick reference

| You need to test... | Use |
|---|---|
| Pure logic, repository, ktor client, DI wiring | `commonTest` |
| A new `@Preview` composable renders correctly | Nothing to write — add `@Preview`, run `recordRoborazzi` |
| Something requiring Android `Context`/Robolectric | `androidHostTest` |
| Real activity lifecycle, permissions, hardware | `app/androidApp`'s `androidTest` (managed device) |
| Browser-only behavior | `jsBrowserTest` / `wasmJsBrowserTest` |
| iOS-only behavior | `iosSimulatorArm64Test` (`iosTest` source set) |
| A real user flow across screens (splash → main) | `diApp`'s `KoinAppCommonTest` (`commonTest`) |

## Auth/sync — always test against `*Fake`, never real Firebase

No automated test anywhere in this repo talks to real Firebase/KMPAuth. `coreAuthFake`
and `coreSyncFake` (see `docs/architecture.md`) are what `commonTest` binds for anything
exercising sign-in state or sync — the same way `coreNetworkKtor`'s
`TestCoreNetworkKtorModule` + `ktor-client-mock` keep network tests off the real network.
Three things specifically need `commonTest` coverage once `coreAuthApi`/`coreSyncApi`
exist:

- **LWW merge logic** — pure function over `(local updatedAt, remote updatedAt, isDeleted)`
  triples; deterministic, no coroutines/IO needed, easy to hit every ordering case.
- **Auth-state → sync-activation wiring** — a fake `AuthApi` flipping from signed-out to
  signed-in should be observed to start `coreSyncFake`'s (or a test double's) `start()`,
  and signing out should stop it without touching already-written Room rows.
- **Streaming/session-manager throttle logic** — a fake `LlmService` emitting a canned
  `StreamFrame` sequence (`TextDelta`×N, `End`) drives `LlmSessionManager`; assert the
  ~150ms persistence throttle doesn't drop the final chunk and that `ChatEntryType`
  finalizes to `SUCCESS_RESPONSE`/`ERROR_RESPONSE` correctly.
