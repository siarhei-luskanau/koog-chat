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

## `*Fake` as a third module kind alongside `*Api`/`*Impl`, for auth and sync only

**Decision:** `coreAuthApi` and `coreSyncApi` each get two backend modules —
`coreAuthFirebase`/`coreSyncFirestore` (real) and `coreAuthFake`/`coreSyncFake` (no-op) —
and `diApp` picks which pair to depend on based on whether a Firebase config is present
at build time, not at runtime.

**Rejected alternative:** a single `coreAuthFirebase`/`coreSyncFirestore` implementation
with an internal `if (Firebase.isConfigured) ... else noop()` branch. Rejected for two
reasons: it would force the Firebase/KMPAuth/Koog-client dependencies into every build
regardless of whether the developer ever configures Firebase, and it would make "app
works fully offline and unauthenticated" untestable in `commonTest` without either
mocking Firebase internals or accepting that this critical path is only exercised
manually. A compile-time module swap keeps the offline path real in tests (`coreAuthFake`
*is* what `commonTest` runs against, not a stand-in for something else) and keeps
Firebase entirely absent from a build that never configures it.

**Non-obvious cost:** two backend modules to keep behaviorally consistent per capability
instead of one — e.g. `coreSyncFake.syncState` must still emit the same `SyncState` shape
`coreSyncFirestore` would, just permanently `Idle`/`Disabled`, or `ui/uiSettings` would
need to special-case which implementation is bound.

## KMPAuth (Google-only) + GitLive `signInWithCredential`, not `kmpauth-firebase`

**Decision:** use `kmpauth-google` purely to obtain a Google ID token, then hand it to
GitLive's `firebase-kotlin-sdk` (`Firebase.auth.signInWithCredential`) for the actual
session. Firestore access relies on that same GitLive session.

**Rejected alternative:** KMPAuth's own `kmpauth-firebase` artifact, which ships an
independent Firebase Auth client (its own REST engine on JVM/WasmJs) rather than
integrating with GitLive's. Using it alongside GitLive's `Firebase.firestore` would leave
Firestore unauthenticated, since the two clients don't share session state at all.

**Non-obvious cost / open risk:** GitLive's own JVM (desktop) `Firebase.auth` is backed by
`firebase-java-sdk`, described by third parties as a minimal Auth implementation that may
need manual session-token persistence via `FirebasePlatform` internals to survive past a
single call. This needs explicit verification on the desktop target early — tracked in
`docs/TASKS.md` — before relying on it for anything beyond a proof of concept.

## `firebase-kotlin-sdk` pinned to `3.0.0-alpha02`, not the stable `2.7.0`

**Decision:** pin GitLive's `firebase-kotlin-sdk` to `3.0.0-alpha02`.

**Rejected alternative:** the latest stable `2.7.0`. Rejected because `2.7.0` has no
`wasmJs` target at all — `3.0.0-alpha02` is the release that adds `wasmJs` with (per its
release notes) full parity to the `js` target, which this app's target module list
requires (see `docs/architecture.md`).

**Non-obvious cost / open risk:** this repo's `libs.versions.toml` currently pins
`kotlinx-coroutines = "1.11.0"`; GitLive's own `libs.versions.toml` pins `1.10.2` around
the same alpha. Whether this is a real conflict (resolution failure, ABI mismatch on
wasm) or a non-issue (transitive resolution just picks one) is unverified — resolve it by
actually building the `wasmJs` target once `coreSyncFirestore`/`coreAuthFirebase` are
added, before assuming either version number is final. Track as an early item in
`docs/TASKS.md`, not something to guess at now.

## No `SyncLevel` setting — API keys never sync, everything else always does

**Decision:** there is exactly one sync scope. When signed in with Firebase configured,
chats, chat entries, and `LlmConfig` rows (minus `apiKey`) sync automatically; there is no
user-facing toggle for narrower or wider sync.

**Rejected alternative:** a tiered `SyncLevel` preference (e.g. chats-only vs.
chats-and-configs vs. also-API-keys) giving the user opt-in control over what syncs.
Rejected as unnecessary product surface for a single, clear security line: API keys are
secrets and should never leave the device, full stop, while everything else is
low-sensitivity chat content the user already expects to follow them across devices once
they've signed in. One scope means one code path to test instead of three, and removes an
easy way to accidentally opt into syncing a secret.

**Non-obvious cost:** if a future requirement genuinely needs finer-grained sync control,
introducing it later is itself a decision that belongs in a new entry here, not a silent
relaxation of hard constraint #12 in `AGENTS.md`.

## Adaptive list-detail navigation via Nav3 `ListDetailSceneStrategy`

**Decision:** the conversation list and chat detail screens are wired as the list/detail
panes of Navigation3's `ListDetailSceneStrategy`
(`androidx.compose.material3.adaptive.navigation3`), following the pattern already
proven in [pixabayeye's `NavApp.kt`](https://github.com/siarhei-luskanau/pixabayeye).

**Rejected alternative:** manually branching on `WindowSizeClass`/
`currentWindowAdaptiveInfo()` inside a single composable to decide one-pane vs. two-pane
layout. Rejected because `ListDetailSceneStrategy` already encapsulates that decision as
part of Navigation3's scene-strategy mechanism — reimplementing it manually would
duplicate logic Navigation3 already owns and this template already depends on
(`jetbrains-compose-material3-adaptive-navigation3` is already a dependency for the
existing `adaptive-navigation-suite` usage).

**Non-obvious cost:** this is a *different* adaptive mechanism from
`NavigationSuiteScaffold` (used elsewhere for bar/rail/drawer top-level switching, as seen
in pixabayeye's separate `AppNavigationSuiteScaffold.kt`) — Koog Chat doesn't currently
need a top-level tab switcher, so only the list-detail strategy is in scope. Don't conflate
the two if a future feature needs one.

## Koog (JetBrains) for the LLM layer, not direct per-provider SDKs or raw Ktor calls

**Decision:** `coreLlmKoog` is built on `ai.koog:koog-agents:1.2.0` rather than calling
OpenAI/Anthropic/Google/Ollama's HTTP APIs directly with `coreNetworkKtor`.

**Rejected alternative:** hand-rolled Ktor clients per provider (what koog-chat-1 would
have needed if it grew beyond Ollama on its own). Rejected because Koog already gives a
single streaming/tool-calling/history abstraction across all four providers this app
needs, and koog-chat-1's existing `LlmServiceKoog` already used Koog for its one provider
— extending an existing dependency to more providers it already supports is strictly
less work than replacing it with four bespoke clients.

**Non-obvious cost:** Koog has no HTTP auto-discovery on non-JVM KMP targets (iOS/JS/
WasmJs) — every non-JVM executor construction needs an explicit
`KtorKoogHttpClient.Factory()`, and no verified code sample for this was found during
research (see `docs/TASKS.md` row 3, the resulting spike). The Google/DeepSeek/Mistral
clients are also beta — pin `ai.koog` versions deliberately, don't float them.

## Last-write-wins on `updatedAt`, not a CRDT or server-authoritative merge

**Decision:** conflicting edits to the same row from two devices resolve by comparing
`updatedAt` timestamps — whichever write is newer wins outright, including a delete
(`isDeleted = true`) beating an older concurrent edit.

**Rejected alternative:** a CRDT-based merge (e.g. per-field merge, operational
transform) that could, in principle, preserve both concurrent edits to different fields
of the same row. Rejected as disproportionate for chat data: a `ChatEntry` is
effectively append-only in normal use (users don't collaboratively edit the same message
from two devices at once), so the only conflicts LWW handles worse than a CRDT would are
edge cases (near-simultaneous edits to the *same* row's metadata, e.g. renaming a `Chat`
on two devices within the same sync interval) that are rare, low-stakes, and easy for a
user to notice and redo — not worth the implementation and testing cost of a merge
algorithm.

**Non-obvious cost:** clock skew between devices could make LWW pick the "wrong" (older
wall-clock, later intent) write in the near-simultaneous case above. `updatedAt` is set
locally by the writing device, not by a Firestore server timestamp — if this proves to be
a real problem in practice (not just a theoretical one), switching to
`FieldValue.serverTimestamp()` semantics is the fix, and would need its own
`docs/DECISIONS.md` entry since it changes the merge rule's authority model.

## One centralized `docs/architecture.md`, not a per-module `ARCHITECTURE.md` alongside each module

**Decision:** module architecture lives in a single `docs/architecture.md` describing the
whole target module map, rather than a separate `ARCHITECTURE.md` file next to each
module's `build.gradle.kts`.

**Rejected alternative:** a per-module doc placed adjacent to the code it describes (the
harness-engineering literature's usual recommendation, on the theory that knowledge
adjacent to code is cheaper to find and keep current than a centralized doc). Rejected
*for now* because this repo is still pre-code — every module in the target map beyond the
existing 15 baseline modules doesn't exist yet, so a per-module doc would sit next to
nothing. Revisit this once `docs/TASKS.md` rows start landing real modules: a reasonable
rule at that point is a short per-module `ARCHITECTURE.md` stub for any module whose
constraints aren't obvious from its own code, while `docs/architecture.md` keeps the
cross-module picture (dependency graph, the `*Api`/`*Impl`/`*Fake` rule) no single
module's doc could own anyway.

**Non-obvious cost:** until that revisit happens, a session working on one module has to
read the whole architecture doc, not just its section — acceptable while the module count
is small, worth re-checking once most of the target map exists.

## Hard constraints stay inline in `AGENTS.md`, not a separate `CONSTRAINTS.md`

**Decision:** the numbered "Hard constraints" section lives directly in `AGENTS.md`
(13 items) rather than in its own `docs/CONSTRAINTS.md` file.

**Rejected alternative:** splitting constraints into a dedicated file, on the theory that
it keeps the entry file shorter. Rejected because the entry-file budget this repo targets
(roughly 50-200 lines, with room for up to about 15 global hard constraints inline) is
exactly what `AGENTS.md` is sized to — at 13 constraints and under 200 lines, moving them
out would trade a one-file read for a two-file read without buying back any of the budget
that actually matters. If the constraint count grows past ~15 or the file starts pushing
past 200 lines, that's the trigger to split, not a fixed preference for one file over two.

## JVM desktop data lives under `~/.koog-chat-app`, not `~/.koog-chat`

**Decision:** the JVM `RoomDatabaseProvider` and DataStore `AppStorageProvider` store data
under `~/.koog-chat-app/{room,datastore}`, named after the app id `koog.chat.app`.

**Rejected alternative:** `~/.koog-chat`, the obvious name after the row-1 rename.
Rejected because the porting source koog-chat-1 already uses exactly
`~/.koog-chat/room/koog_chat.db` and `~/.koog-chat/datastore/app.pref.json` with a
different Room schema. Sharing the path made Room fail its identity-hash check at runtime
("Room cannot verify the data integrity"), and a DataStore write from this app would
overwrite koog-chat-1's preferences. Other platforms are sandboxed per app id or per
origin, so only JVM needed a distinct directory.

**Non-obvious cost:** the JVM `commonTest`/`jvmTest` suites for `coreDatabaseRoom` and
`corePrefDatastore` write to this real home directory, not a temp dir. That's inherited
from the template, not introduced here, but it is why the collision surfaced as a test
failure.
