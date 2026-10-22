package koog.chat.core.llm.ondevice

import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType

private const val TURN_SEPARATOR = "\n\n"

internal fun formatTranscript(
    history: List<ChatEntry>,
    prompt: String,
): String {
    val turns = history.mapNotNull { it.toTranscriptTurn() }
    if (turns.isEmpty()) return prompt
    return (turns + "User: $prompt" + "Assistant:").joinToString(TURN_SEPARATOR)
}

private fun ChatEntry.toTranscriptTurn(): String? =
    when (type) {
        ChatEntryType.USER_PROMPT -> "User: $content"
        ChatEntryType.SUCCESS_RESPONSE -> "Assistant: $content"
        ChatEntryType.THINKING, ChatEntryType.ERROR_RESPONSE -> null
    }
