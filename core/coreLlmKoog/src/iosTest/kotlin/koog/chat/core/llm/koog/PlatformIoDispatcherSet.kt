package koog.chat.core.llm.koog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual fun platformIoDispatcherSet() = FakeDispatcherSet(io = Dispatchers.IO)
