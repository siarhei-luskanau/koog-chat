package koog.chat.core.llm.ondevice

import koog.chat.core.llm.OnDeviceAvailability
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class OnDeviceLlmJvmTest {
    private val llm = OnDeviceLlmJvm()

    @Test
    fun availability_shouldBeUnavailable_whenOnDesktop() {
        assertIs<OnDeviceAvailability.Unavailable>(llm.availability.value)
    }

    @Test
    fun maxContextTokens_shouldBeDefault_whenOnDesktop() {
        assertEquals(4096, llm.maxContextTokens)
    }

    @Test
    fun download_shouldKeepAvailability_whenCalled() =
        runTest {
            val before = llm.availability.value

            llm.download()

            assertEquals(before, llm.availability.value)
        }

    @Test
    fun stream_shouldThrowIllegalState_whenCollected() =
        runTest {
            assertFailsWith<IllegalStateException> { llm.stream(emptyList(), "hi").toList() }
        }
}
