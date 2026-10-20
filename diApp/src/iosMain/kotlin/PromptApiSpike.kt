import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import platform.Foundation.NSDate
import platform.Foundation.NSThread
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIViewController

private const val TAG = "PromptApiSpike"
private const val SPIKE_PROMPT = "Reply in one short sentence: what is Kotlin Multiplatform?"
private const val LONG_PROMPT = "Write a detailed, multi-paragraph essay about the history of programming languages."
private const val RAW_CHUNKS_TO_LOG = 3
private const val RAW_TEXT_LIMIT = 80
private const val CANCEL_AFTER_CHUNKS = 3
private const val MILLIS_PER_SECOND = 1000
private val OVERFLOW_WORD_COUNTS = listOf(2_000, 3_000, 4_000, 6_000)

interface FoundationModelsSpikeListener {
    fun onChunk(text: String)

    fun onComplete(failure: String?)
}

interface FoundationModelsSpikeCancellable {
    fun cancel()
}

interface FoundationModelsSpikeBridge {
    fun availability(): String

    fun stream(
        prompt: String,
        listener: FoundationModelsSpikeListener,
    ): FoundationModelsSpikeCancellable
}

fun promptSpikeViewController(bridge: FoundationModelsSpikeBridge): UIViewController = ComposeUIViewController { PromptSpikeScreen(bridge) }

private class BridgeFailure(
    message: String,
) : Exception(message)

private fun FoundationModelsSpikeBridge.streamFlow(prompt: String): Flow<String> =
    callbackFlow {
        val handle =
            stream(
                prompt,
                object : FoundationModelsSpikeListener {
                    override fun onChunk(text: String) {
                        trySend(text)
                    }

                    override fun onComplete(failure: String?) {
                        log("bridge onComplete failure=$failure mainThread=${NSThread.isMainThread}")
                        if (failure == null) close() else close(BridgeFailure(failure))
                    }
                },
            )
        awaitClose {
            log("awaitClose -> bridge cancel")
            handle.cancel()
        }
    }

private fun nowMs(): Long = (NSDate().timeIntervalSince1970 * MILLIS_PER_SECOND).toLong()

private var appendToScreen: (String) -> Unit = {}

private fun log(message: String) {
    println("$TAG: $message")
    appendToScreen(message)
}

@Composable
private fun PromptSpikeScreen(bridge: FoundationModelsSpikeBridge) {
    var logText by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    appendToScreen = { logText += "$it\n" }

    fun runExclusive(block: suspend CoroutineScope.() -> Unit) {
        if (job?.isActive == true) {
            log("busy, ignored")
            return
        }
        job =
            scope.launch {
                try {
                    block()
                } catch (e: CancellationException) {
                    log("cancelled: ${e.message}")
                    throw e
                } catch (e: Exception) {
                    log("failed: ${e::class.simpleName}: ${e.message}")
                }
            }
    }

    LaunchedEffect(Unit) {
        runExclusive {
            log("availability (load): ${bridge.availability()}")
            if (bridge.availability() != "available") return@runExclusive
            streamPrompt(bridge, SPIKE_PROMPT) { reply = it }
            cancelMidStream(bridge)
            for (words in OVERFLOW_WORD_COUNTS) overflowProbe(bridge, words)
            log("auto run done")
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row {
            Button(onClick = { runExclusive { log("availability (click): ${bridge.availability()}") } }) { Text("Status") }
            Button(onClick = { runExclusive { streamPrompt(bridge, SPIKE_PROMPT) { reply = it } } }) { Text("Run") }
            Button(onClick = { job?.cancel(CancellationException("user cancel")) }) { Text("Cancel") }
        }
        Text(reply)
        Text(logText)
    }
}

private suspend fun streamPrompt(
    bridge: FoundationModelsSpikeBridge,
    prompt: String,
    onText: (String) -> Unit,
) {
    log("--- stream prompt (${prompt.length} chars)")
    val startedAt = nowMs()
    val accumulated = StringBuilder()
    var count = 0
    var last: String? = null
    try {
        bridge.streamFlow(prompt).collect { chunk ->
            if (count == 0) {
                log("firstChunkMs=${nowMs() - startedAt} collectorMainThread=${NSThread.isMainThread}")
            }
            if (count < RAW_CHUNKS_TO_LOG) {
                val startsWithPrev = last?.let { chunk.startsWith(it) } ?: "n/a"
                log("chunk[$count] len=${chunk.length} startsWithPrev=$startsWithPrev")
                log("chunk[$count] text=\"${chunk.take(RAW_TEXT_LIMIT)}\"")
            }
            count++
            last = chunk
            accumulated.append(chunk)
            onText(chunk)
        }
    } finally {
        log("chunks=$count elapsedMs=${nowMs() - startedAt}")
        log("last chunk len=${last?.length} trailingBlank=${last?.lastOrNull()?.isWhitespace()}")
        log("cumulative interpretation (last chunk): ${last.orEmpty()}")
        log("deltas interpretation (first 200): ${accumulated.take(200)}")
    }
}

private suspend fun cancelMidStream(bridge: FoundationModelsSpikeBridge) {
    log("--- cancel after $CANCEL_AFTER_CHUNKS chunks")
    val startedAt = nowMs()
    var received = 0
    bridge.streamFlow(LONG_PROMPT).take(CANCEL_AFTER_CHUNKS).collect { received++ }
    log("collector stopped after $received chunks, elapsedMs=${nowMs() - startedAt}")
}

private suspend fun overflowProbe(
    bridge: FoundationModelsSpikeBridge,
    words: Int,
) {
    log("--- overflow probe: $words words")
    val prompt = "Summarize in one word: " + List(words) { "apple" }.joinToString(" ")
    val startedAt = nowMs()
    try {
        var chunks = 0
        bridge.streamFlow(prompt).collect { chunks++ }
        log("overflow probe $words words: ok, chunks=$chunks elapsedMs=${nowMs() - startedAt}")
    } catch (e: BridgeFailure) {
        log("overflow probe $words words: failed: ${e.message}")
    }
}
