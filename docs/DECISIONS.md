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

**Superseded** by "`*Fake` selection via `IS_FAKE_DATA_ENABLED`, not config-file
presence" (2026-09-28).

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
actually building the `wasmJs` target once `coreSyncFirebase`/`coreAuthFirebase` are
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

**Decision:** `coreLlmKoog` is built on `ai.koog:koog-agents` rather than calling
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

## Local storage is named `koog-chat-app`/`koog_chat_app`, not `koog-chat`/`koog_chat`

**Decision:** the JVM `RoomDatabaseProvider` and DataStore `AppStorageProvider` store data
under `~/.koog-chat-app/{room,datastore}`, and on every platform the files are named
`koog_chat_app.db` and `koog_chat_app.pref.json`, after the app id `koog.chat.app`.

**Rejected alternative:** `~/.koog-chat`, the obvious name after the row-1 rename.
Rejected because the porting source koog-chat-1 already uses exactly
`~/.koog-chat/room/koog_chat.db` and `~/.koog-chat/datastore/app.pref.json` with a
different Room schema. Sharing the path made Room fail its identity-hash check at runtime
("Room cannot verify the data integrity"), and a DataStore write from this app would
overwrite koog-chat-1's preferences. The iOS simulator has the same problem for tests:
Kotlin/Native test binaries run via `simctl spawn` with no app sandbox, so
`NSDocumentDirectory` is the simulator-wide `data/Documents` shared by every project's
iOS tests, where koog-chat-1's `koog_chat.db` and `app.pref.json` already sit. The real
apps are sandboxed per bundle id/origin; the distinct file names are what keep tests
from colliding.

**Non-obvious cost:** the JVM `commonTest`/`jvmTest` suites for `coreDatabaseRoom` and
`corePrefDatastore` write to this real home directory, not a temp dir. That's inherited
from the template, not introduced here, but it is why the collision surfaced as a test
failure.

## `LlmConfigRepository.getAllFlow()` does not seed a default config

**Decision:** the ported `LlmConfigRepositoryRoom.getAllFlow()` only maps rows. A fresh
install starts with zero `LlmConfig`s until the user adds one (`ui/uiLlmConfig`, row 12).

**Rejected alternative:** koog-chat-1's behavior of inserting a hardcoded Ollama config
(a personal ngrok URL) from an `onEach` inside the flow whenever the table is empty.
Rejected because a read path with a write side effect is surprising: it re-seeds after
the user deletes every config, and with sync (row 7) it would push that row to every
signed-in device. It also bakes a private endpoint into the app. If a first-run default
is wanted, it belongs in an explicit onboarding step, not in the repository's read flow.

## Koog streaming on non-JVM targets needs a per-platform `flowOn` (row 3 spike)

**Finding (2026-09-24, Koog 1.3.0, Ollama 0.34.4 `qwen3.5:0.8b` on localhost):** building
`OllamaClient(httpClientFactory = KtorKoogHttpClient.Factory(HttpClient()), baseUrl)` from
`commonMain` works on iOS simulator, JS browser and WasmJs browser. Non-streaming
`execute()` returned a real response on all three. `executeStreaming()` fails on all three
with `KoogHttpClientException: ... Flow invariant is violated`. Koog emits frames from a
different dispatcher than the collector's: `Dispatchers.IO` on iOS, the browser's
`WindowDispatcher` (`Dispatchers.Default`) on JS/WasmJs. JVM streaming works unchanged.
This isn't a test artifact: any collector not already on that dispatcher (e.g. a
`viewModelScope` on Main) hits the same invariant.

**Decision:** `coreLlmKoog` (row 4) applies `.flowOn(streamingDispatcher)` to
`executeStreaming()`, using an `expect val` that is `Dispatchers.IO` on iOS and
`Dispatchers.Default` on web (`Dispatchers.IO` isn't available there). JVM/Android can use
`Dispatchers.IO`. Verified by the spike: with that `flowOn`, streaming returned a real
response on iOS, JS and WasmJs.

**Rejected alternative:** avoiding streaming on non-JVM targets and falling back to
`execute()`. Rejected because streamed tokens are core chat UX, and the `flowOn` fix is
a one-line, platform-scoped workaround. Revisit when a Koog release fixes the emission
context; the row-4 streaming test on each target will show when it's no longer needed.

**Test-harness note:** browser tests that make real network calls need Mocha's 2 s
default timeout raised via `<module>/karma.config.d/*.js`
(`config.set({ client: { mocha: { timeout: 180000 } } })`), otherwise they time out
before the model answers. OpenAI/Anthropic/Google executors weren't exercised (no keys);
they share the same `KtorKoogHttpClient` path, but only Ollama is proven.

## `coreLlmKoog` shape: DispatcherSet for `flowOn`, session manager behind an interface (row 4)

**Decision:** the row-3 `flowOn` uses the injected `DispatcherSet.ioDispatcher()` from
`coreCommon`, not a new `expect val`. `coreCommon` already maps it to `Dispatchers.IO` on
JVM/Android/iOS and `Dispatchers.Default` on web, which is exactly the row-3 mapping. It's
also injectable, so tests use the same mapping (`platformIoDispatcherSet()`). A MockEngine
test on iOS with `Default` hit the flow invariant, and removing `flowOn` broke JS, so both
the fix and the tests guard it. `LlmSessionManager` is an interface in `coreLlmApi` with
`LlmSessionManagerImpl` in `coreLlmKoog`, so `ui/uiChat` (row 10) can depend on the Api
module alone.

**Rejected alternatives:** (a) an `expect val streamingDispatcher` in `coreLlmKoog`, which
duplicates `DispatcherSet` and can't be swapped in tests; (b) putting the concrete session
manager in `coreLlmApi`, which breaks constraint #1 (Api modules hold interfaces/models
only); (c) depending on `koog-agents`/`koog-agents-additions` as koog-chat-1 did, which
pulls in agents, MCP, memory and OpenTelemetry for what is only `LLMClient` streaming.

**Non-obvious cost:** the Koin wiring test (`CoreLlmKoogModuleJvmTest`) is JVM-only. Koin
compile safety reports a false KOIN-D002 for `commonMain` `@ComponentScan` bindings in the
js/wasm/native test klibs (the same issue `coreDatabaseRoom` works around by disabling
`compileSafety` for the whole module). Keeping compile safety on for main code was judged
worth more than running that single test on every target. Ktor's `MockEngine` doesn't
support SSE, so the provider stream tests wrap it (`SseMockEngine` in `TestFixtures.kt`)
to declare `SSECapability` and return a `DefaultClientSSESession`. That relies on Ktor
`@InternalAPI` and may need adjusting on a Ktor upgrade.

## `*Fake` selection via `IS_FAKE_DATA_ENABLED`, not config-file presence

**Decision:** auth and sync each ship as an `*Api` module plus two backend modules — a
no-op `*Fake` (`coreAuthFake`, `coreSyncFake`) and a real `*Firebase` (`coreAuthFirebase`,
`coreSyncFirebase`). `diApp` picks one pair with a plain `if`/`else` in
`diApp/build.gradle.kts`'s `commonMain.dependencies`, driven by a single explicit boolean
flag, `IS_FAKE_DATA_ENABLED`, read by `isFakeDataEnabled()` in
`buildSrc/src/main/kotlin/LocalPropertiesUtils.kt`: the `-DIS_FAKE_DATA_ENABLED=` JVM
system property (set on every CI/screenshot build) wins over the `IS_FAKE_DATA_ENABLED=`
key in `local.properties`. Both capabilities always switch together — never a mix of fake
and Firebase within the same build.

**Rejected alternative:** inferring "Firebase configured" from config-file presence
(`google-services.json` on Android). It's implicit: each platform's config artifact
differs (Android's `google-services.json`, iOS's `GoogleService-Info.plist`, desktop/web's
own `local.properties`-style keys), so "is Firebase configured" has no single, checkable
definition across all five targets without per-platform detection logic living inside
the build. A silent fallback to the fake variant when a file happens to be missing (e.g.
a `.gitignore`'d config not yet pulled locally) also hides a real misconfiguration behind
what looks like a normal, supported offline mode. A per-capability flag (e.g. separate
`IS_FAKE_AUTH_ENABLED`/`IS_FAKE_SYNC_ENABLED`) was also rejected: auth and sync only make
sense as one unit here (sync depends on `coreAuthApi.currentUser` to do anything), and two
independent flags would allow real auth with fake sync (or vice versa) — a combination
nothing in `docs/architecture.md`'s wiring section is designed to support.

**Non-obvious cost:** the flag defaults to `false` when unset, i.e. the *Firebase*
variant is the default — a fresh clone with no `local.properties` entry and no CI system
property builds against real Firebase and needs the setup in `docs/setup-firebase.md`
before it links. Anyone who wants the offline-only fake build locally must explicitly
set `IS_FAKE_DATA_ENABLED=true` in `local.properties`; it is not inferred from the
absence of Firebase config files.

## Auth/sync backend Koin modules auto-load via `@Configuration`, not `DiKoinApplication`'s list

**Decision:** `coreAuthFake`'s `CoreAuthFakeModule` (and, by the same rule, the future
`coreAuthFirebase`/`coreSyncFake`/`coreSyncFirebase` modules) is annotated
`@Module @Configuration @ComponentScan`. `DiKoinApplication`'s `@KoinApplication` picks up
every `@Configuration` module on the classpath, so it never names an auth/sync module
class. Verified with a throwaway `diApp` test resolving `AuthService` under
`-DIS_FAKE_DATA_ENABLED=true` (2026-09-28).

**Rejected alternatives:** listing `CoreAuthFakeModule::class` in `DiKoinApplication`
doesn't compile in the Firebase variant, where `coreAuthFake` isn't a dependency. Giving
the fake and Firebase modules the same fully qualified class name would compile both
ways, but two modules would declare one class, and it breaks the moment both are on a
test classpath. Per-flag `diApp` source sets would add build logic just to choose one
class reference.

**Non-obvious cost:** nothing in `diApp`'s source lists the auth/sync modules. Whether one
is bound depends on the Gradle `if`/`else` alone. Under `IS_FAKE_DATA_ENABLED=false`, with
`coreAuthFirebase` not built yet, the graph has no `AuthService` binding. That's harmless
until a consumer injects it (row 10+), and row 9's `KoinAppCommonTest` extension must
cover both flag values. `diApp/src/commonTestFake` (compiled only when the flag is true)
holds `AuthServiceCommonTest`, which resolves `AuthService` from `DiKoinApplication`.
It passes on jvm/js/wasmJs/iOS sim. The Koin compile-safety check still reports it as
KOIN-D002 "missing definition" on JS/WasmJs, because it can't see `@Configuration` modules
from dependency klibs. So `diApp` sets `koinCompiler { compileSafety = false }`, as
`corePrefDatastore`/`coreDatabaseRoom` already do. That also turns the compile-time check
off for `diApp`'s own graph. Passing a test-only `compileSafety=false` compiler argument
was tried and rejected by the plugin ("Multiple values are not allowed").

## iOS native SDKs via Swift Package Manager, with Kotlin's linkage package committed

**Decision:** KMPAuth 3.x needs GoogleSignIn through SwiftPM (it dropped CocoaPods). So
`app/iosApp/iosApp.xcodeproj` references only the local
`app/iosApp/KotlinMultiplatformLinkedPackage` that `:diApp:integrateLinkagePackage`
generates; `GoogleSignIn-iOS` comes in transitively through it. Both are committed along
with `Package.resolved`. Added 2026-10-04.

**Amended 2026-10-04:** the app target first also linked the `GoogleSignIn` product
directly (as KMPAuth's sample app does). With both the app and the dynamic
`KotlinMultiplatformLinkedPackageDylib` using it, Xcode moved GoogleSignIn into a separate
`GoogleSignIn_*_PackageProduct.framework`, leaving the dylib empty, while `ComposeApp` was
linked to resolve `GIDSignIn` from the dylib. Launch failed with
`dyld: Symbol not found: _OBJC_CLASS_$_GIDSignIn`. The direct product reference was
removed; Swift's `import GoogleSignIn` still resolves transitively.

**Rejected alternatives:** CocoaPods isn't supported by KMPAuth 3.x. Leaving out the
linkage package isn't an option, because Kotlin 2.4's `embedAndSign` integration fails the
Xcode build ("SwiftPM linkage package not integrated into Xcode project") whenever any
dependency declares `swiftPMDependencies`. Generating it in CI instead would mean CI
mutates the Xcode project on every run.

**Non-obvious cost:** the linkage package is generated from the Firebase variant's
dependency graph (`-DIS_FAKE_DATA_ENABLED=false`). Its subpackage names encode the
artifact and version (e.g. `io_github_mirzemehdi_kmpauth_google_3_0_6`), so every
SwiftPM-backed dependency change needs `integrateLinkagePackage` re-run and committed.
The command is in `docs/setup-firebase.md`. The fake variant still builds with that
package in place: verified with `xcodebuild` under both flag values. Running the task also
rewrote `project.pbxproj` without Xcode's inline `/* ... */` comments. That's
cosmetic, and Xcode restores them the next time it saves the project.

## Google ID token comes from a `@Composable` `GoogleIdTokenProvider`, not from `AuthService`

**Decision:** `coreAuthApi` exposes `GoogleIdTokenProvider.rememberGoogleSignInLauncher(onResult)`
(`@Composable`, returns a `GoogleSignInLauncher`), bound by both `coreAuthFirebase` (KMPAuth)
and `coreAuthFake` (fixed token). `AuthService` keeps `signInWithGoogleIdToken(idToken)`. The UI
wires the two together. Added 2026-10-04.

**Rejected alternatives:** a `suspend fun signInWithGoogle()` on `AuthService` doesn't work,
because KMPAuth 3.x only gives out its sign-in UI provider from a `@Composable`
(`GoogleAuthProvider.getUiProvider()`), and on Android it needs the current Activity's
context. Calling KMPAuth's `rememberGoogleSignInState` directly from `ui/uiAuth` was also
rejected: `ui/*` can't depend on `coreAuthFirebase` (constraint #2), and a fake-mode build
would have no token source without a runtime branch (constraint #11).

**Non-obvious cost:** an `*Api` module now has a Compose-typed member. That's fine, since
every module applies `composeMultiplatformConvention` and gets Compose runtime anyway, but it
means `coreAuthApi` can't be consumed by a non-Compose module. `KMPAuth.initialize` runs in
the `@Single`'s constructor, not at app start. KMPAuth keeps the first configuration, so
repeated Koin graphs (tests) are harmless.

## Zero-setup first launch: runtime `DefaultLlmSelector` with virtual configs, not a seeded row

**Decision:** a fresh install chats immediately. A chat without an explicit `LlmConfig`
uses `DefaultLlmSelector` (`coreLlmApi`, implemented in `coreLlmKoog`), which picks the
user's default config, else a reachable local Ollama with an installed model, else the
on-device model, else `None` (inline, non-modal "set up a model" state). The on-device and
local-Ollama choices are in-memory `LlmConfig`s with fixed ids (`builtin:on-device`,
`builtin:ollama-local`) that are never written to Room. Nothing gates the first chat on
config, sign-in, or sync (`AGENTS.md` constraint #14). Added 2026-10-07 from the
requirement "launch the app and chat immediately; set up, sign in, and sync later".

**Rejected alternatives:** (a) seeding a default `LlmConfig` row on first run. It
re-creates the problems in "`LlmConfigRepository.getAllFlow()` does not seed a default
config": it would sync a device-specific choice (a phone's Gemini Nano) to every device,
and it would need "re-seed?" logic after the user deletes it. (b) A first-run onboarding
screen that asks for a provider. That is exactly the setup step the requirement removes.
(c) Making sign-in a prerequisite for a "free" hosted model. That needs a backend and an
account, which contradicts both the requirement and the offline-first design.

**Non-obvious cost:** where no model is available (no supported device, no Chrome built-in
AI, no Ollama running), "chat immediately" degrades to "the chat screen opens immediately
and explains how to get a model". The requirement is fully met only on supported phones,
desktop Chrome, and desktops with Ollama installed. Because virtual configs aren't Room
rows, entries they answer are persisted with `llmConfigId = null`: `chat_entries` has a
foreign key to `llm_configs`. The denormalized `llmProvider`/`llmModelId` columns still
record the model. The rejected alternative was dropping that foreign key, which would
let dangling ids accumulate and would need a schema migration.

## On-device LLM via thin per-platform wrappers in `coreLlmOnDevice`, not `koog-ondevice`

**Decision:** `coreLlmApi.OnDeviceLlm` is implemented by our own `coreLlmOnDevice` module:
ML Kit GenAI Prompt API on Android, a Swift bridge to Apple `FoundationModels` on iOS
(implemented in `app/iosApp`, injected at startup), and Chrome's `LanguageModel` Prompt API
on js/wasmJs. JVM reports `Unavailable`. `LlmServiceKoog` routes `LlmProvider.OnDevice` to
it. Added 2026-10-07.

**Rejected alternatives:** (a) `dev.ynagai.koog:koog-ondevice` (a Koog `LLMClient` for
Gemini Nano and Foundation Models). It is v0.1.0 from a single maintainer with no
dependents, compatibility with Koog is unverified, and it has no web support. Each
platform wrapper is small, so owning it costs less than tracking an early dependency. Revisit
if that library matures. (b) Implementing Koog's `LLMClient` for on-device. That interface
is shaped around HTTP model catalogues and tool calling, which these system models don't
offer; a narrow `stream()` interface is easier to fake and test. (c) Bundled or downloadable
models (LiteRT-LM + Gemma on Android, WebLLM on WebGPU browsers, llama.cpp on desktop) as the
default. A 0.5–5 GB download is not "zero setup". They are deferred as an opt-in fallback, not
rejected.

**Non-obvious cost:** every backend is device-gated (supported phone models, iOS 26 +
Apple Intelligence, desktop Chrome with 22 GB free disk). That makes availability a runtime
fact this design has to model (`OnDeviceAvailability`) rather than a build-time one. CI
hardware always sees `Unavailable`, so real verification is manual on specific devices.
Context is about 4K tokens, which forces history trimming. ML Kit GenAI is still beta and
Chrome's Prompt API differs by version, so pin versions and keep availability checks
defensive. The iOS bridge adds Swift code to `app/iosApp`, the first non-trivial Swift in
the repo.

## Local Ollama outranks the on-device model; Android also probes the adb/emulator host

**Decision:** `DefaultLlmSelector` tries a reachable local Ollama **before** the on-device
model (user config → local Ollama → on-device → `None`). The probe isn't just
`localhost:11434`: `coreLlmKoog`'s `expect fun localOllamaBaseUrls()` adds
`http://10.0.2.2:11434` on Android, after `localhost`. `localhost` covers a device or
emulator where `adb reverse tcp:11434 tcp:11434` tunnels the development host's Ollama over
adb; `10.0.2.2` is the emulator's alias for the host's loopback, which works with no setup.
Candidates are probed in parallel with a ~1 s timeout, and the first responder in priority
order becomes the `builtin:ollama-local` config's `providerUrl`. Added 2026-10-08 at the
user's request; this changes the 2026-10-07 order recorded in "Zero-setup first launch".

**Rejected alternatives:** (a) keeping on-device first. A running Ollama only exists
because someone started it and pulled a model, which is a stronger signal of intent than a
system model that happens to be on the phone. Ollama models also aren't limited to a ~4K
window or per-app quotas. Ordinary users don't run Ollama, so for them the order doesn't
change anything. (b) Scanning the LAN or using mDNS to find Ollama on other machines. That
needs Ollama bound to `0.0.0.0`, Android's local-network permission and iOS's
local-network prompt, and it can silently pick a stranger's server on shared Wi-Fi. A
non-local server stays an explicit `LlmConfig`. (c) Allowing cleartext HTTP globally on
Android. The `network_security_config.xml` exception is limited to `localhost`,
`127.0.0.1` and `10.0.2.2`, so every remote provider stays HTTPS-only.

**Non-obvious cost:** `adb reverse` lasts only as long as the adb connection, so Ollama can
appear or vanish while the app is running. The selector already re-evaluates on app resume,
but a turn already in flight against a vanished host fails like any other network error. A
physical iPhone has no adb-reverse equivalent, so on iOS only the simulator ever finds a
local Ollama.

## `SyncState` is `Idle`/`Syncing`/`Error`, with no `Disabled`/`SignedOut` state

**Decision:** `coreSyncApi.SyncService` exposes `syncState: StateFlow<SyncState>` with three
states: `Idle`, `Syncing` and `Error(message)`. `Idle` means "not syncing right now", whether
the user is signed out, sync is stopped, or everything is caught up. `start()`/`stop()` are
non-suspend; `coreSyncFirebase` will own its Firestore listeners in its own scope, and
`start()` must be idempotent there. `coreSyncFake` follows the calls with no backend:
`start()` → `Syncing`, `stop()` → `Idle`, ignoring auth, so a UI can exercise both states. Added 2026-10-08
(row 7, fake half). This supersedes the "`Idle`/`Disabled`" wording in the
`IS_FAKE_DATA_ENABLED` entry above.

**Rejected alternative:** a `Disabled`/`SignedOut` state. It would duplicate
`AuthService.currentUser`, and keeping the two consistent would become `coreSyncFirebase`'s
job. `ui/uiSettings` already reads `currentUser` to show the account, so it can say
"signed out" or "synced" from the two flows without branching on which variant is bound
(`AGENTS.md` #11).

**Non-obvious cost:** a richer status line ("last synced 2 min ago", "3 changes pending")
needs new fields or states later. That's a source change for every exhaustive `when` over
`SyncState`, which is cheap only while there are no consumers (before row 12).

