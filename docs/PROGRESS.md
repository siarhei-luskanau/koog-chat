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
  (`coreLlmApi`/`coreLlmKoog`) are `passing`, so there are now 17 Gradle modules; the
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

- Start `docs/TASKS.md` row 5: set up a real (test) Firebase project per
  `docs/setup-firebase.md` and implement the desktop/web `local.properties` reader. This
  needs the user: Firebase console access and Google sign-in configuration.
- Doc drift, not yet fixed: `AGENTS.md`/`docs/quality-gates.md` say the Kover floor is
  70%, but the root `build.gradle.kts` enforces 88%.

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
