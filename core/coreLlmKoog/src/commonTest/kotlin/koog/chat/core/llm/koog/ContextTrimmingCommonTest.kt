package koog.chat.core.llm.koog

import koog.chat.core.database.api.entity.ChatEntryType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ContextTrimmingCommonTest {
    private val history =
        listOf(
            entry("1", "a".repeat(40)),
            entry("2", "b".repeat(40)),
            entry("3", "c".repeat(40)),
        )

    @Test
    fun historyThatFitsIsReturnedUnchanged() {
        val trimmed = trimToContext(history, prompt = "hi", maxContextTokens = 4096)

        assertEquals(history, trimmed)
    }

    @Test
    fun oldestEntriesAreDroppedFirst() {
        val trimmed = trimToContext(history, prompt = "hi", maxContextTokens = 40)

        assertEquals(listOf("2", "3"), trimmed.map { it.id })
    }

    @Test
    fun chronologicalOrderIsKeptAfterDropping() {
        val trimmed = trimToContext(history, prompt = "hi", maxContextTokens = 40)

        assertEquals(history.drop(1), trimmed)
    }

    @Test
    fun entryThatFillsRemainingBudgetExactlyIsKept() {
        val trimmed = trimToContext(listOf(entry("1", "a".repeat(40))), prompt = "p".repeat(8), maxContextTokens = 16)

        assertEquals(listOf("1"), trimmed.map { it.id })
    }

    @Test
    fun entryOneTokenOverRemainingBudgetIsDropped() {
        val trimmed = trimToContext(listOf(entry("1", "a".repeat(41))), prompt = "p".repeat(8), maxContextTokens = 16)

        assertTrue(trimmed.isEmpty())
    }

    @Test
    fun trimmingStopsAtFirstNewestEntryThatDoesNotFit() {
        val mixed =
            listOf(
                entry("old-small", "a".repeat(4)),
                entry("big", "b".repeat(120)),
                entry("new-small", "c".repeat(4)),
            )

        val trimmed = trimToContext(mixed, prompt = "", maxContextTokens = 40)

        assertEquals(listOf("new-small"), trimmed.map { it.id })
    }

    @Test
    fun noHistoryIsKeptWhenPromptAloneExceedsBudget() {
        val trimmed = trimToContext(history, prompt = "p".repeat(400), maxContextTokens = 100)

        assertTrue(trimmed.isEmpty())
    }

    private fun entry(
        id: String,
        content: String,
    ) = chatEntry(id = id, chatId = "chat", type = ChatEntryType.USER_PROMPT, content = content)
}
