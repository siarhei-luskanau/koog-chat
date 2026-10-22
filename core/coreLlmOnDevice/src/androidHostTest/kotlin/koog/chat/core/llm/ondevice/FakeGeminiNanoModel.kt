package koog.chat.core.llm.ondevice

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class FakeGeminiNanoModel(
    var status: () -> Int = { 0 },
    var tokenLimit: () -> Int = { 4096 },
    var downloadEvents: () -> List<GeminiNanoDownload> = { emptyList() },
    var downloadError: Exception? = null,
    var generated: () -> List<String> = { emptyList() },
    var generateError: Exception? = null,
) : GeminiNanoModel {
    var statusCalls = 0
    var lastPrompt: String? = null

    override suspend fun checkStatus(): Int {
        statusCalls++
        return status()
    }

    override suspend fun tokenLimit(): Int = tokenLimit.invoke()

    override fun download(): Flow<GeminiNanoDownload> =
        flow {
            downloadEvents().forEach { emit(it) }
            downloadError?.let { throw it }
        }

    override fun generate(prompt: String): Flow<String> =
        flow {
            lastPrompt = prompt
            generated().forEach { emit(it) }
            generateError?.let { throw it }
        }
}
