internal actual val spikeTarget: String = "js"

internal actual fun errorText(e: Throwable): String = "${e::class.simpleName}: ${e.message}"
