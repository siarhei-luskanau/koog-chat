package koog.chat.core.llm.ondevice

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
internal class MlKitGeminiNanoModel : GeminiNanoModel {
    private val client: GenerativeModel by lazy { Generation.getClient() }

    override suspend fun checkStatus(): Int = client.checkStatus()

    override suspend fun tokenLimit(): Int = client.getTokenLimit()

    override fun download(): Flow<GeminiNanoDownload> =
        client.download().map { status ->
            when (status) {
                is DownloadStatus.DownloadStarted -> GeminiNanoDownload.Started(status.bytesToDownload)
                is DownloadStatus.DownloadProgress -> GeminiNanoDownload.Progress(status.totalBytesDownloaded)
                is DownloadStatus.DownloadFailed -> GeminiNanoDownload.Failed(status.e)
                else -> GeminiNanoDownload.Completed
            }
        }

    override fun generate(prompt: String): Flow<String> =
        client.generateContentStream(prompt).map { response ->
            response.candidates
                .firstOrNull()
                ?.text
                .orEmpty()
        }
}
