package koog.chat.core.llm.ondevice

import com.google.mlkit.genai.common.FeatureStatus
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.OnDeviceAvailability
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class OnDeviceLlmAndroidFakeModelHostTest {
    private val model = FakeGeminiNanoModel()

    private fun newLlm() = OnDeviceLlmAndroid(FakeDispatcherSet(), model)

    private suspend fun OnDeviceLlmAndroid.settled(): OnDeviceAvailability =
        withTimeout(TIMEOUT_MS) { availability.first { it != OnDeviceAvailability.Unavailable(CHECKING_REASON) } }

    private suspend fun OnDeviceLlmAndroid.settledAs(expected: OnDeviceAvailability) =
        withTimeout(TIMEOUT_MS) { availability.first { it == expected } }

    @Test
    fun init_shouldBeAvailableAndReadTokenLimit_whenStatusAvailable() =
        runBlocking {
            model.status = { FeatureStatus.AVAILABLE }
            model.tokenLimit = { 1234 }
            val llm = newLlm()

            assertEquals(OnDeviceAvailability.Available, llm.settled())
            assertEquals(1234, llm.maxContextTokens)
        }

    @Test
    fun init_shouldFallBackToDefaultTokenLimit_whenTokenLimitFails() =
        runBlocking {
            model.status = { FeatureStatus.AVAILABLE }
            model.tokenLimit = { error("no limit") }
            val llm = newLlm()

            assertEquals(OnDeviceAvailability.Available, llm.settled())
            assertEquals(4096, llm.maxContextTokens)
        }

    @Test
    fun init_shouldBeDownloadable_whenStatusDownloadable() =
        runBlocking {
            model.status = { FeatureStatus.DOWNLOADABLE }

            assertEquals(OnDeviceAvailability.Downloadable, newLlm().settled())
        }

    @Test
    fun init_shouldBeDownloading_whenStatusDownloading() =
        runBlocking {
            model.status = { FeatureStatus.DOWNLOADING }

            assertEquals(OnDeviceAvailability.Downloading(null), newLlm().settled())
        }

    @Test
    fun init_shouldBeUnavailable_whenStatusUnavailable() =
        runBlocking {
            model.status = { FeatureStatus.UNAVAILABLE }

            assertEquals(OnDeviceAvailability.Unavailable(UNSUPPORTED_REASON), newLlm().settled())
        }

    @Test
    fun init_shouldBeUnavailableWithMessage_whenStatusCheckThrows() =
        runBlocking {
            model.status = { throw IllegalStateException("boom") }

            assertEquals(OnDeviceAvailability.Unavailable("boom"), newLlm().settled())
        }

    @Test
    fun init_shouldBeUnavailableWithDefaultReason_whenStatusCheckThrowsWithoutMessage() =
        runBlocking {
            model.status = { throw IllegalStateException() }

            assertEquals(OnDeviceAvailability.Unavailable(UNSUPPORTED_REASON), newLlm().settled())
        }

    @Test
    fun download_shouldRefreshAvailability_whenCompleted() =
        runBlocking {
            var current = FeatureStatus.DOWNLOADABLE
            model.status = { current }
            model.downloadEvents = {
                current = FeatureStatus.AVAILABLE
                listOf(GeminiNanoDownload.Started(100L), GeminiNanoDownload.Progress(50L), GeminiNanoDownload.Completed)
            }
            val llm = newLlm()
            llm.settledAs(OnDeviceAvailability.Downloadable)

            llm.download()

            assertEquals(OnDeviceAvailability.Available, llm.availability.value)
        }

    @Test
    fun download_shouldStayUnavailable_whenStatusStillUnavailableAfterCompletion() =
        runBlocking {
            model.status = { FeatureStatus.UNAVAILABLE }
            model.downloadEvents = { listOf(GeminiNanoDownload.Started(10L), GeminiNanoDownload.Completed) }
            val llm = newLlm()
            llm.settled()

            llm.download()

            assertEquals(OnDeviceAvailability.Unavailable(UNSUPPORTED_REASON), llm.availability.value)
        }

    @Test
    fun download_shouldPublishProgressFraction_whenProgressReported() =
        runBlocking {
            model.status = { FeatureStatus.DOWNLOADABLE }
            val llm = newLlm()
            llm.settledAs(OnDeviceAvailability.Downloadable)
            val seen = mutableListOf<OnDeviceAvailability>()
            model.status = {
                seen += llm.availability.value
                FeatureStatus.DOWNLOADABLE
            }
            model.downloadEvents = { listOf(GeminiNanoDownload.Started(200L), GeminiNanoDownload.Progress(50L)) }

            llm.download()

            assertEquals(OnDeviceAvailability.Downloading(0.25f), seen.last())
        }

    @Test
    fun download_shouldPublishNullProgress_whenTotalUnknown() =
        runBlocking {
            model.status = { FeatureStatus.DOWNLOADABLE }
            val llm = newLlm()
            llm.settledAs(OnDeviceAvailability.Downloadable)
            val seen = mutableListOf<OnDeviceAvailability>()
            model.status = {
                seen += llm.availability.value
                FeatureStatus.DOWNLOADABLE
            }
            model.downloadEvents = { listOf(GeminiNanoDownload.Progress(50L)) }

            llm.download()

            assertEquals(OnDeviceAvailability.Downloading(null), seen.last())
        }

    @Test
    fun download_shouldRefreshAndRethrow_whenDownloadFailed() =
        runBlocking {
            model.status = { FeatureStatus.DOWNLOADABLE }
            val failure = IllegalStateException("network")
            model.downloadEvents = { listOf(GeminiNanoDownload.Started(10L), GeminiNanoDownload.Failed(failure)) }
            val llm = newLlm()
            llm.settledAs(OnDeviceAvailability.Downloadable)
            val callsBefore = model.statusCalls

            val thrown = assertFailsWith<IllegalStateException> { llm.download() }

            assertSame(failure, thrown)
            assertEquals(callsBefore + 1, model.statusCalls)
            assertEquals(OnDeviceAvailability.Downloadable, llm.availability.value)
        }

    @Test
    fun download_shouldRefreshAndRethrow_whenFlowThrows() =
        runBlocking {
            model.status = { FeatureStatus.DOWNLOADABLE }
            model.downloadError = IllegalStateException("flow")
            val llm = newLlm()
            llm.settledAs(OnDeviceAvailability.Downloadable)

            val thrown = assertFailsWith<IllegalStateException> { llm.download() }

            assertEquals("flow", thrown.message)
            assertEquals(OnDeviceAvailability.Downloadable, llm.availability.value)
        }

    @Test
    fun stream_shouldEmitTextChunksThenEnd_whenModelGeneratesText() =
        runBlocking {
            model.generated = { listOf("Hel", "", "lo") }

            val chunks = newLlm().stream(emptyList(), "hi").toList()

            assertEquals(listOf(LlmChunk.Text("Hel"), LlmChunk.Text("lo"), LlmChunk.End(null)), chunks)
            assertEquals("hi", model.lastPrompt)
        }

    @Test
    fun stream_shouldEmitOnlyEnd_whenAllTextsEmpty() =
        runBlocking {
            model.generated = { listOf("", "") }

            val chunks = newLlm().stream(emptyList(), "hi").toList()

            assertEquals(listOf<LlmChunk>(LlmChunk.End(null)), chunks)
        }

    @Test
    fun stream_shouldPassFormattedTranscript_whenHistoryPresent() =
        runBlocking {
            val history =
                listOf(
                    chatEntry(ChatEntryType.USER_PROMPT, "q"),
                    chatEntry(ChatEntryType.SUCCESS_RESPONSE, "a"),
                )

            newLlm().stream(history, "next").toList()

            assertEquals(formatTranscript(history, "next"), model.lastPrompt)
        }

    @Test
    fun stream_shouldPropagateException_whenModelFails() =
        runBlocking {
            model.generated = { listOf("partial") }
            model.generateError = IllegalStateException("gen")
            val chunks = mutableListOf<LlmChunk>()

            val thrown =
                assertFailsWith<IllegalStateException> {
                    newLlm().stream(emptyList(), "hi").collect { chunks += it }
                }

            assertEquals("gen", thrown.message)
            assertEquals(listOf<LlmChunk>(LlmChunk.Text("partial")), chunks)
        }

    private companion object {
        const val TIMEOUT_MS = 30_000L
    }
}
