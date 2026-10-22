package koog.chat.core.llm.ondevice

import kotlinx.coroutines.flow.Flow

internal sealed interface GeminiNanoDownload {
    data class Started(
        val bytesToDownload: Long,
    ) : GeminiNanoDownload

    data class Progress(
        val bytesDownloaded: Long,
    ) : GeminiNanoDownload

    data class Failed(
        val cause: Exception,
    ) : GeminiNanoDownload

    data object Completed : GeminiNanoDownload
}

internal interface GeminiNanoModel {
    suspend fun checkStatus(): Int

    suspend fun tokenLimit(): Int

    fun download(): Flow<GeminiNanoDownload>

    fun generate(prompt: String): Flow<String>
}
