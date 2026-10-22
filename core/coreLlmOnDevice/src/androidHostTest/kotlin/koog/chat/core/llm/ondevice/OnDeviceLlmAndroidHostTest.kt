package koog.chat.core.llm.ondevice

import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.llm.OnDeviceAvailability
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
internal class OnDeviceLlmAndroidHostTest {
    private val noHistory = emptyList<ChatEntry>()

    @Test
    fun init_shouldSettleToUnavailable_whenMlKitAbsent() =
        runBlockingTest {
            val llm = OnDeviceLlmAndroid(FakeDispatcherSet(), MlKitGeminiNanoModel())

            val settled = awaitSettled(llm)

            assertIs<OnDeviceAvailability.Unavailable>(settled)
            assertEquals(4096, llm.maxContextTokens)
        }

    @Test
    fun download_shouldFailAndKeepUnavailable_whenMlKitAbsent() =
        runBlockingTest {
            val llm = OnDeviceLlmAndroid(FakeDispatcherSet(), MlKitGeminiNanoModel())
            awaitSettled(llm)

            assertFailsWith<Exception> { llm.download() }

            assertIs<OnDeviceAvailability.Unavailable>(llm.availability.value)
        }

    @Test
    fun stream_shouldFail_whenMlKitAbsent() =
        runBlockingTest {
            val llm = OnDeviceLlmAndroid(FakeDispatcherSet(), MlKitGeminiNanoModel())

            assertFailsWith<Exception> { llm.stream(noHistory, "hi").toList() }
        }

    private suspend fun awaitSettled(llm: OnDeviceLlmAndroid): OnDeviceAvailability =
        withTimeout(SETTLE_TIMEOUT_MS) {
            llm.availability.first { it != OnDeviceAvailability.Unavailable(CHECKING_REASON) }
        }

    private fun runBlockingTest(block: suspend () -> Unit) = runBlocking { block() }

    private companion object {
        const val SETTLE_TIMEOUT_MS = 30_000L
    }
}
