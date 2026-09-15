# Progress

Working memory for what's in flight, updated at the end of every non-trivial session and
read at the start of the next one. This is short-lived — prune an entry once it's merged
to `main`, don't let this turn into a changelog (git history already is one).

## Current state

- The repo is at the `koog-chat` baseline: package root
  `template.*`, 15 Gradle modules (3 `app/*` + 7 `core/*` + `diApp` + `navigation` + 3
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
  **No Koog Chat code has been written yet** — every module in the target map beyond the
  15 that already exist is still `not_started` in `docs/TASKS.md`.
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

## Next steps

- Start `docs/TASKS.md` row 1 (rename `template.*` → `koog.chat.*`, rootProject →
  `koog-chat`, app id `koog.chat.app`, and update the scaffold skill in the same pass) —
  the only row that touches every existing module, so it goes first to avoid every later
  row needing a rebase on top of it.
