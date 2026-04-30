# Koog Chat — v0.1 Spec

## Overview

Cross-platform LLM chat client built with Kotlin Multiplatform. Primary audience: developers (open-source/portfolio) and non-technical end users.

## Tech Stack

- Kotlin Multiplatform (Android, iOS, JVM, Web)
- Compose Multiplatform
- Koog Library (all LLM functionality)
- Kotlinx Serialization
- Kotlinx Coroutines
- Navigation3
- Koin DI
- Kotlinx DateTime
- androidx-room3 (database + migrations)
- androidx-paging (chat items)

## User Modes

| Mode | Behavior |
|---|---|
| Simple | "Thinking..." shown during model reasoning, disappears on completion. No token metrics shown. |
| Advanced | Full reasoning text shown as chat entry. Token metrics (time, tokens/s, speed) shown per message. Optional total tokens in chat header. |

Mode is a global setting, toggled once in app settings.

## Default LLM Config

Seeded at install:

```kotlin
val QWEN_3_5_0_8B: LLModel = LLModel(
    provider = LLMProvider.Ollama,
    id = "qwen3.5:0.8b",
    capabilities = listOf(
        LLMCapability.Schema.JSON.Basic,
        LLMCapability.Temperature,
        LLMCapability.Thinking,
        LLMCapability.ToolChoice,
        LLMCapability.Tools,
        LLMCapability.Vision.Image,
    ),
    contextLength = 256_000,
)
```

## Screens — v0.1

| Screen | Status |
|---|---|
| Chats list | v0.1 |
| Chat screen | v0.1 |
| LLM providers list (chat configs) | v0.2 |
| LLM provider details | v0.2 |

### Chat Screen

- LLM selector near Send button — user can switch model mid-chat
- Each message records which model generated it
- Paging via androidx-paging

## Database Schema

```
LlmConfig {
  id,
  provider,
  modelId,
  apiKey,
  providerUrl,
  isDefault
}

Chat {
  id,
  title,
  createdAt
}

ChatEntry {
  id,
  chatId,
  type,              // USER_PROMPT | THINKING | SUCCESS_RESPONSE | ERROR_RESPONSE
  content,
  thinkingContent,   // always saved regardless of mode
  llmConfigId,       // nullable FK, ON DELETE SET NULL
  llmProvider,       // value copy at write time
  llmModelId,        // value copy at write time
  tokensUsed,        // null for user/error entries
  tokensPerSecond,   // null for user/error entries
  responseTimeMs,    // null for user/error entries
  timestamp          // GMT+0 time zone
}
```

### Foreign Keys

AppSettings stored using androidx-datastore : core/corePref/src/commonMain/kotlin/koog/chat/core/pref/PrefData.kt

### Foreign Keys

- `ChatEntry.chatId → Chat.id`
- `ChatEntry.llmConfigId → LlmConfig.id` (nullable, ON DELETE SET NULL)

### LlmConfig Deletion

On delete: `ChatEntry.llmConfigId` set to null. `llmProvider` and `llmModelId` are copied at write time and never updated — history remains accurate after deletion. Display always reads from entry fields, not joined config.

## Testing Strategy

| Layer | Approach |
|---|---|
| Manual testing | JVM (primary) |
| Screenshot regression | Android + JVM + iOS |
| UI interaction | Compose UI tests |
| Unit/logic | Common module (all targets) |

## UI Design System

Material3 defaults.