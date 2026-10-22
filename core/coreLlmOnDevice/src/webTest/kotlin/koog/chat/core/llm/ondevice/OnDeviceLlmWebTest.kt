package koog.chat.core.llm.ondevice

import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.OnDeviceAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

internal class OnDeviceLlmWebTest {
    private val checking = OnDeviceAvailability.Unavailable(CHECKING_REASON)

    @BeforeTest
    fun setUp() = installFakeLanguageModel()

    @AfterTest
    fun tearDown() = uninstallFakeLanguageModel()

    private fun webTest(block: suspend CoroutineScope.() -> Unit): TestResult =
        runTest { withContext(Dispatchers.Default) { withTimeout(10.seconds) { block() } } }

    private suspend fun settledLlm(): OnDeviceLlmWeb {
        val llm = OnDeviceLlmWeb(FakeDispatcherSet())
        llm.availability.first { it != checking }
        return llm
    }

    private suspend fun eventually(condition: () -> Boolean) {
        while (!condition()) delay(5)
    }

    private suspend fun assertSettlesTo(
        chromeValue: String,
        expected: OnDeviceAvailability,
    ) {
        fakeSetAvailability(chromeValue)

        val llm = settledLlm()

        assertEquals(expected, llm.availability.value)
    }

    @Test
    fun availability_shouldStartAsChecking_whenConstructed() =
        webTest {
            val llm = OnDeviceLlmWeb(FakeDispatcherSet())

            assertEquals(checking, llm.availability.value)
            llm.availability.first { it != checking }
        }

    @Test
    fun availability_shouldBeAvailable_whenChromeReportsAvailable() =
        webTest { assertSettlesTo("available", OnDeviceAvailability.Available) }

    @Test
    fun availability_shouldBeDownloadable_whenChromeReportsDownloadable() =
        webTest { assertSettlesTo("downloadable", OnDeviceAvailability.Downloadable) }

    @Test
    fun availability_shouldBeDownloadingWithoutProgress_whenChromeReportsDownloading() =
        webTest { assertSettlesTo("downloading", OnDeviceAvailability.Downloading(progress = null)) }

    @Test
    fun availability_shouldBeUnavailableWithValue_whenChromeReportsUnavailable() =
        webTest {
            assertSettlesTo("unavailable", OnDeviceAvailability.Unavailable("Chrome built-in AI is unavailable: unavailable"))
        }

    @Test
    fun availability_shouldBeMissingReason_whenLanguageModelUndefined() =
        webTest {
            removeLanguageModel()

            val llm = settledLlm()

            assertEquals(OnDeviceAvailability.Unavailable(CHROME_AI_MISSING_REASON), llm.availability.value)
        }

    @Test
    fun availability_shouldBeUnavailableWithErrorText_whenAvailabilityRejects() =
        webTest {
            fakeFailAvailability("availability exploded")

            val llm = settledLlm()

            val state = llm.availability.value
            assertTrue(state is OnDeviceAvailability.Unavailable)
            assertContains(state.reason, "availability exploded")
        }

    @Test
    fun availability_shouldQueryWithSameEnglishTextOptionsAsCreate_whenStreaming() =
        webTest {
            val llm = settledLlm()

            llm.stream(emptyList(), "hi").toList()

            val availabilityInputs = fakeAvailabilityInputs()
            assertEquals(fakeCreateInputs(), availabilityInputs)
            assertEquals(
                """[[{"type":"text","languages":["en"]}],[{"type":"text","languages":["en"]}]]""",
                availabilityInputs,
            )
        }

    @Test
    fun refreshAvailability_shouldReReadBrowserState_whenStateChangedSinceConstruction() =
        webTest {
            fakeSetAvailability("downloading")
            val llm = settledLlm()
            assertEquals(OnDeviceAvailability.Downloading(progress = null), llm.availability.value)
            fakeSetAvailability("available")

            llm.refreshAvailability()

            assertEquals(OnDeviceAvailability.Available, llm.availability.value)
            assertEquals(2, fakeAvailabilityCalls())
        }

    @Test
    fun stream_shouldPassDeltasThroughInOrderAndEndLast_whenChunksScripted() =
        webTest {
            listOf(" Multi", "", "platform", "", "\n\n").forEach(::fakeAddChunk)
            val llm = settledLlm()

            val chunks = llm.stream(emptyList(), "hi").toList()

            assertEquals(
                listOf(
                    LlmChunk.Text(" Multi"),
                    LlmChunk.Text("platform"),
                    LlmChunk.Text("\n\n"),
                    LlmChunk.End(tokensUsed = null),
                ),
                chunks,
            )
            assertEquals("hi", fakePromptText())
            assertEquals(0, fakeReaderCancelCount())
            assertEquals(1, fakeDestroyCount())
        }

    @Test
    fun stream_shouldEmitOnlyEnd_whenStreamHasNoChunks() =
        webTest {
            val llm = settledLlm()

            val chunks = llm.stream(emptyList(), "hi").toList()

            assertEquals(listOf<LlmChunk>(LlmChunk.End(tokensUsed = null)), chunks)
            assertEquals(1, fakeDestroyCount())
        }

    @Test
    fun stream_shouldMapHistoryToInitialPromptsSkippingThinkingAndErrors_whenHistoryMixed() =
        webTest {
            val history =
                listOf(
                    chatEntry(ChatEntryType.USER_PROMPT, "q1"),
                    chatEntry(ChatEntryType.THINKING, "pondering"),
                    chatEntry(ChatEntryType.SUCCESS_RESPONSE, "a1"),
                    chatEntry(ChatEntryType.USER_PROMPT, "q2"),
                    chatEntry(ChatEntryType.ERROR_RESPONSE, "failed"),
                )
            val llm = settledLlm()

            llm.stream(history, "q3").toList()

            assertEquals(
                """[{"role":"user","content":"q1"},{"role":"assistant","content":"a1"},{"role":"user","content":"q2"}]""",
                fakeCreateInitialPrompts(),
            )
            assertEquals("q3", fakePromptText())
        }

    @Test
    fun stream_shouldSendEmptyInitialPrompts_whenHistoryEmpty() =
        webTest {
            val llm = settledLlm()

            llm.stream(emptyList(), "hi").toList()

            assertEquals("[]", fakeCreateInitialPrompts())
        }

    @Test
    fun maxContextTokens_shouldBeDefault_whenNoSessionCreated() =
        webTest {
            val llm = settledLlm()

            assertEquals(DEFAULT_MAX_CONTEXT_TOKENS, llm.maxContextTokens)
        }

    @Test
    fun maxContextTokens_shouldUseSessionContextWindow_whenSessionCreated() =
        webTest {
            fakeSetContextWindow(9216.0)
            val llm = settledLlm()

            llm.stream(emptyList(), "hi").toList()

            assertEquals(9216, llm.maxContextTokens)
        }

    @Test
    fun maxContextTokens_shouldKeepDefault_whenSessionContextWindowNotPositive() =
        webTest {
            fakeSetContextWindow(0.0)
            val llm = settledLlm()

            llm.stream(emptyList(), "hi").toList()

            assertEquals(DEFAULT_MAX_CONTEXT_TOKENS, llm.maxContextTokens)
        }

    @Test
    fun stream_shouldCancelReaderAndDestroySession_whenCollectorTakesOneFromEndlessStream() =
        webTest {
            fakeAddChunk("first")
            fakeSetStreamMode("hang")
            val llm = settledLlm()

            val chunks = llm.stream(emptyList(), "hi").take(1).toList()

            assertEquals(listOf<LlmChunk>(LlmChunk.Text("first")), chunks)
            eventually { fakeReaderCancelCount() == 1 }
            assertEquals(1, fakeDestroyCount())
            assertTrue(fakePromptSignalAborted())
        }

    @Test
    fun stream_shouldCancelReaderAbortAndDestroySession_whenCollectorCancelledWhileWaitingForChunk() =
        webTest {
            fakeSetStreamMode("hang")
            val llm = settledLlm()
            val job = launch { llm.stream(emptyList(), "hi").toList() }
            eventually { fakePromptCalls() == 1 }

            job.cancelAndJoin()

            eventually { fakeReaderCancelCount() == 1 }
            assertEquals(1, fakeDestroyCount())
            assertTrue(fakePromptSignalAborted())
            assertTrue(fakeCreateSignalAborted())
        }

    @Test
    fun stream_shouldAbortCreateAndDestroyLateSession_whenCancelledDuringPendingCreate() =
        webTest {
            fakeSetCreateMode("pending")
            val llm = settledLlm()
            val job = launch { llm.stream(emptyList(), "hi").toList() }
            eventually { fakeCreateCalls() == 1 }

            job.cancelAndJoin()

            assertTrue(fakeCreateSignalAborted())
            assertEquals(0, fakeDestroyCount())
            fakeResolvePendingCreate()
            eventually { fakeDestroyCount() == 1 }
            assertEquals(0, fakePromptCalls())
        }

    @Test
    fun stream_shouldThrowChromePromptApiException_whenStreamErrorsMidReply() =
        webTest {
            fakeAddChunk("partial")
            fakeSetStreamMode("error")
            fakeSetStreamError("stream boom")
            val llm = settledLlm()
            val received = mutableListOf<LlmChunk>()

            val error =
                assertFailsWith<ChromePromptApiException> {
                    llm.stream(emptyList(), "hi").collect { received += it }
                }

            assertContains(error.message.orEmpty(), "stream boom")
            assertEquals(listOf<LlmChunk>(LlmChunk.Text("partial")), received)
            assertEquals(1, fakeDestroyCount())
        }

    @Test
    fun stream_shouldThrowChromePromptApiException_whenCreateRejects() =
        webTest {
            fakeSetCreateMode("reject")
            val llm = settledLlm()

            val error = assertFailsWith<ChromePromptApiException> { llm.stream(emptyList(), "hi").toList() }

            assertContains(error.message.orEmpty(), "create failed")
            assertEquals(0, fakePromptCalls())
            assertEquals(0, fakeDestroyCount())
        }

    @Test
    fun download_shouldCreateWithinGestureReportProgressDestroySessionAndRefresh_whenDownloadable() =
        webTest {
            fakeSetAvailability("downloadable")
            fakeRequireGesture()
            fakeSetCreateMode("pending")
            fakeSetContextWindow(6144.0)
            val llm = settledLlm()

            fakeSetUserActivation(true)
            val job = launch(start = CoroutineStart.UNDISPATCHED) { llm.download() }
            fakeSetUserActivation(false)

            assertEquals(1, fakeCreateCalls())
            assertTrue(fakeCreatedDuringGesture())
            assertTrue(fakeCreateHadMonitor())
            assertEquals(OnDeviceAvailability.Downloading(progress = null), llm.availability.value)
            fakeEmitDownloadProgress(0.25)
            assertEquals(OnDeviceAvailability.Downloading(progress = 0.25f), llm.availability.value)
            fakeSetAvailability("available")
            fakeResolvePendingCreate()
            job.join()
            assertEquals(1, fakeDestroyCount())
            assertEquals(OnDeviceAvailability.Available, llm.availability.value)
            assertEquals(2, fakeAvailabilityCalls())
            assertEquals(6144, llm.maxContextTokens)
        }

    @Test
    fun download_shouldThrowChromePromptApiExceptionAndRefresh_whenCreateRejectedWithoutGesture() =
        webTest {
            fakeSetAvailability("downloadable")
            fakeRequireGesture()
            val llm = settledLlm()

            val error = assertFailsWith<ChromePromptApiException> { llm.download() }

            assertContains(error.message.orEmpty(), "user gesture")
            assertEquals(OnDeviceAvailability.Downloadable, llm.availability.value)
            assertEquals(2, fakeAvailabilityCalls())
            assertEquals(0, fakeDestroyCount())
        }

    @Test
    fun download_shouldRefreshInsteadOfStayingDownloading_whenCancelled() =
        webTest {
            fakeSetAvailability("downloadable")
            fakeSetCreateMode("pending")
            val llm = settledLlm()
            val job = launch(start = CoroutineStart.UNDISPATCHED) { llm.download() }
            assertEquals(OnDeviceAvailability.Downloading(progress = null), llm.availability.value)
            fakeSetAvailability("downloadable")

            job.cancelAndJoin()

            assertEquals(OnDeviceAvailability.Downloadable, llm.availability.value)
            assertEquals(2, fakeAvailabilityCalls())
            fakeResolvePendingCreate()
            eventually { fakeDestroyCount() == 1 }
        }

    @Test
    fun download_shouldNotCreateSession_whenNotDownloadable() =
        webTest {
            val llm = settledLlm()

            llm.download()

            assertEquals(0, fakeCreateCalls())
            assertEquals(OnDeviceAvailability.Available, llm.availability.value)
            assertEquals(1, fakeAvailabilityCalls())
        }
}
