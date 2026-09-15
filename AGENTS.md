# AGENTS.md

**Koog Chat** — a Kotlin Multiplatform LLM chat client (Android / Desktop / iOS / JS /
WasmJs from one shared codebase, Koin DI) with local-first chat history, optional Google
sign-in, and optional cross-device sync. The app is fully usable offline and
unauthenticated; signing in only turns sync on, it never gates chat.

This repo started as `koog-chat` and is being evolved into Koog
Chat. **Current status: documentation/design phase only — no Koog Chat code has been
written yet.** The module map, constraints, and conventions below describe the *target*
architecture this repo is being built toward. Read `docs/PROGRESS.md` for what's
actually implemented right now, and `docs/TASKS.md` for the ordered build plan. This
file is a routing file: quick orientation + hard constraints + pointers to `docs/`. Read
the linked doc before doing the thing it covers; don't try to hold all of it in your head
at once.

## Target module map

```
app/androidApp          Android entry point (Activity), depends only on diApp
app/desktopApp          Desktop (JVM) entry point, depends only on diApp
app/webApp              Browser entry point (js + wasmJs), depends only on diApp
app/iosApp              Xcode project wrapping the framework diApp produces
diApp                   Koin DI graph — the ONLY module allowed to depend on *Impl/*Fake modules
navigation              Navigation3 + adaptive list-detail (chat list / chat detail panes)
ui/uiCommon             Shared composables, theme, Compose resources
ui/uiChatList           Conversation list screen (the app's home screen)
ui/uiChat               Chat detail screen — messages, streaming, model picker
ui/uiLlmConfig          Add/edit/remove LLM provider configs (incl. API keys)
ui/uiAuth               Google sign-in screen/button (optional entry point, never a gate)
ui/uiSettings           Account, sign-out, sync status
ui/uiSplash             Splash screen
core/coreCommon         Dispatchers/platform-service abstractions
core/coreDatabaseApi    Chat/ChatEntry/LlmConfig models + repository interfaces
core/coreDatabaseRoom   Room 3 KMP implementation of coreDatabaseApi (source of truth)
core/coreNetworkApi     Generic network client interface only
core/coreNetworkKtor    Ktor implementation of coreNetworkApi
core/corePrefApi        Preferences interface only
core/corePrefDatastore  AndroidX DataStore implementation of corePrefApi
core/coreLlmApi         Provider-agnostic LLM chat interface (streaming, tool-calling)
core/coreLlmKoog        Koog 1.2.0-backed implementation (Ollama/OpenAI/Anthropic/Google)
core/coreAuthApi        Auth state/session interface (signed-in user or none)
core/coreAuthFirebase   KMPAuth (Google) + GitLive Firebase Auth implementation
core/coreAuthFake       No-op implementation: always signed-out, used when Firebase unconfigured
core/coreSyncApi        Sync interface: start/stop, sync-state observation
core/coreSyncFirestore  Firestore LWW sync implementation; internally no-ops until signed in
core/coreSyncFake       No-op implementation: bound (compile-time) when Firebase is unconfigured
```

Full dependency graph, the `*Api`/`*Impl`/`*Fake` rule, and the auth/sync wiring: see
`docs/architecture.md` — **read before adding or wiring a new module.**

## First commands to run

```
./gradlew ktlintFormat                                   # auto-fix style before anything else
./gradlew ktlintCheck detekt lint checkModuleBoundaries  # static analysis gate
./gradlew jvmTest testAndroidHostTest                    # fastest test targets for local iteration
./gradlew koverVerifyCoverage                            # coverage gate (70% floor)
```

Full command list per gate/target: `docs/quality-gates.md`.

## Hard constraints

1. `core/*Api` modules define interfaces/models only — zero implementation dependencies
   (no Room, Ktor, DataStore, Firebase, KMPAuth, Koog clients).
2. Only `diApp` may depend on a `core/*Impl` or `core/*Fake` module. `ui/*` and
   `navigation` depend on `*Api` modules directly, never on `*Impl`/`*Fake`.
   Gradle-enforced: `./gradlew checkModuleBoundaries` fails the build on a violation.
3. Apps (`androidApp`, `desktopApp`, `webApp`) depend only on `diApp` — never reach into
   `core/*` or `ui/*` directly.
4. Every new `core/*` or `ui/*` module applies `id("composeMultiplatformConvention")`
   and is registered in `settings.gradle.kts`.
5. Every new module — `*Api` included, not just `*Impl`/`*Fake` — must be added to the
   root `build.gradle.kts` `kover { dependencies { kover(projects...) } }` block so
   coverage stays aggregated. A new `*Impl`/`*Fake` module must *also* be added to
   `checkModuleBoundaries`'s `coreImplModulePaths` set (constraint #2) — do both in the
   same change, don't defer either to a follow-up task.
6. Don't hand-write Koin `single<Api> { Impl() }` bindings — annotate the implementation
   class `@Single`; `diApp`'s `@ComponentScan` picks it up via KSP.
7. Run `ktlintFormat` before `ktlintCheck` — CI does, and an unformatted diff fails the
   `Lint` job even if the code is otherwise correct.
8. Before adding a test, check `docs/testing.md` for which of the six test mechanisms
   (`commonTest`, `jvmTest`/Roborazzi, `androidHostTest`, `androidTest`,
   `jsBrowserTest`/`wasmJsBrowserTest`, `iosSimulatorArm64Test`) actually fits — most new
   tests belong in `commonTest`.
9. "Feature complete" means the app actually launched on an affected target
   (`./gradlew :app:desktopApp:run` is the cheapest check), not just "it compiles" —
   see Layer 3 in `docs/quality-gates.md`.
10. One module/feature actively worked at a time (WIP=1). A large ask ("add a new
    platform target", "add offline sync") gets broken into an ordered list in
    `docs/TASKS.md` before any code changes start.
11. `coreAuthApi`/`coreSyncApi` **consumers** (`ui/*`, `navigation`) never branch on "is
    Firebase configured?" or "is the user signed in?" — they just read
    `coreAuthApi.currentUser`/`coreSyncApi`'s state flow like any other data. Whether
    Firebase is configured at all is decided once, at compile time, by which concrete
    module `diApp` binds (`*Firebase`/`*Firestore` vs `*Fake`) — that's what makes "app
    works offline until Firebase is configured" a wiring fact, not a runtime `if` some
    screen has to remember. Whether the user is currently signed in is a legitimate
    runtime check, but it lives **inside** `coreSyncFirestore` itself (its `start()`
    no-ops until `coreAuthApi.currentUser` is non-null) — not in anything that merely
    consumes `coreSyncApi`.
12. API keys (`LlmConfig.apiKey`) never leave the device. Every sync DTO/mapper in
    `coreSyncFirestore` must exclude that field explicitly — this is a security
    constraint, not a default that can be relaxed by a future `SyncLevel` setting without
    a new `docs/DECISIONS.md` entry explaining the tradeoff.
13. Every user-facing feature has a row in `docs/features.json` with a verification
    procedure — an actual Gradle command where one exists, or an explicit `manual:` step
    a person/agent can literally carry out (most auth/sync/cross-platform features have
    no automatable check). Vague criteria like "the code looks right" don't count. A
    feature's state moves to `passing` only after that procedure has actually been
    carried out and succeeded — never self-declared by the implementing agent.

## Skills

- `.claude/skills/scaffold-core-module-pair/SKILL.md` — scaffolds a new `core/*Api` +
  `core/*Impl` module pair. Currently doesn't cover `*Fake` modules; extending it is
  tracked in `docs/TASKS.md`. Until then, model a new `*Fake` module by hand on
  `core/coreAuthFake`/`core/coreSyncFake` once those exist, or on any existing `*Impl`
  module's shape with a no-op body.

## Session start checklist

Before making any change:

1. Read `docs/PROGRESS.md` — what's actually implemented, in progress, or blocked.
2. Read `docs/TASKS.md` — pick up the single `active` row, or the next `not_started` row
   if none is active (WIP=1 — don't start a second row).
3. Confirm the baseline still builds: `./gradlew :app:desktopApp:run` (or `jvmTest` if
   desktop isn't the target you're touching).
4. If the row you're picking up depends on an earlier row that isn't `passing` yet, stop
   and say so — don't work out of order.

## Session exit checklist

Before ending a session with non-trivial changes:

1. `./gradlew ktlintFormat ktlintCheck detekt lint checkModuleBoundaries` — fix anything
   it flags.
2. Run the tests actually touched by the change (see `docs/testing.md` for which
   mechanism applies) — not the full suite unless the change is broad.
3. Update `docs/PROGRESS.md` — move finished items out, note anything left in-progress
   or blocked so the next session doesn't have to reconstruct it.
4. Update the row's state in `docs/TASKS.md` (and the matching entry in
   `docs/features.json` if it corresponds to a user-facing feature) — only after its
   verification command actually ran and passed.
5. If the session made a non-obvious architectural choice (rejected an alternative for a
   reason that isn't visible in the code), add an entry to `docs/DECISIONS.md`.
6. Remove debug/leftover code (stray `println`, commented-out blocks, temp test data)
   before the final commit.

## Parallel agent work (git worktrees)

`.claude/worktrees/` is where independent agents check out separate worktrees for
concurrent tasks on this repo — it's excluded locally (`.git/info/exclude`, not tracked)
so worktrees never collide with the main checkout or with each other:

```
git worktree add .claude/worktrees/<task-name> -b <branch-name>
git worktree remove .claude/worktrees/<task-name>   # after the branch merges
```

Reach for this when the orchestrator's parallel-execution rule applies — different
modules, no shared state, e.g. one agent scaffolding `coreLlmKoog` while another builds
`ui/uiLlmConfig`. It's a mechanism for running independent work concurrently, not a way
around WIP=1 (#10) within a single line of work — one module/feature per worktree, same
as one module/feature per session. See `docs/agent-workflow.md` for how research, build,
and validation agents hand off to each other across a `docs/TASKS.md` row.

## Docs

- `docs/architecture.md` — target module dependency graph, the `*Api`/`*Impl`/`*Fake`
  rule, auth/sync wiring, LLM provider abstraction, adaptive navigation. Read before
  adding or wiring a new module.
- `docs/testing.md` — what each of the six test mechanisms is for and when to reach for
  it. Read before adding any test.
- `docs/quality-gates.md` — the exact commands CI runs per layer and where to look when
  one fails. Read before declaring a change done.
- `docs/PROGRESS.md` — current state / in-progress / blocked / next-steps. Read at the
  start of a session, update at the end of one (see checklists above).
- `docs/DECISIONS.md` — append-only log of non-obvious architectural choices and the
  alternatives that were rejected. Read before revisiting a decision that looks
  arbitrary; add an entry when you make one worth not re-litigating.
- `docs/TASKS.md` — tracked build-out work as
  `description | verification command | state`. Enforces WIP=1: at most one row
  `active` at a time.
- `docs/features.json` — machine-checkable user-facing feature list (behavior,
  verification command, state). See `docs/agent-workflow.md` for the pass-state gating
  rule around it.
- `docs/setup-firebase.md` — how to configure the Firebase project, per-platform config
  files, and local.properties keys needed for auth + sync.
- `docs/agent-workflow.md` — how research/build/validator agents hand off work on this
  repo, and what state each step is allowed to change.
