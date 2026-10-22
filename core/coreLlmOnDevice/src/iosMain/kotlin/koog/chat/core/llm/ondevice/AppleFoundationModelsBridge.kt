package koog.chat.core.llm.ondevice

interface AppleFoundationModelsListener {
    fun onSnapshot(text: String)

    fun onComplete(failure: String?)
}

interface AppleFoundationModelsCancellable {
    fun cancel()
}

interface AppleFoundationModelsBridge {
    fun availability(): String

    fun stream(
        prompt: String,
        callback: AppleFoundationModelsListener,
    ): AppleFoundationModelsCancellable
}

object AppleFoundationModelsBridgeRegistry {
    var bridge: AppleFoundationModelsBridge? = null
}
