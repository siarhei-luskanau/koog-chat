# Progress

Working memory for what's in flight, updated at the end of every non-trivial session and
read at the start of the next one. This is short-lived — prune an entry once it's merged
to `main`, don't let this turn into a changelog (git history already is one).

## Current state

- Phase 1 (Instructions layer) done: `AGENTS.md`, `docs/architecture.md`,
  `docs/testing.md`, `docs/quality-gates.md` exist and are current.
- Phase 2 (Continuity across sessions) done: `docs/PROGRESS.md`, `docs/DECISIONS.md`,
  and `AGENTS.md` Session Exit Checklist all in place.
- Phase 3 (WIP + termination criteria) done: WIP=1 rule and the "feature complete means
  Layer 3, not just compiles" rule were already in `AGENTS.md` constraints #10–11 and
  `docs/quality-gates.md`'s Layer 1/2/2b/3 structure; added `docs/TASKS.md` (the
  remaining piece) and linked it from `AGENTS.md`.
- Phase 4 (E2E test + architectural constraint) done: `diApp`'s `KoinAppCommonTest`
  (`commonTest`) now pauses `mainClock` to observe `Splash:` before resuming and
  asserting `Main:`, exercising the real Koin + navigation graph end-to-end — verified
  via `./gradlew :diApp:jvmTest --tests "template.di.KoinAppCommonTest"`. Root
  `build.gradle.kts` gained a `checkModuleBoundaries` task enforcing the `*Impl` half of
  the architecture rule (only `diApp` may depend on `coreDatabaseRoom`/`coreNetworkKtor`/
  `corePrefDatastore`), wired into the CI `Lint` job and `docs/quality-gates.md`. See
  `docs/DECISIONS.md` for why the check doesn't also restrict `ui/*` dependents, and why
  it's implemented via `gradle.projectsEvaluated` instead of a plain `doLast`.

- Phase 5 (Observability) done: `.github/pull_request_template.md` mirrors the Layer
  1/2/2b/3 termination criteria and points to CI artifact names for evidence.
  `docs/quality-gates.md`'s Roborazzi row claimed diffs were "uploaded on CI" but the
  `VerifyScreenshot` job had no upload step — added one (`roborazzi-diff-<module>-<os>`,
  failure-only) to `ci.yml` so the doc claim is actually true, matching the pattern
  already used by the `Tests`/`Coverage` jobs.

- Phase 6 (Loop engineering) done: added
  `.claude/skills/scaffold-core-module-pair/SKILL.md` — scaffolds a new `core/*Api` +
  `core/*Impl` pair (module creation, `settings.gradle.kts`, `diApp` wiring, `kover`
  aggregation, `checkModuleBoundaries` allowlist) — and validated it by scaffolding a
  throwaway `coreThrowawayApi`/`coreThrowawayFake` pair through all four steps
  (`ktlintCheck`/`detekt`, module `assemble`, `checkModuleBoundaries`,
  `KoinAppCommonTest`) before reverting it. Also added a "Parallel agent work (git
  worktrees)" section to `AGENTS.md` documenting `.claude/worktrees/` (locally excluded
  via `.git/info/exclude`) as the mechanism for running independent agents without file
  collisions. Graph engineering stays explicitly deferred per the plan — no
  competing autonomous loops exist on this repo yet.

## In progress

- (none)

## Blocked

- (none)

## Next steps

- (none) — all planned phases complete. Revisit graph engineering only if multiple
  long-running autonomous loops start operating on this repo concurrently.
