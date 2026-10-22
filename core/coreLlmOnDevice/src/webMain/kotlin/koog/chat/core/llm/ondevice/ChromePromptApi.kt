@file:OptIn(ExperimentalWasmJsInterop::class)

package koog.chat.core.llm.ondevice

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.Promise

internal external interface LanguageModelOptions : JsAny

internal external interface InitialPrompts : JsAny

internal external interface LanguageModelSession : JsAny

internal external interface PromptStreamReader : JsAny

internal external interface PromptReadResult : JsAny

internal external interface AbortHandle : JsAny

internal fun isLanguageModelDefined(): Boolean = js("typeof LanguageModel !== 'undefined'")

internal fun englishTextOptions(): LanguageModelOptions =
    js(
        """({
        expectedInputs: [{ type: 'text', languages: ['en'] }],
        expectedOutputs: [{ type: 'text', languages: ['en'] }]
    })""",
    )

internal fun queryAvailability(options: LanguageModelOptions): Promise<JsAny?> = js("LanguageModel.availability(options)")

internal fun jsValueText(value: JsAny?): String = js("String(value)")

internal fun emptyInitialPrompts(): InitialPrompts = js("[]")

internal fun appendInitialPrompt(
    prompts: InitialPrompts,
    role: String,
    content: String,
): Unit = js("(function () { prompts.push({ role: role, content: content }); })()")

internal fun newAbortHandle(): AbortHandle = js("new AbortController()")

internal fun abortHandle(handle: AbortHandle): Unit = js("handle.abort()")

internal fun createSession(
    options: LanguageModelOptions,
    initialPrompts: InitialPrompts,
    handle: AbortHandle,
): Promise<LanguageModelSession> =
    js("LanguageModel.create(Object.assign({}, options, { initialPrompts: initialPrompts, signal: handle.signal }))")

internal fun destroyWhenCreated(creation: Promise<LanguageModelSession>): Unit =
    js("(function () { creation.then(function (s) { try { s.destroy(); } catch (e) {} }, function () {}); })()")

internal fun createSessionWithDownloadMonitor(
    options: LanguageModelOptions,
    onProgress: (Double) -> Unit,
): Promise<LanguageModelSession> =
    js(
        """(function () {
        var p = LanguageModel.create(Object.assign({}, options, {
            monitor: function (m) {
                m.addEventListener('downloadprogress', function (e) { onProgress(Number(e.loaded)); });
            }
        }));
        p.catch(function () {});
        return p;
    })()""",
    )

internal fun sessionContextWindow(session: LanguageModelSession): Double = js("Number(session.contextWindow)")

internal fun destroySession(session: LanguageModelSession): Unit = js("(function () { try { session.destroy(); } catch (e) {} })()")

internal fun startPromptStreaming(
    session: LanguageModelSession,
    text: String,
    handle: AbortHandle,
): PromptStreamReader = js("session.promptStreaming(text, { signal: handle.signal }).getReader()")

internal fun readNext(reader: PromptStreamReader): Promise<PromptReadResult> = js("reader.read()")

internal fun isReadDone(result: PromptReadResult): Boolean = js("result.done === true")

internal fun readChunkText(result: PromptReadResult): String = js("String(result.value)")

internal fun cancelReader(reader: PromptStreamReader): Unit =
    js(
        "(function () { try { var p = reader.cancel(); if (p && p.catch) p.catch(function () {}); } catch (e) {} })()",
    )
