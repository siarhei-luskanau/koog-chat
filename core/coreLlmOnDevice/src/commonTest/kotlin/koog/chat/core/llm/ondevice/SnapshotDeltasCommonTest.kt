package koog.chat.core.llm.ondevice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SnapshotDeltasCommonTest {
    @Test
    fun next_shouldReturnWholeText_whenFirstSnapshot() {
        val deltas = SnapshotDeltas()

        assertEquals("Hello", deltas.next("Hello"))
    }

    @Test
    fun next_shouldReturnSuffixes_whenSnapshotsGrow() {
        val deltas = SnapshotDeltas()

        assertEquals("Hel", deltas.next("Hel"))
        assertEquals("lo", deltas.next("Hello"))
        assertEquals(" world", deltas.next("Hello world"))
    }

    @Test
    fun next_shouldReturnNull_whenSnapshotIdentical() {
        val deltas = SnapshotDeltas()
        deltas.next("Hello")

        assertNull(deltas.next("Hello"))
    }

    @Test
    fun next_shouldReturnNull_whenEmptySnapshotFirst() {
        assertNull(SnapshotDeltas().next(""))
    }

    @Test
    fun next_shouldThrow_whenSnapshotDoesNotExtendPrevious() {
        val deltas = SnapshotDeltas()
        deltas.next("Hello")

        val error = assertFailsWith<IllegalStateException> { deltas.next("Goodbye world") }

        assertEquals("On-device model rewrote its earlier output", error.message)
    }

    @Test
    fun next_shouldThrow_whenSnapshotShorterThanPrevious() {
        val deltas = SnapshotDeltas()
        deltas.next("Hello")

        assertFailsWith<IllegalStateException> { deltas.next("Hel") }
    }
}
