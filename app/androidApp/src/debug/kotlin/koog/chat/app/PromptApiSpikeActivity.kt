package koog.chat.app

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "PromptApiSpike"
private const val SPIKE_PROMPT = "Reply in one short sentence: what is Kotlin Multiplatform?"
private const val RAW_CHUNKS_TO_LOG = 3
private const val RAW_TEXT_LIMIT = 80

internal class PromptApiSpikeActivity : ComponentActivity() {
    private val model: GenerativeModel by lazy { Generation.getClient() }
    private lateinit var logView: TextView
    private lateinit var replyView: TextView
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildLayout())
        log("device: ${Build.MANUFACTURER} ${Build.MODEL}, SDK ${Build.VERSION.SDK_INT}")
        runExclusive { logModelInfo("load") }
    }

    override fun onDestroy() {
        job?.cancel()
        model.close()
        super.onDestroy()
    }

    private fun buildLayout(): ScrollView {
        logView = TextView(this).apply { setTextIsSelectable(true) }
        replyView = TextView(this).apply { setTextIsSelectable(true) }
        val column =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                fitsSystemWindows = true
                addView(button("Check status") { runExclusive { logModelInfo("click") } })
                addView(button("Download") { runExclusive { download() } })
                addView(button("Run prompt") { runExclusive { streamPrompt() } })
                addView(button("Cancel") { job?.cancel(CancellationException("user cancel")) })
                addView(replyView)
                addView(logView)
            }
        return ScrollView(this).apply { addView(column) }
    }

    private fun button(
        label: String,
        onClick: () -> Unit,
    ): Button =
        Button(this).apply {
            text = label
            setOnClickListener { onClick() }
        }

    private fun runExclusive(block: suspend () -> Unit) {
        if (job?.isActive == true) {
            log("busy, ignored")
            return
        }
        job =
            lifecycleScope.launch {
                try {
                    block()
                } catch (e: CancellationException) {
                    log("cancelled: ${e.message}")
                    throw e
                } catch (e: GenAiException) {
                    log("GenAiException errorCode=${e.errorCode} retryDelay=${e.retryDelay}: ${e.message}")
                } catch (e: Exception) {
                    log("failed: ${e::class.qualifiedName}: ${e.message}")
                }
            }
    }

    private suspend fun logModelInfo(label: String) {
        val status = model.checkStatus()
        log("checkStatus ($label): ${statusName(status)} ($status)")
        if (status != FeatureStatus.AVAILABLE) return
        log("baseModelName=${model.getBaseModelName()}")
        log("tokenLimit=${model.getTokenLimit()}")
        log("systemPrompt=${model.isSystemPromptAvailable()} caching=${model.isCachingFeatureAvailable()}")
        log("promptTokens=${model.countTokens(generateContentRequest(TextPart(SPIKE_PROMPT)) {}).totalTokens}")
    }

    private suspend fun download() {
        model.download().collect { status ->
            when (status) {
                is DownloadStatus.DownloadStarted -> log("download started, bytes=${status.bytesToDownload}")
                is DownloadStatus.DownloadProgress -> log("download progress, bytes=${status.totalBytesDownloaded}")
                is DownloadStatus.DownloadFailed -> log("download failed: errorCode=${status.e.errorCode} ${status.e.message}")
                else -> log("download: $status")
            }
        }
    }

    private suspend fun streamPrompt() {
        val startedAt = SystemClock.elapsedRealtime()
        val accumulated = StringBuilder()
        var count = 0
        var last: String? = null
        var finishReason: Int? = null
        try {
            model.generateContentStream(SPIKE_PROMPT).collect { response ->
                val candidate = response.candidates.firstOrNull()
                val chunk = candidate?.text.orEmpty()
                if (count == 0) {
                    log("firstChunkMs=${SystemClock.elapsedRealtime() - startedAt} thread=${Thread.currentThread().name}")
                }
                if (count < RAW_CHUNKS_TO_LOG) logRawChunk(count, chunk, last, accumulated.toString())
                count++
                last = chunk
                candidate?.finishReason?.let { finishReason = it }
                accumulated.append(chunk)
                replyView.text = accumulated
            }
        } finally {
            log("chunks=$count finishReason=$finishReason elapsedMs=${SystemClock.elapsedRealtime() - startedAt}")
            log("last chunk=\"${last.orEmpty().take(RAW_TEXT_LIMIT)}\" blank=${last?.isBlank()}")
            log("deltas interpretation: $accumulated")
            log("cumulative interpretation (last chunk): ${last.orEmpty()}")
        }
    }

    private fun logRawChunk(
        index: Int,
        chunk: String,
        previous: String?,
        accumulated: String,
    ) {
        val startsWithPrev = previous?.let { chunk.startsWith(it) } ?: "n/a"
        log("chunk[$index] len=${chunk.length} startsWithPrev=$startsWithPrev startsWithAccumulated=${chunk.startsWith(accumulated)}")
        log("chunk[$index] text=\"${chunk.take(RAW_TEXT_LIMIT)}\"")
    }

    private fun statusName(status: Int): String =
        when (status) {
            FeatureStatus.UNAVAILABLE -> "Unavailable"
            FeatureStatus.DOWNLOADABLE -> "Downloadable"
            FeatureStatus.DOWNLOADING -> "Downloading"
            FeatureStatus.AVAILABLE -> "Available"
            else -> "Unknown"
        }

    private fun log(message: String) {
        Log.i(TAG, message)
        logView.append("$message\n")
    }
}
