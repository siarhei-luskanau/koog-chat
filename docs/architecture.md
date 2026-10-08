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
core/coreLlmKoog        Koog implementation (Ollama/OpenAI/Anthropic/Google) + DefaultLlmSelector
core/coreLlmOnDevice    OnDeviceLlm per platform: ML Kit GenAI (Android), Swift bridge to
                        FoundationModels (iOS), Chrome LanguageModel (js/wasmJs), Unavailable (JVM)
core/coreAuthApi        Auth session interface: current user, sign-in, sign-out
core/coreAuthFirebase   KMPAuth (kmpauth-google) + GitLive Firebase Auth implementation
core/coreAuthFake       In-memory fake: sign-in creates a user, sign-out clears it; bound (compile-time) when IS_FAKE_DATA_ENABLED
core/coreSyncApi        Sync interface: start/stop, sync-state observation
core/coreSyncFirebase   Firestore LWW sync implementation; internally no-ops until signed in
core/coreSyncFake       No-op, sync state permanently idle, bound (compile-time) when IS_FAKE_DATA_ENABLED
```

Note: `ui/uiMain` (the template's placeholder home screen) is removed as part of
`docs/TASKS.md` row 10 — `uiChatList` takes over as the splash destination.

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
  `coreLlmKoog`, `coreLlmOnDevice`, `coreAuthFirebase`, `coreSyncFirebase`) — a concrete, real-backend
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
  two auth lines are live so far**: `coreAuthFake` (row 6, fake half) and
  `coreAuthFirebase`, which so far binds only `GoogleIdTokenProvider` via KMPAuth (no
  `AuthService` binding until GitLive lands). The two sync lines are commented placeholders until row 7
  builds those modules and row 9 finishes the `if`/`else`. Each auth/sync backend module's Koin `@Module` is also annotated
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

## Zero-setup first launch

Product requirement: *a user launches the app and chats immediately — no setup, no
sign-in, no registration. Provider setup, sign-in, and sync can happen later.* Concretely:

1. **Navigation.** Splash → `ChatList`. With zero chats, `navigation` immediately opens a
   new, empty `Chat` (detail pane; on a narrow screen it's pushed on top of the list),
   composer focused. Nothing is pushed before it: no onboarding, no `Auth`, no
   `LlmConfig`.
2. **Model choice without a config.** A chat with no explicitly chosen `LlmConfig` uses
   `DefaultLlmSelector.selected: StateFlow<SelectedLlm>` (`coreLlmApi`, implemented in
   `coreLlmKoog`). It resolves in this order, re-evaluating when any input changes:
   1. the user's own default `LlmConfig`, if one exists. `isDefault` is unique per
      provider, so several can exist; the one with the latest `updatedAt` wins. An
      existing chat keeps whatever config its last assistant entry used, if that config
      still exists;
   2. a local Ollama that answers `GET /api/tags` with at least one installed model (the
      first one is picked). It ranks above the on-device model because a running Ollama is
      a deliberate choice by the person at the keyboard, and its models aren't held to the
      ~4K-token window and per-app quotas of the system models (`docs/DECISIONS.md`). The
      candidate base URLs are per platform, from `expect fun localOllamaBaseUrls()` in
      `coreLlmKoog`:

      | Target | Candidates, in priority order | How the host's Ollama is reached |
      |---|---|---|
      | Desktop JVM | `http://localhost:11434` | directly |
      | js / wasmJs | `http://localhost:11434` | directly; Ollama's default `OLLAMA_ORIGINS` allows a `localhost` origin |
      | Android | `http://localhost:11434`, then `http://10.0.2.2:11434` | `localhost`: a physical device or emulator after `adb reverse tcp:11434 tcp:11434` tunnels the host's port over adb. `10.0.2.2`: the emulator's alias for the host's loopback, so no `adb reverse` and no `OLLAMA_HOST=0.0.0.0` are needed |
      | iOS | `http://localhost:11434` | the simulator shares the Mac's loopback; a physical iPhone has no adb-reverse equivalent, so it finds nothing here |

      All candidates are probed in parallel with a short timeout (about 1 s) so a missing
      server never delays the first chat. Of the ones that answer, the first in priority
      order wins, and its base URL becomes the virtual config's `providerUrl`. Android
      blocks cleartext HTTP by default, so `app/androidApp` ships a
      `network_security_config.xml` that permits cleartext **only** for `localhost`,
      `127.0.0.1` and `10.0.2.2`, never globally. iOS needs `NSAllowsLocalNetworking` in
      `Info.plist` for the same reason;
   3. the on-device model, if `OnDeviceLlm.availability` is `Available`;
   4. `SelectedLlm.None(reason, canDownload)` — the chat screen shows an inline
      "set up a model" state (add an API key / point at an Ollama server / download the
      on-device model when `availability` is `Downloadable`). Non-modal, never a redirect
      (`AGENTS.md` constraint #14).

   Options 2–3 are **virtual** `LlmConfig`s with fixed well-known ids
   (`builtin:ollama-local`, `builtin:on-device`), built in memory and never written to Room.
   That keeps the "no seeded config" decision intact, means they never sync (a phone's
   Gemini Nano is meaningless on a desktop), and makes deleting every user config fall back
   to them rather than re-seeding. Because they aren't Room rows, a `ChatEntry` produced by
   one stores `llmConfigId = null` (the `chat_entries.llmConfigId` foreign key to
   `llm_configs` would reject `builtin:*`). Its `llmProvider`/`llmModelId` columns still
   record what answered. `LlmSessionManagerImpl` maps any `builtin:` id to `null` when it
   builds the entry. The model picker in `ui/uiChat` lists them alongside the
   user's configs, labelled as on-device/local.
3. **Sign-in and sync later.** Chats created before sign-in are ordinary Room rows with
   `isDirty = true`; the first `coreSyncFirebase.start()` after sign-in pushes them, so
   nothing the user did while anonymous is lost or duplicated. Entries answered by a
   built-in model carry `llmConfigId = null`, so they sync like any other entry. The sync
   pull must also null out an incoming `llmConfigId` that has no local `llm_configs` row
   yet (same foreign key) rather than fail the insert. A chat whose last config doesn't
   exist on this device falls back to this device's `DefaultLlmSelector`.
4. **Discoverability, not gating.** `ui/uiAuth`/`ui/uiSettings`/`ui/uiLlmConfig` are reached
   from the chat list's settings action and from the inline "set up a model" state. An
   optional, dismissible "sign in to sync" hint may appear after the user has some chats;
   its dismissal lives in `corePrefDatastore`.

## On-device LLM (`coreLlmOnDevice`)

Koog has no on-device provider — its providers are all HTTP APIs (Ollama
included). So `coreLlmApi` defines its own small interface, and `coreLlmKoog`'s
`LlmServiceKoog` routes `LlmProvider.OnDevice` turns to it instead of to a Koog
`LLMClient`:

```
interface OnDeviceLlm {
    val availability: StateFlow<OnDeviceAvailability>  // Available | Downloadable | Downloading(progress) | Unavailable(reason)
    suspend fun download()                              // only meaningful when Downloadable
    fun stream(history: List<ChatEntry>, prompt: String): Flow<LlmChunk>  // new sealed type: Text | Reasoning | End(tokensUsed?)
    val maxContextTokens: Int
}
```

`LlmProvider` gains `OnDevice` (no `apiKey`, no `providerUrl`). This is a fifth value on
top of the four listed under *LLM provider abstraction*. `LlmServiceKoog` adapts the
`LlmChunk` flow to the same chunk callbacks and `ChatResult` that Koog-backed turns produce. `coreLlmOnDevice` is a
regular `*Impl` (only `diApp` depends on it); `coreLlmKoog` sees only the interface. It is
bound in both `IS_FAKE_DATA_ENABLED` variants — it has no backend to fake, and on CI
hardware it simply reports `Unavailable`.

| Target | Backend | Availability | Notes |
|---|---|---|---|
| Android | ML Kit GenAI Prompt API (Gemini Nano via AICore), `com.google.mlkit:genai-prompt` (beta) | `checkStatus()` → AVAILABLE / DOWNLOADABLE / DOWNLOADING / UNAVAILABLE | Supported devices only (Pixel 9+, Galaxy S26, recent OnePlus/OPPO/Xiaomi …), locked bootloader; AICore downloads the model, not the app. ~4K-token input, per-app quotas, no session API — history is rebuilt into each prompt. |
| iOS | Apple `FoundationModels` (`SystemLanguageModel`, `LanguageModelSession.streamResponse`) | `SystemLanguageModel.default.availability` | iOS 26+, Apple Intelligence device with Apple Intelligence enabled. **Swift-only**, so Kotlin/Native can't call it: `iosMain` declares an `AppleFoundationModelsBridge` interface, `app/iosApp` implements it in Swift and hands it to Koin at startup (`diApp` exports the interface in its framework). Without a bridge (or below iOS 26) → `Unavailable`. |
| js / wasmJs | Chrome built-in AI Prompt API (`LanguageModel.availability()`, `create({ monitor })`, `promptStreaming()`) via `external` declarations | `LanguageModel.availability()`; missing global → `Unavailable` | Desktop Chrome only (not Android/iOS Chrome); multi-GB first-use download, 22 GB free disk, GPU ≥4 GB VRAM or 16 GB RAM. Firefox/Safari: no API → `Unavailable`. Edge's Phi-4-mini Prompt API is flag-only/experimental — treated as a bonus if the same `LanguageModel` global exists, never relied on. |
| Desktop JVM | none | always `Unavailable` | Desktop's zero-setup path is the local-Ollama probe in `DefaultLlmSelector`. |

Things every implementer of this module must account for:

- **Small context.** Gemini Nano / Foundation Models / Chrome Nano have roughly a 4K-token
  window. `LlmSessionManagerImpl` trims the oldest turns to `maxContextTokens` before an
  `OnDevice` turn (rough token estimate is fine) rather than letting the call fail.
- **No tool calling** is assumed for `OnDevice` — feature code must not depend on it.
- **Download is user-initiated.** `Downloadable` is surfaced in the "set up a model" state
  as an explicit action (size warning on web); never auto-started on a metered connection.
- **Same output contract** as every other provider: `stream` emits text/reasoning chunks
  and an end frame, so persistence, the 150 ms throttle, and stats work unchanged
  (`tokensUsed` may be `null` where the platform doesn't report it).
- **Deferred, not rejected:** an opt-in downloadable model for devices without a system
  model (LiteRT-LM + Gemma on Android, WebLLM on WebGPU browsers, a bundled llama.cpp on
  desktop). Each is a multi-hundred-MB-to-GB download, so none is "zero setup"; revisit
  after the system-model path ships (`docs/DECISIONS.md`).

## Auth & sync design

**Sign-in flow:** `kmpauth-google` (KMPAuth 3.0.6) obtains a Google ID token →
`dev.gitlive.firebase.auth.GoogleAuthProvider.credential(idToken, null)` →
`Firebase.auth.signInWithCredential(credential)` (GitLive `firebase-kotlin-sdk`
3.0.0-alpha02) establishes the session used everywhere else (Firestore included).
`kmpauth-firebase` is deliberately **not used** — it ships its own, separate Firebase
Auth client that does not share session state with GitLive's, so using both would leave
GitLive's `Firebase.auth`/`Firebase.firestore` unauthenticated. See `docs/DECISIONS.md`.

**Getting the ID token from the UI:** KMPAuth's sign-in is a Compose state
(`rememberGoogleSignInState`; on Android it needs the Activity), so it can't sit behind a
plain `suspend` call on `AuthService`. `coreAuthApi` therefore has a second interface,
`GoogleIdTokenProvider`, with one `@Composable` member, `rememberGoogleSignInLauncher(onResult:
(Result<String>) -> Unit): GoogleSignInLauncher`. A screen calls `launcher.launch()`, gets the
ID token in `onResult`, and passes it to `AuthService.signInWithGoogleIdToken`. It never knows
which variant is bound:

- `coreAuthFirebase`'s `GoogleIdTokenProviderKmpAuth` calls `KMPAuth.initialize { google(serverId
  = GoogleAuthConfig.WEB_CLIENT_ID) }` when Koin creates it. `GoogleAuthConfig` is generated
  at build time by the module's `generateGoogleAuthConfig` task from `local.properties`'
  `GOOGLE_WEB_CLIENT_ID`. If that key is blank, KMPAuth isn't initialized and `launch()`
  delivers a failure `Result` (no crash). Desktop uses KMPAuth's default loopback redirect
  `http://localhost:8080/callback`.
- `coreAuthFake`'s `GoogleIdTokenProviderFake` returns the fixed token
  `fake-google-id-token` immediately on `launch()`.
- iOS: `iosApp.swift` forwards `onOpenURL` to `GIDSignIn.sharedInstance.handle(url)`.

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
