package koog.chat.core.llm.ondevice

internal class SnapshotDeltas {
    private var previous: String = ""

    fun next(snapshot: String): String? {
        if (snapshot == previous) return null
        check(snapshot.length > previous.length && snapshot.startsWith(previous)) {
            "On-device model rewrote its earlier output"
        }
        val delta = snapshot.substring(previous.length)
        previous = snapshot
        return delta
    }
}
