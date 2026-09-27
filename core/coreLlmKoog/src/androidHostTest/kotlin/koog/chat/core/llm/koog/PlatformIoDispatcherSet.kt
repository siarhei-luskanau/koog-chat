package koog.chat.core.llm.koog

import kotlinx.coroutines.Dispatchers

internal actual fun platformIoDispatcherSet() = FakeDispatcherSet(io = Dispatchers.IO)
