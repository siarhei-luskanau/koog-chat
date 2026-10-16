@file:OptIn(ExperimentalWasmJsInterop::class)

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsException

internal actual val spikeTarget: String = "wasmJs"

internal actual fun errorText(e: Throwable): String =
    if (e is JsException) jsString(e.thrownValue) else "${e::class.simpleName}: ${e.message}"

private fun jsString(v: JsAny?): String = js("String(v)")
