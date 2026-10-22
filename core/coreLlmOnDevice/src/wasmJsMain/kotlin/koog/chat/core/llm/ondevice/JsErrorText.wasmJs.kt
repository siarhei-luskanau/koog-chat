@file:OptIn(ExperimentalWasmJsInterop::class)

package koog.chat.core.llm.ondevice

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsException

internal actual fun jsErrorText(error: Throwable): String = if (error is JsException) jsValueText(error.thrownValue) else error.toString()
