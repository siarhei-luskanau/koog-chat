package koog.chat.core.llm.ondevice

internal class ChromePromptApiException(
    message: String,
    cause: Throwable,
) : Exception(message, cause)
