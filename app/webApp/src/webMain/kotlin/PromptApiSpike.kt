@file:OptIn(ExperimentalWasmJsInterop::class)

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.Promise

internal expect val spikeTarget: String

internal expect fun errorText(e: Throwable): String

external interface PromptSession : JsAny

external interface StreamReader : JsAny

external interface ReadResult : JsAny

private const val SPIKE_PROMPT = "Reply in one short sentence: what is Kotlin Multiplatform?"
private const val RAW_CHUNKS_TO_LOG = 3
private const val RAW_TEXT_LIMIT = 80

private var isRunning = false

internal fun installPromptApiSpike() {
    installPanel()
    log("target: $spikeTarget")
    log("userAgent: ${userAgent()}")
    val hasModel = hasLanguageModel()
    log("hasLanguageModel=$hasModel")
    if (!hasModel) {
        log("availability: Unavailable (LanguageModel undefined)")
        return
    }
    MainScope().launch { logAvailability("availability (load)") }
    bindRunButton {
        if (!isRunning) {
            isRunning = true
            try {
                val createPromise = startCreate()
                MainScope().launch { runSpike(createPromise) }
            } catch (e: Throwable) {
                log("startCreate failed: ${errorText(e)}")
                isRunning = false
            }
        }
    }
}

private suspend fun logAvailability(label: String) {
    try {
        log("$label: ${asString(availability().await())}")
    } catch (e: Throwable) {
        log("$label failed: ${errorText(e)}")
    }
}

private suspend fun runSpike(createPromise: Promise<JsAny?>) {
    try {
        logAvailability("availability (click)")
        val session = toSession(createPromise.await())
        try {
            log("session created, contextWindow=${contextWindow(session)} contextUsage=${contextUsage(session)}")
            streamPrompt(session)
            log("contextUsage after=${contextUsage(session)}")
        } finally {
            destroySession(session)
        }
    } catch (e: Throwable) {
        log("run failed: ${errorText(e)}")
    } finally {
        isRunning = false
    }
}

private fun logRawChunk(
    index: Int,
    chunk: String,
    previous: String?,
    accumulated: String,
) {
    val startsWithPrev = previous?.let { chunk.startsWith(it) } ?: "n/a"
    val startsWithAccumulated = chunk.startsWith(accumulated)
    log("chunk[$index] len=${chunk.length} startsWithPrev=$startsWithPrev startsWithAccumulated=$startsWithAccumulated")
    log("chunk[$index] text=\"${chunk.take(RAW_TEXT_LIMIT)}\"")
}

private suspend fun streamPrompt(session: PromptSession) {
    val startedAt = nowMs()
    val accumulated = StringBuilder()
    var count = 0
    var last: String? = null
    try {
        promptChunks(session).collect { chunk ->
            if (count < RAW_CHUNKS_TO_LOG) logRawChunk(count, chunk, last, accumulated.toString())
            count++
            last = chunk
            accumulated.append(chunk)
            setReply(accumulated.toString())
        }
    } catch (e: Throwable) {
        log("stream failed: ${errorText(e)}")
    }
    log("chunks=$count")
    log("deltas interpretation: $accumulated")
    log("cumulative interpretation (last chunk): ${last.orEmpty()}")
    log("elapsedMs=${(nowMs() - startedAt).toLong()}")
}

private fun promptChunks(session: PromptSession): Flow<String> =
    flow {
        val reader = getReader(promptStreaming(session, SPIKE_PROMPT))
        try {
            while (true) {
                val result = readNext(reader).await()
                if (readDone(result)) break
                emit(readText(result))
            }
        } finally {
            releaseReader(reader)
        }
    }

private fun log(message: String) {
    consoleLog(message)
    appendLog(message)
}

private fun consoleLog(s: String): Unit = js("console.log(s)")

private fun appendLog(s: String): Unit = js("globalThis.spikeLog(s)")

private fun setReply(s: String): Unit = js("globalThis.spikeReply(s)")

private fun nowMs(): Double = js("performance.now()")

private fun userAgent(): String = js("navigator.userAgent")

private fun hasLanguageModel(): Boolean = js("typeof LanguageModel !== 'undefined'")

private fun asString(v: JsAny?): String = js("String(v)")

private fun toSession(v: JsAny?): PromptSession = js("v")

private fun contextWindow(s: PromptSession): Double = js("Number(s.contextWindow)")

private fun contextUsage(s: PromptSession): Double = js("Number(s.contextUsage)")

private fun destroySession(s: PromptSession): Unit = js("s.destroy()")

private fun promptStreaming(
    s: PromptSession,
    text: String,
): JsAny = js("s.promptStreaming(text)")

private fun getReader(stream: JsAny): StreamReader = js("stream.getReader()")

private fun readNext(r: StreamReader): Promise<ReadResult> = js("r.read()")

private fun readDone(r: ReadResult): Boolean = js("r.done === true")

private fun readText(r: ReadResult): String = js("String(r.value)")

private fun releaseReader(r: StreamReader): Unit =
    js(
        "(function () { try { var p = r.cancel(); if (p && p.catch) p.catch(function () {}); } catch (e) {} })()",
    )

private fun availability(): Promise<JsAny?> =
    js(
        """LanguageModel.availability({
        expectedInputs: [{ type: 'text', languages: ['en'] }],
        expectedOutputs: [{ type: 'text', languages: ['en'] }]
    })""",
    )

private fun startCreate(): Promise<JsAny?> =
    js(
        """(function () {
        var p = LanguageModel.create({
            expectedInputs: [{ type: 'text', languages: ['en'] }],
            expectedOutputs: [{ type: 'text', languages: ['en'] }],
            monitor: function (m) {
                m.addEventListener('downloadprogress', function (e) {
                    globalThis.spikeLog('download progress: ' + Math.round(e.loaded * 100) + '%');
                });
            }
        });
        p.catch(function () {});
        return p;
    })()""",
    )

private fun bindRunButton(onClick: () -> Unit): Unit =
    js(
        "document.getElementById('prompt-spike-run').addEventListener('click', function () { onClick(); })",
    )

private fun installPanel(): Unit =
    js(
        """(function () {
        var panel = document.createElement('div');
        panel.style.cssText = 'position:fixed;right:8px;bottom:8px;z-index:2147483647;width:420px;' +
            'max-width:90vw;font-family:monospace;font-size:12px';
        var button = document.createElement('button');
        button.id = 'prompt-spike-run';
        button.textContent = 'Run Prompt API spike';
        var reply = document.createElement('pre');
        reply.id = 'prompt-spike-reply';
        reply.style.cssText = 'margin:4px 0;padding:4px;background:#222;color:#9f9;white-space:pre-wrap';
        var log = document.createElement('pre');
        log.id = 'prompt-spike-log';
        log.style.cssText = 'margin:4px 0;padding:4px;max-height:40vh;overflow:auto;white-space:pre-wrap;' +
            'font-family:monospace;background:#111;color:#eee';
        panel.appendChild(button);
        panel.appendChild(reply);
        panel.appendChild(log);
        document.documentElement.appendChild(panel);
        globalThis.spikeLog = function (s) {
            log.textContent += s + '\n';
            log.scrollTop = log.scrollHeight;
        };
        globalThis.spikeReply = function (s) { reply.textContent = 'reply: ' + s; };
    })()""",
    )
