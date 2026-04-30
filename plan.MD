# Koog Chat — Implementation Plan

## Current State

| Module | State |
|---|---|
| `core/coreCommon` | Complete — dispatcher/platform abstractions |
| `core/corePref` | Complete — DataStore prefs, `AppMode`, `selectedLlmConfigId` |
| `core/coreDatabaseApi` | Complete — entities, DAOs, repository interfaces |
| `core/coreDatabaseRoom` | Complete — Room impl, platform builders, auto-seed in `LlmConfigRepositoryRoom` |
| `core/coreLlm` | Complete — `LlmService`, `LlmServiceKoog` (Ollama streaming), `LlmSessionManager` |
| `navigation` | Complete — Splash→ChatList→Chat routes, back stack wired |
| `diApp` | Complete — all modules wired |
| `ui/uiCommon` | Complete — theme, components, design system |
| `ui/uiChat` | Complete — paging messages, streaming, Simple/Advanced mode |
| `ui/uiChatList` | Complete — paging chat list, FAB new chat, search bar |
| `ui/uiSplash` | Complete |
| `app/*` | Minimal entry points |

---

## Step 1 — Database API contracts (`core/coreDatabaseApi`)

Define all entities, DAOs, and repository interfaces as pure Kotlin (no Room annotations here).

**Files to create:**

```
core/coreDatabaseApi/src/commonMain/kotlin/koog/chat/core/database/api/
  entity/LlmConfig.kt       — data class with fields from spec
  entity/Chat.kt
  entity/ChatEntry.kt       — type enum: USER_PROMPT, THINKING, SUCCESS_RESPONSE, ERROR_RESPONSE
  dao/LlmConfigDao.kt       — interface: insert, update, delete, getAll(): Flow<List<LlmConfig>>, getDefault()
  dao/ChatDao.kt            — interface: insert, update, delete, getAll(): Flow<List<Chat>>, getById()
  dao/ChatEntryDao.kt       — interface: insert, getPaged(chatId): PagingSource<Int, ChatEntry>
  repository/LlmConfigRepository.kt
  repository/ChatRepository.kt
  repository/ChatEntryRepository.kt
  DatabaseSeed.kt           — interface: suspend fun seed()
```

**Entity fields** (per spec):
- `LlmConfig`: `id`, `provider`, `modelId`, `apiKey`, `providerUrl`, `isDefault`
- `Chat`: `id`, `title`, `createdAt`
- `ChatEntry`: `id`, `chatId`, `type`, `content`, `thinkingContent`, `llmConfigId` (nullable), `llmProvider`, `llmModelId`, `tokensUsed`, `tokensPerSecond`, `responseTimeMs`, `timestamp`

---

## Step 2 — Room implementation (`core/coreDatabaseRoom`)

Implement the contracts from Step 1 using Room3 multiplatform.

**Implemented files:**

```
core/coreDatabaseRoom/src/commonMain/kotlin/koog/chat/core/database/room/
  entity/LlmConfigEntity.kt
  entity/ChatEntity.kt
  entity/ChatEntryEntity.kt
  dao/LlmConfigDao.kt
  dao/ChatDao.kt
  dao/ChatEntryDao.kt
  AppDatabase.kt
  repository/LlmConfigRepositoryRoom.kt  — auto-seeds default QWEN_3_5_0_8B on first getAllFlow() read
  repository/ChatRepositoryRoom.kt
  repository/ChatEntryRepositoryRoom.kt
  CoreDatabaseRoomCommonModule.kt
  util/PagingExtensions.kt
```

**Platform-specific database builders:**
```
androidMain/kotlin/.../AndroidRoomDatabaseProvider.kt
jvmMain/kotlin/.../JvmRoomDatabaseProvider.kt
iosMain/kotlin/.../IosRoomDatabaseProvider.kt
webMain/kotlin/.../WebRoomDatabaseProvider.kt
```

**Seed approach**: instead of a separate `DatabaseSeedImpl`, seeding is embedded in `LlmConfigRepositoryRoom.getAllFlow()` via `.onEach { if (it.isEmpty()) save(defaultQwenConfig) }`. No splash-screen seed call needed.

**Foreign key**: `ChatEntry.llmConfigId → LlmConfig.id` with `onDelete = CASCADE` → set null via nullable field (Room3 handles nullability).

---

## Step 3 — Expand preferences (`core/corePref`)

Add `AppMode` (Simple/Advanced) and `selectedLlmConfigId` to persistent settings.

**Changes:**
- `PrefData.kt` — add `appMode: AppMode = AppMode.Simple`, `selectedLlmConfigId: String? = null`
- `PrefService.kt` — add `fun getAppMode(): Flow<AppMode>`, `suspend fun setAppMode(mode: AppMode)`, `fun getSelectedLlmConfigId(): Flow<String?>`, `suspend fun setSelectedLlmConfigId(id: String?)`
- `PrefServiceDataStore.kt` — implement new methods

---

## Step 4 — Wire DI (`diApp`)

Connect all modules and handle first-run database seeding.

**Implemented:**
- `DiKoinApplication.kt` — wires `CoreCommonCommonModule`, `CoreDatabaseRoomCommonModule`, `CoreLlmCommonModule`, `CorePrefCommonModule`, `NavigationCommonModule`, `ChatCommonModule`, `ChatListCommonModule`, `SplashCommonModule`, `DiCommonModule`
- No explicit seed call needed — seeding is lazy via `LlmConfigRepositoryRoom.getAllFlow()`

---

## Step 5 — Navigation: add Chat route

**Changes in `navigation` module:**
- `AppRoutes.kt` — add `data class Chat(val chatId: String) : AppRoutes`
- `AppNavigation.kt` — add handler for `Chat` route, render `ChatScreen`
- `MainNavigationCallback.kt` (in `ui/uiMain`) — add `goToChat(chatId: String)`
- `AppNavigation.kt` — wire `MainNavigationCallback.goToChat` → push `Chat` onto back stack
- `AppNavigation.kt` — wire `ChatNavigationCallback.goBack` → pop back stack

---

## Step 6 — Chat list screen (`ui/uiChatList`)

Replace stub with a real chat list.

**Files to change/create:**
- `MainViewModel.kt` — inject `ChatRepository`; expose `Flow<List<Chat>>` as view state; handle `NewChat`, `OpenChat(id)`, `DeleteChat(id)` events
- `MainViewState.kt` — `Success(chats: List<Chat>)`
- `MainViewEvent.kt` — add `NewChat`, `OpenChat(chatId: String)`, `DeleteChat(chatId: String)`
- `MainNavigationCallback.kt` — add `goToChat(chatId: String)`
- `MainScreen.kt` — `LazyColumn` of `ChatListItem`; FAB → new chat (creates DB entry, navigates); swipe-to-delete; empty state

**New chat flow**: on `NewChat` event → insert `Chat(title="New Chat", createdAt=now)` → navigate to `Chat(id)`.

---

## Step 7 — Chat ViewModel: real implementation (`ui/uiChat`)

Wire up message persistence and Koog LLM calls.

**Dependencies to add to `uiChat`:** `coreDatabaseApi`, `koog-agents`

**`ChatViewModel.kt` changes:**
- Inject `ChatRepository`, `ChatEntryRepository`, `LlmConfigRepository`
- Load `PagingData<ChatEntry>` from `ChatEntryDao.getPaged(chatId)` via `Pager`
- On `SendMessage`:
  1. Insert `ChatEntry(type=USER_PROMPT, content=inputText, llmConfigId=selected, ...)`
  2. Insert `ChatEntry(type=THINKING, content="")` placeholder (update in place while streaming)
  3. Call Koog agent with chat history
  4. Stream response → update `SUCCESS_RESPONSE` entry with content + metrics
  5. On error → insert `ERROR_RESPONSE` entry
- `ChatViewState.Success` — expose `PagingData<ChatEntry>` instead of preview list
- Handle `PickModel` → show model selection dialog backed by `LlmConfigRepository.getAll()`

**`ChatScreen.kt` changes:**
- Use `LazyPagingItems` for message list
- Display `ThinkingBlock` when `type=THINKING` and mode=Advanced (hide in Simple mode)
- Display `MetricsRow` only in Advanced mode
- Show total tokens in header only in Advanced mode

---

## Step 8 — Koog LLM integration

Create a dedicated service wrapping the Koog library.

**New module or file** (can live in `coreDatabaseApi` or a new `coreLlm` module — prefer `core/coreLlm` for isolation):

```
core/coreLlm/src/commonMain/kotlin/koog/chat/core/llm/
  LlmService.kt              — interface: suspend fun chat(messages, config, onToken): ChatResult
  LlmServiceKoog.kt          — implementation using koog-agents
  ChatResult.kt              — data class: content, thinkingContent, tokensUsed, tokensPerSecond, responseTimeMs
  CoreLlmCommonModule.kt     — Koin module
```

**Koog usage:**
- Use `OllamaLLMClient` (or equivalent) from `koog-agents`
- Provider URL from `LlmConfig.providerUrl` (default `http://localhost:11434`)
- API key from `LlmConfig.apiKey`
- Support streaming for live thinking display

**Wire**: add `CoreLlmCommonModule` to `DiKoinApplication`.

---

## Step 9 — Screenshot & UI tests

Add screenshot tests for all new/changed screens.

| Screen | Test file | Platforms |
|---|---|---|
| MainScreen (chat list) | `uiMain` | Android, JVM, iOS |
| ChatScreen (with messages) | `uiChat` | Android, JVM, iOS |
| ChatScreen (Simple mode) | `uiChat` | Android, JVM, iOS |
| ChatScreen (Advanced mode + metrics) | `uiChat` | Android, JVM, iOS |

Follow existing Roborazzi test patterns in `ui/uiCommon` and `ui/uiSplash`.

---

## Step 10 — Settings: AppMode toggle

Minimal settings accessible from Main screen (v0.1 scope: AppMode only).

**Options:**
- A) Settings dialog inside `MainScreen` (simpler, no new screen)
- B) Simple settings screen `ui/uiSettings` (cleaner, future-proof)

**Recommendation: A** — single toggle in a `ModalBottomSheet` or `AlertDialog` on `MainScreen`. Reads/writes `PrefService.appMode`.

---

## Step 11 — Repository additions for LLM configs (`core/coreDatabaseApi` + `core/coreDatabaseRoom`)

**`LlmConfigRepository.kt`** — add:
```kotlin
suspend fun getById(id: String): LlmConfig?
suspend fun setDefault(id: String)   // clears isDefault on all rows, sets it on `id`
```

**`LlmConfigDao.kt`** — add:
```sql
@Query("SELECT * FROM llm_configs WHERE id = :id")
suspend fun getById(id: String): LlmConfigEntity?

@Query("UPDATE llm_configs SET isDefault = 0")
suspend fun clearDefault()

@Query("UPDATE llm_configs SET isDefault = 1 WHERE id = :id")
suspend fun setDefault(id: String)
```

`LlmConfigRepositoryRoom.setDefault()` wraps `clearDefault()` + `setDefault(id)` in `@Transaction`.

---

## Step 12 — Navigation: LLM config routes (`navigation` module)

**`AppRoutes.kt`** — add:
```kotlin
@Serializable data object LlmConfigList : AppRoutes
@Serializable data class LlmConfigDetails(val configId: String?) : AppRoutes
```

**`AppNavigation.kt`** — implement new `LlmConfigNavigationCallback`:
- `openLlmConfigList()` → push `LlmConfigList`
- `openLlmConfigDetails(configId: String?)` → push `LlmConfigDetails(configId)`
- reuse existing `goBack()`

`ChatListNavigationCallback` gets `openLlmConfigList()` too.

**`NavigationCommonModule.kt`**:
```kotlin
navigation<AppRoutes.LlmConfigList> { LlmConfigListScreen(viewModel = koinViewModel()) }
navigation<AppRoutes.LlmConfigDetails> { route ->
    LlmConfigDetailsScreen(viewModel = koinViewModel(key = route.configId ?: "new") { parametersOf(route.configId) })
}
```

---

## Step 13 — LLM config list & details screens (`ui/uiLlmConfig`, new module)

One module for both screens — small, share data model and navigation context.

Gradle setup mirrors `ui/uiChatList/build.gradle.kts` (composeMultiplatformConvention + roborazzi), deps on `coreCommon`, `coreDatabaseApi`, `uiCommon`. Add `:ui:uiLlmConfig` to `settings.gradle.kts`.

```
ui/uiLlmConfig/src/commonMain/kotlin/koog/chat/ui/llmconfig/
  LlmConfigNavigationCallback.kt   — openConfigDetails(configId: String?), goBack()
  LlmConfigCommonModule.kt         — @Module @ComponentScan

  list/
    LlmConfigListViewState.kt      — Loading | Success(configs: List<LlmConfig>)
    LlmConfigListViewEvent.kt       — OpenDetails(id), AddNew, Delete(id), NavigateBack
    LlmConfigListViewModel.kt       — collects LlmConfigRepository.getAllFlow(), no paging (small list)
    LlmConfigListScreen.kt          — Scaffold + LazyColumn of provider rows (provider, modelId, "Default" badge, delete icon), FAB "Add provider"

  details/
    LlmConfigDetailsViewState.kt    — provider, modelId, apiKey, providerUrl, isDefault, isNew, canDelete
    LlmConfigDetailsViewEvent.kt    — FieldChanged(...), ToggleDefault, Save, Delete, NavigateBack
    LlmConfigDetailsViewModel.kt    — @InjectedParam configId: String?; loads via getById() or starts blank; Save → save() (+ setDefault if toggled, generating Uuid if new); Delete → repository.delete()
    LlmConfigDetailsScreen.kt       — form: provider dropdown (ExposedDropdownMenuBox over LlmProvider.entries — currently just Ollama), modelId TextField, apiKey TextField (PasswordVisualTransformation, optional), providerUrl TextField (optional, placeholder "http://localhost:11434"), "Set as default" Switch, Save action in TopAppBar, Delete action (hidden if isDefault && only config)
```

**Validation rules** in `LlmConfigDetailsViewModel`:
- `modelId` required (non-blank) to enable Save
- Cannot delete the last remaining config
- Deleting the current default auto-promotes another config (e.g. first remaining) to default — keeps `LlmConfigRepositoryRoom`'s auto-seed invariant ("always at least one default") intact

---

## Step 14 — Wire entry point (`ui/uiChatList`)

- `ChatListViewEvent` — add `OpenLlmConfigList`
- `SettingsBottomSheet` — add a list item "LLM Providers" below the AppMode radio group, wired to `onEvent(ChatListViewEvent.OpenLlmConfigList)`
- `ChatListViewModel.onEvent` → `navigationCallback.openLlmConfigList()`

---

## Step 15 — DI, resources & tests for LLM config screens

- `DiKoinApplication.kt` — add `LlmConfigCommonModule::class`
- `ui/uiCommon` — new strings: `llm_providers`, `add_provider`, `model_id`, `api_key`, `provider_url`, `set_as_default`, `delete_provider`, `save`; new icon `ic_delete` (if not present) and `ic_settings`/`ic_llm` for the entry point row
- Screenshot tests (Roborazzi, Android/JVM/iOS) for: list (empty / multiple configs, light/dark), details (new / edit existing, light/dark)
- Common-module unit test for `LlmConfigRepositoryRoom.setDefault()` (only one row has `isDefault = true` after call), following the pattern of the existing `ChatRepository` common tests

---

## Dependency Graph (build order)

```
core/coreCommon
    ↓
core/corePref         core/coreDatabaseApi
    ↓                        ↓
    └──────────────┬──────────┘
                   ↓
          core/coreDatabaseRoom
                   ↓
          core/coreLlm (new)
                   ↓
       ui/uiCommon  ui/uiMain  ui/uiChat  ui/uiSplash
                   ↓
               navigation
                   ↓
                 diApp
                   ↓
             app/* (Android, Desktop, Web)
```

---

## Implementation Order (parallel where possible)

| Wave | Tasks | Status |
|---|---|---|
| 1 | Step 1 (`coreDatabaseApi`) + Step 3 (`corePref` expand) | ✅ Done |
| 2 | Step 2 (`coreDatabaseRoom`) | ✅ Done |
| 3 | Step 8 (`coreLlm` module) | ✅ Done |
| 4 | Step 4 (DI wiring) + Step 5 (navigation) | ✅ Done |
| 5 | Step 6 (`uiChatList`) + Step 7 (`uiChat`) | ✅ Done |
| 6 | Step 10 (AppMode settings) | ✅ Done |
| 7 | Step 9 (screenshot tests) | ✅ Done |

---

## Implementation Order — v0.2 (parallel where possible)

| Wave | Tasks | Status |
|---|---|---|
| 1 | Step 11 (`coreDatabaseApi`/`coreDatabaseRoom` additions) | ✅ Done |
| 2 | Step 12 (navigation routes) + Step 13 (`uiLlmConfig` module) | ⬜ Todo |
| 3 | Step 14 (entry point in `uiChatList`) | ⬜ Todo |
| 4 | Step 15 (DI, resources, tests) | ⬜ Todo |

---

## Out of scope for v0.1

- LLM providers list screen (v0.2)
- LLM provider details screen (v0.2)
- iOS app entry point / Xcode project wiring
- Web PWA / service workers
- Message search
- Chat export

---

## Out of scope for v0.2

- Chats-by-config filter (tapping a provider row to see chats using that config — RAW.MD mentions this, but spec.MD's screens table doesn't list it; deferred to a later pass)
- iOS app entry point / Xcode project wiring
- Web PWA / service workers
- Message search
- Chat export
