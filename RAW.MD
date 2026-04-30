Create a application for chat with LLM client using:
- Kotlin Multiplatform,
- Compose Multiplatform,
- Koog Library,
- Ktor Client,
- Kotlinx Serialization,
- Kotlinx Coroutines,
- Navigation3,
- Koin DI,
- Kotlinx DateTime.

Kotlin Multiplatform targets: Android, iOS, JVM, Web.

LLM functionality should be implemented using Koog Library only.

By default, use local ollama client and `qwen3.5:0.8b` model.
```
val QWEN_3_5_0_8B: LLModel = LLModel(
            provider = LLMProvider.Ollama,
            id = "qwen3.5:0.8b",
            capabilities =
            listOf(
                LLMCapability.Schema.JSON.Basic,
                LLMCapability.Temperature,
                LLMCapability.Thinking,
                LLMCapability.ToolChoice,
                LLMCapability.Tools,
                LLMCapability.Vision.Image,
            ),
            contextLength = 256_000,
        )
    }
```


Koog Chat should use next entities: 
- chat, 
- chat entries (user prompts, error responses, success responses, thinking progress, etc)
- chat config (llm provider, model id, API key, provider URL, etc)

UI chat screen should show chat items using androidx-paging.

Chat data should be saved in database using androidx-room3.

Screens: 
- Chats list screen.
- Chat screen.
- LLM providers list settings (chat configs) (Add, remove, edit buttons). If user clicks to a LLM provider item - user will se the list od chats with selected chat config. 
- LLM provider (chat config) details (select llm provider, model id, API key, provider URL, etc)

How to generate a UI Design system for Compose Multiplatform application?

############## KOOG ##############

Analyze a project, plan.MD and spec.MD.
We need to create a LLM chat using Koog Library "1.0.0".
Koog GitHub project https://github.com/JetBrains/koog
Koog documentation https://docs.koog.ai/

LLM chat process updates database and chat screen should listen database updates only.

We need to create app architecture to launch a long time invoking prompts to Koog.

It means that user can send a message (prompt) in the chat and leave a chat screen, then user can return to the chat screen and see a prompt invocation progress.
That mean chat process should work out of scope chat screen life cycle.

Implement Step 7 and Step 8

Verify code changes by launching `:app:desktopApp:jar` gradle task