# Tasks

Lightweight tracked template-improvement work — not an app feature backlog. Each row is
`description | verification command | state`. States: `not_started`, `active`,
`passing`. WIP=1: at most one row should be `active` at a time (see `AGENTS.md`). Prune a
row once it's merged to `main` — this list is working memory, not a changelog.

| Description | Verification command | State |
|---|---|---|
| E2E UI-flow test through the real navigation graph (splash → main) using Compose UI testing, not isolated screen tests | `./gradlew :diApp:jvmTest --tests "template.di.KoinAppCommonTest"` | passing |
| Executable architectural constraint: fail the build if a `core/*Impl` module is depended on by anything other than `diApp` | `./gradlew checkModuleBoundaries` | passing |
| PR checklist template mirroring the Layer 1/2/2b/3 termination criteria | manual review of `.github/pull_request_template.md` | passing |
| Skill for scaffolding a new `core/*Api` + `core/*Impl` module pair | manually run the skill against a throwaway module name, confirm it builds | passing |
| Document git worktree usage for parallel agent work in `AGENTS.md` | manual review of `AGENTS.md` | passing |
