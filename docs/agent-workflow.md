# Agent workflow

This is a Loop/Graph Engineering artifact: not a feature-dependency diagram, but a
description of how agents working *on this repo's development* hand off to each other —
the shared state they read/write, who's allowed to change what, and the routing rules
between them. If you're looking for the feature list itself, that's
`docs/features.json`; if you're looking for the app's own module architecture, that's
`docs/architecture.md`. This file is about the process that builds those, not about the
app.

## Shared state

Every agent in this loop reads and writes the same three artifacts — nothing is private
to one agent's context:

- **`docs/TASKS.md`** — the WIP=1 row set. Only the orchestrator moves a row between
  `not_started`/`active`/`blocked`/`passing`.
- **`docs/features.json`** — the user-facing Definition of Done. Only the orchestrator
  flips a `state` to `passing`, and only after its `verificationCommand` was actually run
  (hard constraint #13 in `AGENTS.md`) — never because an implementing agent reports
  success.
- **Git commits** — one checkpoint per `docs/TASKS.md` row that reaches `passing`. A new
  session resumes from `docs/PROGRESS.md` + the last commit, not by replaying this
  conversation.

## Nodes

Each node below is a full agent (its own context, its own judgment) — not a function
call:

1. **Orchestrator** — the session driving this repo's work. Makes every architectural
   decision (never delegates one), decides which `docs/TASKS.md` row to activate next,
   and is the only node allowed to write `docs/TASKS.md`/`docs/features.json` state.
2. **Research agents** (code-researcher, web-researcher, diff-researcher) — read-only.
   Answer specific questions the orchestrator poses; never write code or docs
   themselves.
3. **Build agent** — implements one `docs/TASKS.md` row (junior/middle/senior by
   complexity, per the orchestrator's global agent-selection rules). Writes code and
   tests, carries out the row's own verification procedure once, but does not mark the
   row `passing` — that's the orchestrator's call after the validator agrees. When the
   procedure is `manual:` and requires something no agent can do (a physical second
   device, an interactive OAuth consent screen, an Xcode simulator only the user has
   open), the build agent says so explicitly instead of guessing at the result — that
   step routes to the user via the orchestrator, not around them.
4. **Validator agent** (middle/senior-code-validator) — independent from the build agent
   that did the work (generator/evaluator split — never the same agent grading its own
   output). Re-reads the changed files against `docs/architecture.md`/`AGENTS.md`
   constraints, re-runs an automated (`./gradlew ...`) verification command itself rather
   than trusting the build agent's report, and for a `manual:` procedure either repeats
   it independently or — for the same human-only cases above — confirms with the
   orchestrator that the user actually observed the described result, rather than
   accepting the build agent's account of it.

## Edges (routing)

```
Orchestrator → Research agent(s)  [parallel, independent questions]
             ← findings

Orchestrator → Build agent        [one docs/TASKS.md row, WIP=1]
             ← implementation + self-reported verification result

Orchestrator → Validator agent    [same row, independent of the build agent]
             ← PASS or FAIL(minor|critical)

  PASS            → Orchestrator marks the row `passing`, commits (checkpoint),
                     activates the next `not_started` row.
  FAIL(minor)      → Orchestrator sends the same build agent back with the validator's
                      findings; row stays `active`.
  FAIL(critical)   → Orchestrator stops, reports to the user; row stays `active`/
                      `blocked`. No agent unilaterally decides a critical failure is
                      "close enough."
```

**Checkpoints and resume state:** the graph's "checkpoint after every step, run with
thread IDs for replay" primitive maps here to a git commit per `passing` row
plus `docs/PROGRESS.md`'s current-state summary — there's no separate thread-id
mechanism; resuming a session *is* reading the last commit + `docs/PROGRESS.md`.

**Review feedback promotion:** if a validator raises the same kind of finding on two
separate rows (not the same bug twice — the same *category*, e.g. "a ui/* module reached
into a core/*Impl"), that's a signal the rule belongs in an executable check, not repeated
prose review — `checkModuleBoundaries` exists precisely because this happened once
already. The orchestrator, not the validator, decides whether to promote a recurring
finding into a new Gradle-enforced check.

## Design questions

- **Which loops feed which?** Research → Build → Validate → Orchestrator's state update,
  strictly in that order for a given `docs/TASKS.md` row. Research can run ahead of the
  row it's for (e.g. spiking row 3's Koog non-JVM question before row 3 is `active`) but
  Build never starts before its row is `active`, and Validate never starts before Build
  reports done.
- **Who owns targets other loops chase?** Only the orchestrator owns `docs/TASKS.md` and
  `docs/features.json` state. A build or validator agent proposing a change to either is
  a recommendation the orchestrator applies, not a write either agent makes directly.
- **Which loops can veto/rollback others?** The validator can send a row back to the
  build agent (FAIL(minor)) or escalate to a full stop (FAIL(critical)). The orchestrator
  can override a validator's PASS only by re-validating itself and recording why in
  `docs/DECISIONS.md` — never silently.
- **Which metrics may move vs. must stay frozen?** WIP=1 and "an agent never marks its
  own work `passing`" (hard constraint #13) are frozen invariants for this repo — relaxing
  either requires a `docs/DECISIONS.md` entry, not a one-off exception. The 70% coverage
  floor (`docs/quality-gates.md`) and which `docs/features.json` entries exist are allowed
  to move as the app's scope evolves.

## Periodic maintenance

Once feature rows are actively landing (not during this pre-code documentation phase),
revisit this file and `AGENTS.md` roughly monthly: for each harness artifact
(`docs/TASKS.md`, `docs/features.json`, the checklists above), ask whether it's still
earning its keep or has become ceremony nobody reads. Record the outcome (kept as-is / trimmed / dropped) as a `docs/DECISIONS.md` entry
if anything changes, not silently.
