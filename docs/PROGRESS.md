# Progress

Working memory for what's in flight, updated at the end of every non-trivial session and
read at the start of the next one. This is short-lived — prune an entry once it's merged
to `main`, don't let this turn into a changelog (git history already is one).

## Current state

- The repo is at the `koog-chat` baseline: package root
  `koog.chat.*` (app id `koog.chat.app` on Android/iOS/desktop; `docs/TASKS.md` row 1
  done, see below), 15 Gradle modules (3 `app/*` + 7 `core/*` + `diApp` + `navigation` + 3
  `ui/*`; `app/iosApp` is a separate Xcode project, not a Gradle module) plus the harness
  docs that came with the template.
- **Documentation/design phase for the "Koog Chat" rewrite is complete**: `AGENTS.md`,
  `docs/architecture.md`, `docs/testing.md`, `docs/quality-gates.md`,
  `docs/DECISIONS.md`, and `docs/TASKS.md` now describe the target architecture (Koog
  multi-provider LLM chat, KMPAuth+GitLive Google sign-in, Firestore LWW sync,
  Nav3 adaptive list-detail). New harness artifacts `docs/features.json`,
  `docs/setup-firebase.md`, and `docs/agent-workflow.md` were added alongside them, and
  a validation pass caught and fixed several ordering/consistency issues in that first
  draft (undefined porting source, a Firebase-setup task ordered after the auth/sync
  tasks that need it, a constraint that contradicted the sync-gating design it was meant
  to describe) — see `docs/DECISIONS.md` for anything that changed a stated decision.
  Rows 1 (rename/initialization), 2 (database layer), 3 (Koog non-JVM spike) and 4
  (`coreLlmApi`/`coreLlmKoog`) are `passing`, and row 6's fake half landed (`coreAuthApi`/`coreAuthFake`), and `coreAuthFirebase` now binds a KMPAuth-backed `GoogleIdTokenProvider` (no `AuthService` yet), so there are now 20 Gradle modules; the
  remaining target-map modules are still `not_started` in `docs/TASKS.md`.
- One open risk is flagged rather than resolved, because it needs an actual build to
  answer, not more research: whether `kotlinx-coroutines 1.11.0` (this repo's pin) conflicts with GitLive
  `firebase-kotlin-sdk 3.0.0-alpha02`'s own `1.10.2` pin — `docs/TASKS.md` row 8,
  resolved right after the dependency lands (rows 5-7) and before any UI work is built
  on top of a possibly-broken `wasmJs` target.

## In progress

- (none)

## Blocked

- (none)

## Recently done

- KMPAuth Google ID-token flow (row 6, KMPAuth half). New `coreAuthApi`
  `GoogleIdTokenProvider`/`GoogleSignInLauncher` (a `@Composable` launcher. See
  `docs/DECISIONS.md` for why it isn't on `AuthService`). `coreAuthFirebase` now has sources:
  `CoreAuthFirebaseModule` (`@Configuration`) and `GoogleIdTokenProviderKmpAuth`, which
  initializes KMPAuth with `GoogleAuthConfig.WEB_CLIENT_ID`. A `generateGoogleAuthConfig`
  task generates that from `local.properties`' `GOOGLE_WEB_CLIENT_ID`. This is the first
  piece of row 5's `local.properties` reader, covering only that key. `coreAuthFake` binds
  `GoogleIdTokenProviderFake`. `iosApp.swift` forwards `onOpenURL` to GoogleSignIn.
  **Fixed a pre-existing gap:** `diApp/src/commonTestFake` was never added to any source set,
  so `AuthServiceCommonTest` hadn't been compiling or running since it landed. It is now added
  to `commonTest` when `IS_FAKE_DATA_ENABLED=true`. `AuthServiceCommonTest` (6) and the new
  `GoogleIdTokenProviderCommonTest` (2) pass on jvm/js/wasmJs/iOS sim. Verified under
  `-DIS_FAKE_DATA_ENABLED=false`: `:app:desktopApp:jar`, `:diApp:compileKotlinJs`/`WasmJs`,
  `:app:androidApp:assembleDebug` + `lint`, `:diApp:linkDebugFrameworkIosSimulatorArm64`,
  `:diApp:jvmTest`, `ciIos` (xcodebuild). `ktlintCheck detekt checkModuleBoundaries` are clean.
  Independent validator: PASS. Not done: actual sign-in on any platform. Nothing consumes
  the launcher until `ui/uiAuth` (row 12), and iOS also needs the Info.plist keys from row 5.

- KMPAuth dependency added on every target (row 6, Firebase half started). New
  `core/coreAuthFirebase` has no sources yet. Its `commonMain` depends on `coreAuthApi` and
  `kmpauth-google` 3.0.6 (`libs.kmpauth.google`). It's registered in settings, kover and
  `coreImplModulePaths`, and live in `diApp`'s `IS_FAKE_DATA_ENABLED=false` branch, so the
  Firebase variant still has no `AuthService` binding. iOS: the `GoogleSignIn-iOS` SwiftPM
  package and Kotlin's `KotlinMultiplatformLinkedPackage` are wired into the Xcode
  project (see `docs/DECISIONS.md`, and `docs/setup-firebase.md` for the regenerate
  command). Verified under `-DIS_FAKE_DATA_ENABLED=false`: `:app:desktopApp:jar`,
  `:diApp:compileKotlinJs`/`WasmJs`, `:app:androidApp:assembleDebug` + `lint`,
  `:diApp:linkDebugFrameworkIosSimulatorArm64`, `:diApp:iosSimulatorArm64Test`,
  `:diApp:jvmTest`. `xcodebuild` (iOS simulator, arm64) passes under both flag values.
  `ktlintCheck detekt checkModuleBoundaries` are clean. Added a shared
  `xcshareddata/xcschemes/iosApp.xcscheme`: `ciIos` runs `xcodebuild -scheme iosApp`, and
  with Swift packages in the project Xcode no longer reliably auto-generates that scheme.
  `./gradlew ciIos` passes. Not done: Info.plist client IDs,
  the `onOpenURL` handler, `KMPAuth.initialize`, and the `AuthService` implementation. All
  of these need row 5's real Firebase project.

- `docs/TASKS.md` row 6, **fake half only**, done at the user's request. Row 5
  (real Firebase project) needs console access, so it was skipped. New
  `core/coreAuthApi` (`AuthService`, `AuthUser`) and `core/coreAuthFake`
  (`AuthServiceFake`: starts signed out; `signInWithGoogleIdToken` creates an in-memory
  `AuthUser`, `signOut` clears it). Both are registered in settings,
  kover, and `coreImplModulePaths`. `diApp` depends on `coreAuthFake` in the
  `IS_FAKE_DATA_ENABLED=true` branch. `CoreAuthFakeModule` is `@Configuration`, so
  `DiKoinApplication` auto-loads it (see `docs/DECISIONS.md`). `coreAuthFake` has no
  tests of its own. `diApp`'s `AuthServiceCommonTest` (6 tests in `src/commonTestFake`,
  compiled only when the flag is true) resolves `AuthService` from the real app graph and
  checks every `currentUser` emission. It passes on jvm/js/wasmJs/iOS sim.
  `KoinAppCommonTest` and `:app:desktopApp:jar` pass under both flag values. `diApp` now has Koin `compileSafety = false` because of a KOIN-D002 false positive (see `docs/DECISIONS.md`). Nothing consumes `AuthService` yet. Row 6 is `blocked` on row 5 for
  `coreAuthFirebase`.

- `docs/TASKS.md` row 4: `coreLlmApi` (`LlmService`, `LlmSessionManager`, `ChatResult`;
  re-exports `coreDatabaseApi`) + `coreLlmKoog` (`LlmClientFactory` for
  Ollama/OpenAI/Anthropic/Google via the individual Koog client artifacts,
  Google; `LlmServiceKoog` with `.flowOn(dispatcherSet.ioDispatcher())`;
  `LlmSessionManagerImpl` ported from koog-chat-1 with the 150 ms throttle), wired into
  `diApp`, kover and `checkModuleBoundaries`. Error replies redact the API key /
  `key=` URL param (validator's critical finding: Google's key rides in the URL, and
  Ktor timeout messages include it). 18 `commonTest` tests pass on jvm/js/wasmJs/iOS
  simulator, including per-provider wire-format streams over an SSE-capable MockEngine and
  a real `qwen3.5:0.8b` stream on every target (skips when Ollama isn't running).
  Removing `flowOn` fails JS and iOS, so the row-3 workaround is guarded offline.
  Independent validator: PASS. Carry-overs recorded on rows 7 and 10 in
  `docs/TASKS.md`. OpenAI/Anthropic/Google only tested against mocked real-format
  responses (no keys). `:app:desktopApp:run` starts the Koin graph cleanly but exits after a
  few seconds when launched from an agent shell. The pre-row-4 baseline does the same,
  so a manual window check is still owed.
- `docs/TASKS.md` row 3 spike (against local Ollama `qwen3.5:0.8b`): Koog
  `KtorKoogHttpClient.Factory()` works from `commonMain` on iOS simulator, JS and WasmJs,
  and `execute()` returned real responses on all three. `executeStreaming()` breaks the
  flow invariant on all three non-JVM targets. `.flowOn(Dispatchers.IO)` (iOS) /
  `.flowOn(Dispatchers.Default)` (web) fixes it, verified per target; row 4 must build
  that in. Spike module removed; details in `docs/DECISIONS.md`.
- `docs/TASKS.md` row 2: `coreDatabaseApi` now has `Chat`/`ChatEntry`/`ChatEntryType`/
  `LlmConfig`/`LlmProvider` (Ollama/OpenAI/Anthropic/Google) + `ChatRepository`/
  `ChatEntryRepository`/`LlmConfigRepository` (Paging3 `PagingSource` via `api`
  `paging-common`); `coreDatabaseRoom` has the matching entities/DAOs/repositories,
  ported from koog-chat-1. The placeholder `Example`/`DatabaseRepository` are gone;
  `uiMain` reads `LlmConfigRepository` until row 10 removes it. Deliberate deviations
  from koog-chat-1: `isDefault` unique per provider (not global), no hardcoded ngrok
  config seeded by `getAllFlow()`. Sync columns `updatedAt`/`isDirty`/`isDeleted`
  (default 0) are on all three entities, not domain models; schema v1 regenerated.
  18 `commonTest` tests pass on jvm, js, wasmJs; desktop app launches. **Open for row
  7:** repository saves reset the sync columns (noted on the row in `docs/TASKS.md`).
- Fixed local iOS test linking (CMP `ui-uikit` klib hardcodes an `Xcode_26.4.app` Swift
  lib path; the convention plugin now adds the local toolchain's path) and renamed
  storage files to `koog_chat_app.db`/`koog_chat_app.pref.json` on all platforms, since
  the unsandboxed simulator test `Documents` dir already held koog-chat-1's
  `koog_chat.db`. `iosSimulatorArm64Test` is now green on every module (row 2's 18 DB
  tests included) and the `iosArm64` framework links. See `docs/testing.md`.
- `docs/TASKS.md` row 1: `template.*` → `koog.chat.*` across all 15 Gradle modules
  (source dirs, packages, namespaces, `@ComponentScan`, Roborazzi screenshot filenames,
  Room schema dir), app id `koog.chat.app` (Android, iOS `project.pbxproj`, desktop
  macOS `bundleID`), display name "Koog Chat", DB `koog_chat.db`, and the scaffold skill.
  JVM desktop data moved to `~/.koog-chat-app`, not `~/.koog-chat`, because koog-chat-1
  already owns that path (see `docs/DECISIONS.md`). Verified: ktlintCheck/detekt/
  checkModuleBoundaries, `KoinAppCommonTest`, full `jvmTest testAndroidHostTest`,
  Android `assembleDebug`, web js+wasmJs compile, desktop run, a throwaway skill scaffold
  (reverted), and an independent validator PASS. iOS was not built this session.
  Known gaps, not fixed: (a) the scaffold skill doesn't add a `@Module @ComponentScan`
  class to the Impl module or register it in `DiKoinApplication`, so a scaffolded
  `@Single` isn't actually in the Koin graph (fold into row 14); (b) under a full
  parallel `jvmTest`, `KoinAppCommonTest.splashNavigatesToMainThroughRealNavigationGraph`
  flaked once ("Splash:" node not found), then passed on 4 reruns.
- Cross-checked `AGENTS.md`/`docs/*` . Alignment was
  already strong; added: clock-in/clock-out framing + a Fresh Session Test note to the
  session checklists (`AGENTS.md`), a `docs/TASKS.md` row-1-is-the-initialization-phase
  framing and "scope surface" terminology, a three-layer-termination-validation framing
  in `docs/quality-gates.md`, and checkpoint/review-feedback-promotion/periodic-review
  notes in `docs/agent-workflow.md`. Two deliberate divergences from the literal
  file layout (one `docs/architecture.md` instead of per-module `ARCHITECTURE.md` files;
  constraints inline in `AGENTS.md` instead of a separate `CONSTRAINTS.md`) are now
  recorded in `docs/DECISIONS.md` instead of being silent gaps.

## Next steps

- **New requirement (2026-10-07): zero-setup first launch.** A user launches the app and
  chats immediately, with no setup, sign-in or registration; those can happen later.
  This is documented in `AGENTS.md` (intro + constraint #14), `docs/architecture.md`
  (*Zero-setup first launch*, *On-device LLM*), `docs/DECISIONS.md` (two entries),
  `docs/TASKS.md` (new rows 16–18; rows 7, 10, 11 and 12 amended) and `docs/features.json`
  (F01 updated, F14–F19 added). The research findings are in the architecture doc's
  on-device table: Android has Gemini Nano via ML Kit GenAI Prompt API (beta, supported
  devices only); iOS 26+ has Apple Foundation Models (Swift-only, so it needs a Swift
  bridge); desktop Chrome has the Prompt API; desktop uses the local Ollama probe. No code
  exists yet. Rows 16–18 don't need Firebase and must land before row 10, so **row 16
  (on-device spike) is the next unblocked row**. It needs a supported Android device, an
  iOS 26 + Apple Intelligence device or simulator, and desktop Chrome with built-in AI.

- **Amended (2026-10-08): local Ollama before on-device, plus the Android adb/emulator
  host.** `DefaultLlmSelector` order is now user config → local Ollama → on-device →
  `None`. On Android the Ollama probe also tries `10.0.2.2:11434` (emulator) after
  `localhost:11434` (`adb reverse`), with a cleartext exception limited to those hosts.
  These are docs-only changes to `docs/architecture.md`, row 18, `docs/DECISIONS.md`, and
  `docs/features.json` (new F20). The code lands with row 18, and row 16 is still next.

- Without Firebase access, the next fake-only step is row 7's fake half:
  `coreSyncApi` + `coreSyncFake`, mirroring `coreAuthFake` including `@Configuration`.
- Start `docs/TASKS.md` row 5: set up a real (test) Firebase project per
  `docs/setup-firebase.md` and implement the desktop/web `local.properties` reader. This
  needs the user: Firebase console access and Google sign-in configuration.

## 2026-09-28: auth/sync selection approach changed

Before any of rows 5-9 landed code, the auth/sync module-selection design changed:
the `*Fake` modules (`coreAuthFake`, `coreSyncFake`) stay, and
`coreSyncFirestore` is renamed `coreSyncFirebase`. Selection between the Fake trio and the
Firebase trio (`coreAuthFirebase`+`coreSyncFirebase`) is now one explicit build-time flag,
`IS_FAKE_DATA_ENABLED` (read by `isFakeDataEnabled()` in
`buildSrc/src/main/kotlin/LocalPropertiesUtils.kt`: `-DIS_FAKE_DATA_ENABLED=` wins, else
the `local.properties` key, default `false`), instead of being inferred from whether a
`google-services.json`/Firebase config is present. `diApp/build.gradle.kts` currently has
this as a commented-out `if/else` placeholder — none of `coreAuthFake`/`coreSyncFake`/
`coreAuthFirebase`/`coreSyncFirebase` exist yet — pending `docs/TASKS.md` rows 6, 7, and 9.
CI already passes `-DIS_FAKE_DATA_ENABLED=true` so automated builds/tests never touch real
Firebase once these modules land.
