# Decisions

Append-only log of non-obvious architectural choices, with the alternatives that were
rejected and why. This is not a changelog — routine dependency bumps and mechanical
fixes don't belong here. Add an entry when a decision would otherwise have to be
re-derived from scratch (or worse, silently reversed) by the next session.

## Room 3 KMP + a hand-rolled Web Worker for wasmJs/js SQLite

**Decision:** use `androidx.room3` (Room 3.0 KMP) for `coreDatabaseRoom`, backed on
web (`js` + `wasmJs`) by a `WebWorkerSQLiteDriver` talking to a local `sql-js-worker`
npm package (`core/coreDatabaseRoom/worker/`) that wraps `sql.js`.

**Rejected alternative:** SQLDelight, which has more mature multi-target driver support
today. Rejected because the project standardizes on Room across all targets (Android,
iOS, JVM, and web) for a single query-generation story, and Room 3's KMP support now
covers web via the worker pattern — the project accepted the extra web-only setup cost
described below in exchange for that consistency.

**Non-obvious cost this decision carries:** Room on web needs a *real* Worker, not a
same-thread shim, and Webpack only bundles the worker as a separate chunk when it sees
`new Worker(new URL(...))` as a single, statically-analyzable expression. See
`WebRoomDatabaseProvider.kt` and `core/coreDatabaseRoom/worker/`. Splitting that
expression into a helper function silently breaks the web build (the browser gets served
the raw ES-module `worker.js` instead of the bundled chunk).

**Related:** Robolectric (`androidHostTest`) can't load Room's bundled SQLite driver's
Android native lib on the host JVM by default; `roborazziConvention.gradle.kts` extracts
`sqlite-bundled-jvm`'s natives and points `androidx.sqlite.driver.bundled.path`/`.name`
system properties at them. See `docs/testing.md`.

## Navigation3 + adaptive-navigation-suite over classic Navigation

**Decision:** the `navigation` module uses `androidx.navigation3` +
`adaptive-navigation-suite`, not the older single-Activity `androidx.navigation` /
`NavHost` API.

**Rejected alternative:** classic Navigation Compose (`NavHost` + `NavController`).
Rejected because it has no first-class multiplatform story for adaptive layouts
(list-detail, nav-rail vs. bottom-bar) across the five targets this template ships —
`adaptive-navigation-suite` gives that for free, and Navigation3 is the JetBrains/Google
direction for Compose Multiplatform navigation going forward.

**Non-obvious cost:** Navigation3 is younger and its API surface moves faster between
releases than classic Navigation — expect more frequent breaking changes on version
bumps than a typical Dependabot dependency.

## Kover coverage variant is `"android"`, not `"debug"`, for KMP Android modules

**Decision:** the custom Kover `coverage` variant in
`composeMultiplatformConvention.gradle.kts` adds the Android target as
`add("android", optional = true)`.

**Rejected alternative (found the hard way):** `add("debug", optional = true)`, which is
correct for the classic `com.android.application`/`com.android.library` plugins but
silently produces a JVM-only aggregated report under
`com.android.kotlin.multiplatform.library` (the KMP Android plugin this project uses) —
`optional = true` hides the mismatch instead of failing loudly, since there genuinely is
no `debug` variant to report on. `testAndroidHostTest` coverage only shows up in
`koverXmlReportCoverage`/`koverHtmlReportCoverage` output once the variant name is
`"android"`.

## `checkModuleBoundaries` only enforces the `*Impl` half of the architecture rule

**Decision:** the executable check (root `build.gradle.kts`) fails the build only when a
module other than `diApp` depends on `coreDatabaseRoom`, `coreNetworkKtor`, or
`corePrefDatastore`. It does not restrict who may depend on a `ui/*` module.

**Rejected alternative:** an earlier phrasing of this work item read "fail the build if a
`ui/*` **or** `core/*Impl` module is depended on by anything other than `diApp`". That's
wrong for this codebase: `navigation` legitimately depends on `ui/uiMain` and
`ui/uiSplash` to wire screens into `koinEntryProvider()`, and `diApp` depends on all three
`ui/*` modules directly for the same reason. Enforcing "only `diApp` may depend on
`ui/*`" would fail the build on the very wiring the app needs to run. The real, useful
constraint — and the one already stated as hard constraint #2 in `AGENTS.md` and spelled
out in `docs/architecture.md` — is the `*Impl` half only.

**Implementation note:** the check can't run as a normal task-execution-time (`doLast`)
closure that walks `subprojects`/`configurations` directly — Gradle's configuration cache
rejects serializing live `Project`/`Configuration` references. It runs inside
`gradle.projectsEvaluated { ... }` (after every subproject's `build.gradle.kts` has
declared its dependencies, but still during configuration), computes a plain
`List<String>` of violations there, and only that value is captured by the task's
`doLast`.

## `KoinAppCommonTest` pauses `mainClock` to observe the pre-navigation frame

**Decision:** the E2E navigation test (`diApp/src/commonTest/.../KoinAppCommonTest.kt`)
sets `mainClock.autoAdvance = false` before `setContent { KoinApp() }`, asserts the
`Splash:` text, then sets `autoAdvance = true` and asserts the `Main:` text.

**Why it's needed:** `SplashScreen`'s `LaunchedEffect(Unit) { onEvent(Launched) }` calls
`SplashViewModel.onEvent`, which launches a `viewModelScope` coroutine that immediately
calls `navigationCallback.goMainScreen(...)` — real Compose UI test idling
(`waitForIdle`/`awaitIdle`, and even the implicit sync inside `setContent`) drains pending
coroutines regardless of `mainClock`, so without pausing the clock the composition already
shows `Main` by the time any assertion runs; there'd be no way to prove `Splash` was ever
rendered by the real graph rather than skipped straight to `Main`. `mainClock.autoAdvance`
only gates frame-based work (recomposition triggered by the test's synchronization loop),
which is enough to hold the first frame steady for the assertion.
