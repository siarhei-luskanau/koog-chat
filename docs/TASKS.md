# Tasks

Ordered build-out plan for the Koog Chat rewrite (see `docs/PROGRESS.md`,
`docs/architecture.md`) — this repo's **scope surface**: the table below is a linear
dependency chain (a degenerate DAG, not a free-standing backlog), so "ordered" and
"depends on the previous row" mean the same thing here unless a row says otherwise. Each
row is `description | verification command | state`. A verification command is either an
actual shell command, or a `manual:` procedure a person or agent can literally carry out
and observe the result of — never just "it compiles" or "the code looks right." States:
`not_started`, `active`, `blocked`, `passing`. WIP=1: at most one row should be `active`
at a time (see `AGENTS.md`). Rows are ordered — don't start row *N* while an earlier row
it depends on isn't `passing`. Prune a row once it's merged to `main` and reflected in
`docs/PROGRESS.md` — this list is working memory, not a changelog.

**Row 1 is the initialization phase**, not a feature row: it must leave the repo with a
verified Startup Readiness Checklist — `ktlintCheck detekt` clean, the named
`KoinAppCommonTest` passing, and the scaffold skill re-verified — *before* row 2 starts
doing feature work on top of it. Don't blend the two; row 1 existing first in this table
is deliberate, not incidental ordering.

**Porting source:** [github.com/siarhei-luskanau/koog-chat-1](https://github.com/siarhei-luskanau/koog-chat-1)
— an existing Ollama-only KMP chat app. Rows 2, 4, and 10 port specific pieces of it;
`docs/architecture.md` cites exactly what.

| # | Description | Verification command | State |
|---|---|---|---|
| 1 | Rename `template.*` → `koog.chat.*` package root, rootProject → `koog-chat`, app id → `koog.chat.app` across all 15 Gradle modules (`app/iosApp` is a separate Xcode-project rename, not a Gradle module). Also update `.claude/skills/scaffold-core-module-pair/SKILL.md`'s hardcoded `template.*` namespaces, paths, and `--tests "template.di...."` filter to `koog.chat.*` in the same pass — otherwise the skill silently breaks the moment this row lands. | `./gradlew ktlintCheck detekt`; `./gradlew :diApp:jvmTest --tests "koog.chat.di.KoinAppCommonTest"`; manually scaffold one throwaway module with the updated skill to confirm it still produces `koog.chat.*` output | passing |
| 2 | Port `Chat`/`ChatEntry`/`LlmConfig` domain models, Room entities/DAOs, and Paging3 wiring from koog-chat-1 into `coreDatabaseApi`/`coreDatabaseRoom`, replacing the placeholder `Example` entity. Add `updatedAt: Long`, `isDirty: Boolean`, `isDeleted: Boolean` to every entity that will eventually sync (`ChatEntity`, `ChatEntryEntity`, `LlmConfigEntity`) **now**, even though nothing writes to them until row 7 — so row 7 is a sync feature, not also a schema migration. | `commonTest` passing for the ported repositories, including one asserting the three new columns exist with sane defaults | passing |
| 3 | **Spike, do not skip:** verify Koog 1.2.0's non-JVM executor construction (`KtorKoogHttpClient.Factory()`) actually works on iOS, JS, and WasmJs from `commonMain` | manual: run a minimal Koog call on each of the 3 non-JVM targets, confirm a real response | not_started |
| 4 | Build `coreLlmApi` (provider-agnostic interface) + `coreLlmKoog` (Ollama/OpenAI/Anthropic/Google factory), porting `LlmSessionManager`'s streaming/throttle logic from koog-chat-1. Include `checkModuleBoundaries`/`kover` registration for `coreLlmKoog` as part of this row, per `AGENTS.md` constraints #2/#5 — don't defer it. | `commonTest` with a fake `LlmService` per provider; `./gradlew checkModuleBoundaries` | not_started |
| 5 | Set up one real (test) Firebase project end to end per `docs/setup-firebase.md`: Google sign-in enabled, Android + at least one non-Android platform registered, Firestore rules applied, `local.properties` keys in place, and the desktop/web `BuildConfig`-style `local.properties` reader implemented (this reader has no other owning row). Do this **before** rows 6–7 — their manual verification needs a real project to run against. | manual: Firebase console shows the app registered on Android + one non-Android target; a local desktop build reads the `local.properties` keys without crashing | not_started |
| 6 | Build `coreAuthApi` + `coreAuthFirebase` (kmpauth-google + GitLive `signInWithCredential`) + `coreAuthFake` against the row-5 project; verify session persistence on JVM desktop specifically (flagged risk in `docs/DECISIONS.md`). Include `checkModuleBoundaries`/`kover` registration for both backend modules. | manual: sign-in completes per platform against the row-5 project; `commonTest` against `coreAuthFake`; `./gradlew checkModuleBoundaries` | not_started |
| 7 | Build `coreSyncApi` + `coreSyncFirestore` (LWW push/pull over the row-2 sync columns, `apiKey` excluded from every DTO) + `coreSyncFake`, against the row-5 project. **Row-2 carry-over:** repository `save`/`update` build entities from domain models and `@Upsert` the whole row, so a plain domain save currently resets `updatedAt`/`isDirty`/`isDeleted` to defaults — repository writes must set `isDirty = true`/`updatedAt` (and not clobber sync-written values) before sync is built on top. `coreSyncFirestore.start()` itself checks `coreAuthApi.currentUser` at runtime and no-ops until signed in — this lives inside the implementation, it is not a branch its consumers (`ui/*`) ever write (`AGENTS.md` constraint #11). Include `checkModuleBoundaries`/`kover` registration. | `commonTest` covering every LWW ordering case; manual: two-device sync round-trip against the row-5 project; `./gradlew checkModuleBoundaries` | not_started |
| 8 | Resolve the `kotlinx-coroutines 1.11.0` vs. `1.10.2` pin question (`docs/DECISIONS.md`) now that `firebase-kotlin-sdk` is actually on the classpath (rows 5–7) — do this before building UI on top of a possibly-broken `wasmJs` target, not after. | `./gradlew :core:coreAuthFirebase:compileKotlinWasmJs :core:coreSyncFirestore:compileKotlinWasmJs` | not_started |
| 9 | Wire `diApp`'s conditional binding: `coreAuthFirebase`/`coreSyncFirestore` only when Firebase is configured at build time (the row-5 mechanism), `coreAuthFake`/`coreSyncFake` otherwise. This is the only place either decision is made. | extend `KoinAppCommonTest` to cover both the configured and unconfigured binding | not_started |
| 10 | Remove `ui/uiMain` (superseded by `uiChatList` as the post-splash home screen). Port `ui/uiChat` + `ui/uiChatList` from koog-chat-1, adapting `ChatViewModel` to the provider-agnostic `LlmService`. Apply `roborazziConvention` to both new modules. Rewrite `KoinAppCommonTest` to assert `ChatList:` where it used to assert `Main:`. | `jvmTest` (Roborazzi) for both screens in isolation — a full manual run through the real app is deferred to row 11, since neither screen is reachable from splash until navigation is rewired | not_started |
| 11 | Wire adaptive list-detail navigation (`ListDetailSceneStrategy`) — `ChatList` as list pane, `Chat` as detail pane; splash now routes here instead of the old `Main` destination. | manual: resize test across the adaptive breakpoint on desktop/web; `KoinAppCommonTest` asserting the full splash → chat-list round trip | not_started |
| 12 | Build `ui/uiLlmConfig`, `ui/uiAuth`, `ui/uiSettings` (apply `roborazziConvention` to each). | `jvmTest` (Roborazzi) + manual run of each screen | not_started |
| 13 | Wire a `checkFeatureList` Gradle task validating `docs/features.json` against the informal convention documented in that file's own header (well-formed JSON, every field present and non-empty, and — only for entries whose `verificationCommand` begins with `./gradlew` — that the referenced task exists). | `./gradlew checkFeatureList` | not_started |
| 14 | Extend `.claude/skills/scaffold-core-module-pair/SKILL.md` to also scaffold a `*Fake` module alongside `*Api`/`*Impl`. | manually scaffold a throwaway capability with all three, confirm it builds | not_started |
| 15 | Update `README.MD` (run instructions, project name) now that the row 1 rename is real. | manual review | not_started |
