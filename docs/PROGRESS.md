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
  1.2.0 multi-provider LLM chat, KMPAuth+GitLive Google sign-in, Firestore LWW sync,
  Nav3 adaptive list-detail). New harness artifacts `docs/features.json`,
  `docs/setup-firebase.md`, and `docs/agent-workflow.md` were added alongside them, and
  a validation pass caught and fixed several ordering/consistency issues in that first
  draft (undefined porting source, a Firebase-setup task ordered after the auth/sync
  tasks that need it, a constraint that contradicted the sync-gating design it was meant
  to describe) — see `docs/DECISIONS.md` for anything that changed a stated decision.
  Rows 1 (rename/initialization) and 2 (database layer) are `passing`; every module in the target map beyond the 15 that already exist is
  still `not_started` in `docs/TASKS.md`.
- Two open risks are flagged rather than resolved, because they need an actual build to
  answer, not more research: (1) whether Koog's non-JVM executor construction
  (`KtorKoogHttpClient.Factory()`) works as documented on iOS/JS/WasmJs — `docs/TASKS.md`
  row 3; (2) whether `kotlinx-coroutines 1.11.0` (this repo's pin) conflicts with GitLive
  `firebase-kotlin-sdk 3.0.0-alpha02`'s own `1.10.2` pin — `docs/TASKS.md` row 8,
  resolved right after the dependency lands (rows 5-7) and before any UI work is built
  on top of a possibly-broken `wasmJs` target.

## In progress

- (none)

## Blocked

- (none)

## Recently done

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
  **Env:** local `iosSimulatorArm64Test` linking fails on every module (ld looks for
  `/Applications/Xcode_26.4.app`, `swiftCompatibility51` not found), unrelated to
  rows 1-2; iOS wasn't verified locally.
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

- Start `docs/TASKS.md` row 3: the Koog 1.2.0 non-JVM executor spike
  (`KtorKoogHttpClient.Factory()` on iOS/JS/WasmJs). It's a manual verification needing
  a reachable LLM endpoint, and iOS needs the local Xcode linker issue fixed first.
