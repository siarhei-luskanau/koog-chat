# Architecture

This describes the **target** architecture for Koog Chat. The repo is currently at the
`koog-chat` baseline — see `docs/PROGRESS.md` for what's actually
built and `docs/TASKS.md` for the order it's being built in.

## Target module map

```
app/androidApp   Android application entry point (Activity), depends only on diApp
app/desktopApp   Desktop (JVM) application entry point, depends only on diApp
app/webApp       Browser entry point, js + wasmJs targets, depends only on diApp
app/iosApp       Xcode project wrapping the framework diApp produces (no build.gradle.kts)

diApp            Koin DI graph. The ONLY module allowed to depend on *Impl/*Fake modules.

navigation       Navigation3 + adaptive list-detail scene strategy, depends on ui/*

ui/uiCommon      Shared composables, theme, Compose resources
ui/uiChatList    Conversation list — list pane of the adaptive scene, home screen
ui/uiChat        Chat detail — messages, streaming display, model picker; detail pane
ui/uiLlmConfig   Add/edit/remove LlmConfig rows (provider, modelId, apiKey, providerUrl)
ui/uiAuth        Google sign-in screen/button
ui/uiSettings    Account, sign-out, sync status indicator
ui/uiSplash      Splash screen, depends on coreCommon + uiCommon

core/coreCommon        Dispatchers/platform-service abstractions with per-target impls
core/coreDatabaseApi    Chat, ChatEntry, LlmConfig models + repository interfaces
core/coreDatabaseRoom   Room 3 KMP implementation of coreDatabaseApi — source of truth
core/coreNetworkApi     Generic network client interface only
core/coreNetworkKtor    Ktor implementation of coreNetworkApi
core/corePrefApi        Preferences/storage interface only
core/corePrefDatastore  AndroidX DataStore implementation of corePrefApi
core/coreLlmApi         Provider-agnostic chat/streaming interface
core/coreLlmKoog        Koog implementation (Ollama/OpenAI/Anthropic/Google)
core/coreAuthApi        Auth session interface: current user, sign-in, sign-out
core/coreAuthFirebase   KMPAuth (kmpauth-google) + GitLive Firebase Auth implementation
core/coreAuthFake       In-memory fake: sign-in creates a user, sign-out clears it; bound (compile-time) when IS_FAKE_DATA_ENABLED
core/coreSyncApi        Sync interface: start/stop, sync-state observation
core/coreSyncFirebase   Firestore LWW sync implementation; internally no-ops until signed in
core/coreSyncFake       No-op, sync state permanently idle, bound (compile-time) when IS_FAKE_DATA_ENABLED
```

Note: `ui/uiMain` (the template's placeholder home screen) is removed as part of
`docs/TASKS.md` row 9 — `uiChatList` takes over as the splash destination.

`coreNetworkApi`/`coreNetworkKtor` and `corePrefApi`/`corePrefDatastore` carry over from
the template with a narrower, specific purpose in Koog Chat rather than being generic
placeholders: `coreNetworkKtor` is for any direct REST call outside of Koog's own HTTP
handling (e.g. checking whether a configured Ollama base URL is reachable before
attempting a chat call); `corePrefDatastore` holds small local-only UI state that
deliberately never syncs (e.g. "has the user dismissed the sign-in prompt", the
last-opened chat id) — distinct from `LlmConfig`/`Chat` rows, which live in
`coreDatabaseApi`/Room because they're either synced or schema-shaped data, not
key-value UI preferences.

## The `*Api` / `*Impl` / `*Fake` rule

Every core capability is split into an `*Api` module plus one or more backend modules:

- `core/*Api` — interfaces and models only. No implementation dependencies (no Room,
  Ktor, DataStore, Firebase, KMPAuth, or Koog provider clients). Anything can depend on
  these.
- `core/*Impl` (`coreDatabaseRoom`, `coreNetworkKtor`, `corePrefDatastore`,
  `coreLlmKoog`, `coreAuthFirebase`, `coreSyncFirebase`) — a concrete, real-backend
  implementation. **Only `diApp` may depend on one.**
- `core/*Fake` (`coreAuthFake`, `coreSyncFake`) — a concrete, no-op implementation with
  the same restriction. Introduced specifically for `coreAuthApi`/`coreSyncApi` because
  those two capabilities have a real "there is legitimately no backend right now" state
  (no Firebase project wired up, or user not signed in) that isn't a bug to work around
  but the app's normal, fully-supported offline mode. A single impl with an internal
  `if (enabled) ... else noop` branch was rejected: it would pull Firebase/KMPAuth
  dependencies into every build regardless of whether a developer wants them, and it
  would make the offline path untestable without pretending to talk to Firebase in
  tests. A build-time-swappable `*Fake` module keeps Firebase out of the dependency
  graph entirely when the fake variant is selected, and out of every automated test
  always (see `docs/testing.md`).

`ui/*` and `navigation` depend on `*Api` modules directly (e.g. `uiChat` depends on
`coreLlmApi`, `coreDatabaseApi`) but never on an `*Impl`/`*Fake` module. Apps
(`androidApp`, `desktopApp`, `webApp`) don't depend on `core/*` or `ui/*` at all — they
depend only on `diApp`, which is where every implementation is wired together.

```
app/*  →  diApp  →  core/*Impl, core/*Fake  →  core/*Api  ←  ui/*, navigation
```

The `*Impl`/`*Fake` half of this is enforced by Gradle: the root `build.gradle.kts`
`checkModuleBoundaries` task fails the build if any module other than `diApp` declares a
dependency on a listed backend module — see `docs/quality-gates.md`. Adding a new
backend module means adding its path to that task's `coreImplModulePaths` set too.

## How wiring works (Koin)

`diApp`'s `DiCommonModule` is a single `@Module @ComponentScan(["koog.chat.di"])` class.
Implementation classes across every module annotate themselves with `@Single`; Koin's KSP
compiler plugin generates the bindings. There are no hand-written `single<Api> { Impl() }`
blocks.

**Auth/sync binding is the one place this repo makes a deliberate choice between two
`@Single`-annotated implementations of the same `*Api` at wiring time**, rather than
letting `@ComponentScan` pick up whichever one happens to exist:

- `diApp` includes `coreAuthFirebase` and `coreSyncFirebase`, or `coreAuthFake` and
  `coreSyncFake`, based on a single explicit build flag, `IS_FAKE_DATA_ENABLED` —
  read by `isFakeDataEnabled()` in
  `buildSrc/src/main/kotlin/LocalPropertiesUtils.kt`: the `-DIS_FAKE_DATA_ENABLED=` JVM
  system property wins if set, otherwise the `IS_FAKE_DATA_ENABLED=` key in
  `local.properties`; missing, or anything other than `true`, means `false`. `diApp`'s
  `commonMain.dependencies` picks the pair with a plain `if`/`else`:

  ```kotlin
  if (isFakeDataEnabled { gradleLocalProperties(rootDir, providers) }) {
      implementation(projects.core.coreAuthFake)
      implementation(projects.core.coreSyncFake)
  } else {
      implementation(projects.core.coreAuthFirebase)
      implementation(projects.core.coreSyncFirebase)
  }
  ```

  One flag selects both trios together — never a mix of fake and Firebase. **Only the
  `coreAuthFake` line is live so far** (row 6, fake half); the other three are
  commented placeholders until rows 6/7 build the remaining modules and row 9 finishes
  the `if`/`else`. Each auth/sync backend module's Koin `@Module` is also annotated
  `@Configuration`, so `DiKoinApplication` auto-includes whichever variant is on the
  classpath without naming either class (see `docs/DECISIONS.md`). CI (`.github/workflows/ci.yml`,
  `screenshots.yml`) already passes `-DIS_FAKE_DATA_ENABLED=true`, so CI and screenshot
  builds always run against the fake modules and never touch real Firebase. A developer
  without a Firebase project sets `IS_FAKE_DATA_ENABLED=true` in `local.properties`;
  leaving it unset builds the Firebase variant, which needs the config described in
  `docs/setup-firebase.md`.
- Even when `coreAuthFirebase`/`coreSyncFirebase` are selected, `coreSyncFirebase`'s own
  `start()` no-ops until `coreAuthApi.currentUser` emits a signed-in user. Signing out
  stops sync and leaves all local data in Room untouched — no data loss, just no further
  remote writes/reads.

## LLM provider abstraction (Koog)

`coreLlmApi` defines a provider-agnostic `LlmService` (one streamed chat turn, with
thinking/text chunk callbacks, returning a `ChatResult`) and `LlmSessionManager` (runs a
turn in the background, persists it, exposes which chats are generating). It re-exports
`coreDatabaseApi` (`api(...)`), which owns `LlmProvider` (`Ollama`, `OpenAI`, `Anthropic`,
`Google`) and `LlmConfig` since row 2 — ported from
[koog-chat-1](https://github.com/siarhei-luskanau/koog-chat-1)'s schema, which already
carries these fields even though it only used `Ollama`:

```
LlmConfig(id, provider: LlmProvider, modelId: String, apiKey: String?,
          providerUrl: String?, isDefault: Boolean)
```

`isDefault` is unique **per provider** (at most one `LlmConfig` per `LlmProvider` may
have `isDefault = true`) — this is a repository-level invariant `coreDatabaseRoom` must
enforce (e.g. by clearing the flag on any sibling row with the same `provider` when
setting a new default), not a UI-only convention.

`ChatEntry` (also ported from koog-chat-1) additionally carries `tokensUsed: Long?`,
`tokensPerSecond: Double?`, and `responseTimeMs: Long?`, populated when the stream's
`End` frame arrives — this is what backs the token/response-time stats `ui/uiChat`
displays on a finished message.

`coreLlmKoog` implements both. It depends on the individual Koog client artifacts
(`prompt-executor-{ollama,openai,anthropic}-client`,
`prompt-executor-google-client`, `http-client-ktor`), not the
`koog-agents` umbrella, because it only needs raw `LLMClient` streaming. `LlmClientFactory`
maps `LlmConfig` to an `LLModel` plus an `OllamaClient`/`OpenAILLMClient`/
`AnthropicLLMClient`/`GoogleLLMClient`, all built on one shared
`KtorKoogHttpClient.Factory(HttpClient())` singleton. `providerUrl` overrides the base
URL, and `apiKey` is required for every provider except Ollama. Things every implementer
of this module must account for:

- **Non-JVM targets (iOS, JS, WasmJs) have no HTTP auto-discovery.** Koog's convenience
  `simple*Executor` one-liners rely on `ServiceLoader`, which only works on JVM/Android,
  so clients are always constructed with the explicit `KtorKoogHttpClient.Factory`
  (verified on every target by the row-3 spike).
- **Streaming needs `.flowOn(dispatcherSet.ioDispatcher())`**: Koog emits frames off the
  collector's context on iOS/JS/WasmJs (`docs/DECISIONS.md`). `OllamaStreamingCommonTest`
  streams from a real local Ollama on every target when one is running, and skips
  otherwise.
- **Anthropic resolves model ids through `AnthropicClientSettings.modelVersionsMap`** and
  rejects unknown models, so the factory passes `mapOf(model to modelId)`. OpenAI models
  carry `LLMCapability.OpenAIEndpoint.Completions` (Chat Completions, not Responses).
- **Google's client is beta** (as are DeepSeek/Mistral/Alibaba, not used here); pin its
  version carefully and don't assume API stability across Koog patch releases.

Streaming reuses koog-chat-1's proven shape: `executeStreaming(prompt, model)` returns a
`Flow` of `StreamFrame` (`TextDelta`, `ReasoningDelta`, `End`); `LlmSessionManagerImpl`
persists the growing response to Room every ~150ms rather than on every delta, and
finalizes the `ChatEntry` to `SUCCESS_RESPONSE`/`ERROR_RESPONSE` on `End`/failure.

## Auth & sync design

**Sign-in flow:** `kmpauth-google` (KMPAuth 3.0.6) obtains a Google ID token →
`dev.gitlive.firebase.auth.GoogleAuthProvider.credential(idToken, null)` →
`Firebase.auth.signInWithCredential(credential)` (GitLive `firebase-kotlin-sdk`
3.0.0-alpha02) establishes the session used everywhere else (Firestore included).
`kmpauth-firebase` is deliberately **not used** — it ships its own, separate Firebase
Auth client that does not share session state with GitLive's, so using both would leave
GitLive's `Firebase.auth`/`Firebase.firestore` unauthenticated. See `docs/DECISIONS.md`.

**Sync model:** Room is the local source of truth. Every synced row (`ChatEntity`,
`ChatEntryEntity`, `LlmConfigEntity` minus `apiKey`) carries `updatedAt: Long`,
`isDirty: Boolean`, `isDeleted: Boolean` (soft delete). `coreSyncFirebase` pushes dirty
rows to `users/{uid}/chats/{chatId}`, `users/{uid}/chats/{chatId}/entries/{entryId}`,
`users/{uid}/llmConfigs/{configId}`, listens for remote changes, and merges them into
Room using last-write-wins on `updatedAt`. API keys are excluded from the Firestore
mapper entirely — not filtered at read time, never written in the first place (hard
constraint #12 in `AGENTS.md`).

**Firebase setup:** see `docs/setup-firebase.md`.

## Adaptive navigation

The `navigation` module uses Navigation3's `ListDetailSceneStrategy`
(`androidx.compose.material3.adaptive.navigation3`), the same pattern used in
[pixabayeye's `NavApp.kt`](https://github.com/siarhei-luskanau/pixabayeye/blob/main/navigation/src/commonMain/kotlin/siarhei/luskanau/pixabayeye/navigation/NavApp.kt):

```kotlin
val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
NavDisplay(
    backStack = appNavigation.backStack,
    sceneStrategies = listOf(listDetailStrategy),
    entryProvider = entryProvider { ... },
)
```

Routes are a `sealed interface AppRoutes : NavKey` with `@Serializable data class`
entries. `ChatList` is tagged as the scene strategy's list pane, `Chat` as its detail
pane — on a narrow screen only one shows at a time (list, then push to detail); on a wide
screen both render side by side, with a placeholder in the detail pane until a
conversation is selected. `LlmConfig`, `Auth`, and `Settings` are ordinary
non-list-detail routes pushed on top of `NavDisplay`.

This is a different adaptive concern from `NavigationSuiteScaffold` (bar/rail/drawer
switching for a top-level tab set) — Koog Chat doesn't need that; the list-detail pair is
the only adaptive surface.

## Adding a new module

1. Create `core/<name>Api` and its backend module(s) (`core/<name><Tech>` for a real
   implementation, `core/<name>Fake` if the capability needs a no-op variant — see the
   `*Fake` rationale above), each applying `id("composeMultiplatformConvention")`.
2. Register all of them in `settings.gradle.kts`.
3. Add the backend module(s) (never the Api module directly) as a `commonMain`
   dependency of `diApp` — conditionally on `IS_FAKE_DATA_ENABLED` (or an equivalent
   build flag), per the auth/sync pattern above, if the module pair represents an
   optional backend.
4. Add every new module to the `kover { dependencies { kover(projects...) } }` block in
   the root `build.gradle.kts` so coverage is aggregated, and add backend modules to
   `checkModuleBoundaries`'s `coreImplModulePaths`.
