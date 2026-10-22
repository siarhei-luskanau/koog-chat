package koog.chat.core.llm.koog

import koog.chat.core.database.api.entity.ChatEntry

private const val CHARS_PER_TOKEN = 4
private const val BUDGET_NUMERATOR = 3
private const val BUDGET_DENOMINATOR = 4

internal fun trimToContext(
    history: List<ChatEntry>,
    prompt: String,
    maxContextTokens: Int,
): List<ChatEntry> {
    var remainingBudget = maxContextTokens * BUDGET_NUMERATOR / BUDGET_DENOMINATOR - estimateTokens(prompt)
    if (remainingBudget <= 0) return emptyList()
    val kept = ArrayDeque<ChatEntry>()
    for (entry in history.asReversed()) {
        remainingBudget -= estimateTokens(entry.content)
        if (remainingBudget < 0) break
        kept.addFirst(entry)
    }
    return kept.toList()
}

private fun estimateTokens(text: String): Int = (text.length + CHARS_PER_TOKEN - 1) / CHARS_PER_TOKEN
