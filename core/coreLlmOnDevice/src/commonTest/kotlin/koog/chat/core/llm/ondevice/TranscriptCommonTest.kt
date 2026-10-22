package koog.chat.core.llm.ondevice

import koog.chat.core.database.api.entity.ChatEntryType
import kotlin.test.Test
import kotlin.test.assertEquals

class TranscriptCommonTest {
    @Test
    fun formatTranscript_shouldReturnPromptUnchanged_whenHistoryEmpty() {
        assertEquals("Hi there", formatTranscript(emptyList(), "Hi there"))
    }

    @Test
    fun formatTranscript_shouldReturnPromptUnchanged_whenHistoryHasOnlySkippedTypes() {
        val history =
            listOf(
                chatEntry(ChatEntryType.THINKING, "pondering"),
                chatEntry(ChatEntryType.ERROR_RESPONSE, "boom"),
            )

        assertEquals("Hi", formatTranscript(history, "Hi"))
    }

    @Test
    fun formatTranscript_shouldFormatTurnsInOrder_whenHistoryPresent() {
        val history =
            listOf(
                chatEntry(ChatEntryType.USER_PROMPT, "first"),
                chatEntry(ChatEntryType.SUCCESS_RESPONSE, "answer"),
            )

        assertEquals(
            "User: first\n\nAssistant: answer\n\nUser: next\n\nAssistant:",
            formatTranscript(history, "next"),
        )
    }

    @Test
    fun formatTranscript_shouldSkipThinkingAndErrorEntries_whenMixedWithOthers() {
        val history =
            listOf(
                chatEntry(ChatEntryType.USER_PROMPT, "q1"),
                chatEntry(ChatEntryType.THINKING, "hmm"),
                chatEntry(ChatEntryType.ERROR_RESPONSE, "failed"),
                chatEntry(ChatEntryType.USER_PROMPT, "q2"),
                chatEntry(ChatEntryType.SUCCESS_RESPONSE, "a2"),
            )

        assertEquals(
            "User: q1\n\nUser: q2\n\nAssistant: a2\n\nUser: q3\n\nAssistant:",
            formatTranscript(history, "q3"),
        )
    }
}
