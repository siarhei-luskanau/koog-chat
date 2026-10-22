package koog.chat.core.llm.ondevice

import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.OnDeviceAvailability
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeCancellable : AppleFoundationModelsCancellable {
    var isCancelled = false

    override fun cancel() {
        isCancelled = true
    }
}

private class FakeBridge(
    var status: String = "available",
    private val script: (AppleFoundationModelsListener) -> Unit = {},
) : AppleFoundationModelsBridge {
    val cancellable = FakeCancellable()
    val prompts = mutableListOf<String>()

    override fun availability(): String = status

    override fun stream(
        prompt: String,
        callback: AppleFoundationModelsListener,
    ): AppleFoundationModelsCancellable {
        prompts += prompt
        script(callback)
        return cancellable
    }
}

internal class OnDeviceLlmIosTest {
    @BeforeTest
    fun setUp() {
        AppleFoundationModelsBridgeRegistry.bridge = null
    }

    @AfterTest
    fun tearDown() {
        AppleFoundationModelsBridgeRegistry.bridge = null
    }

    @Test
    fun availability_shouldBeUnavailable_whenNoBridgeRegistered() {
        val llm = OnDeviceLlmIos()

        assertTrue(llm.availability.value is OnDeviceAvailability.Unavailable)
        assertEquals(4096, llm.maxContextTokens)
    }

    @Test
    fun availability_shouldBeAvailable_whenBridgeReportsAvailable() {
        AppleFoundationModelsBridgeRegistry.bridge = FakeBridge(status = "available")

        assertEquals(OnDeviceAvailability.Available, OnDeviceLlmIos().availability.value)
    }

    @Test
    fun availability_shouldCarryBridgeStatusAsReason_whenBridgeReportsOtherStatus() {
        AppleFoundationModelsBridgeRegistry.bridge = FakeBridge(status = "deviceNotEligible")

        assertEquals(OnDeviceAvailability.Unavailable("deviceNotEligible"), OnDeviceLlmIos().availability.value)
    }

    @Test
    fun download_shouldKeepAvailability_whenCalled() =
        runTest {
            val llm = OnDeviceLlmIos()
            val before = llm.availability.value

            llm.download()

            assertEquals(before, llm.availability.value)
        }

    @Test
    fun stream_shouldThrowIllegalState_whenNoBridgeRegistered() =
        runTest {
            val llm = OnDeviceLlmIos()

            assertFailsWith<IllegalStateException> { llm.stream(emptyList(), "hi").toList() }
        }

    @Test
    fun refreshAvailability_shouldBecomeAvailable_whenBridgeRegisteredAfterConstruction() =
        runTest {
            val llm = OnDeviceLlmIos()
            assertTrue(llm.availability.value is OnDeviceAvailability.Unavailable)
            AppleFoundationModelsBridgeRegistry.bridge = FakeBridge()

            llm.refreshAvailability()

            assertEquals(OnDeviceAvailability.Available, llm.availability.value)
        }

    @Test
    fun refreshAvailability_shouldReflectNewStatus_whenBridgeStatusChanges() =
        runTest {
            val bridge = FakeBridge(status = "available")
            AppleFoundationModelsBridgeRegistry.bridge = bridge
            val llm = OnDeviceLlmIos()

            bridge.status = "modelNotReady"
            llm.refreshAvailability()

            assertEquals(OnDeviceAvailability.Unavailable("modelNotReady"), llm.availability.value)
        }

    @Test
    fun stream_shouldNotLoseText_whenBridgePushesManySnapshotsBeforeCollectorResumes() =
        runTest {
            val finalText = "x".repeat(200)
            AppleFoundationModelsBridgeRegistry.bridge =
                FakeBridge { listener ->
                    for (i in 1..200) listener.onSnapshot("x".repeat(i))
                    listener.onComplete(null)
                }

            val chunks = OnDeviceLlmIos().stream(emptyList(), "hi").toList()

            assertEquals(LlmChunk.End(null), chunks.last())
            assertEquals(finalText, chunks.filterIsInstance<LlmChunk.Text>().joinToString("") { it.text })
        }

    @Test
    fun stream_shouldFail_whenSnapshotRewritesEarlierOutput() =
        runTest {
            AppleFoundationModelsBridgeRegistry.bridge =
                FakeBridge { listener ->
                    listener.onSnapshot("Hello")
                    listener.onSnapshot("Goodbye")
                    listener.onComplete(null)
                }

            val error =
                assertFailsWith<IllegalStateException> {
                    OnDeviceLlmIos().stream(emptyList(), "hi").toList()
                }

            assertEquals("On-device model rewrote its earlier output", error.message)
        }

    @Test
    fun stream_shouldEmitDeltasThenEnd_whenSnapshotsAreCumulative() =
        runTest {
            val bridge =
                FakeBridge { listener ->
                    listener.onSnapshot("Hel")
                    listener.onSnapshot("Hello")
                    listener.onSnapshot("Hello world")
                    listener.onComplete(null)
                }
            AppleFoundationModelsBridgeRegistry.bridge = bridge

            val chunks = OnDeviceLlmIos().stream(emptyList(), "hi").toList()

            assertEquals(
                listOf(LlmChunk.Text("Hel"), LlmChunk.Text("lo"), LlmChunk.Text(" world"), LlmChunk.End(null)),
                chunks,
            )
            assertEquals(listOf("hi"), bridge.prompts)
        }

    @Test
    fun stream_shouldSkipDuplicateSnapshots_whenSnapshotRepeated() =
        runTest {
            AppleFoundationModelsBridgeRegistry.bridge =
                FakeBridge { listener ->
                    listener.onSnapshot("Hi")
                    listener.onSnapshot("Hi")
                    listener.onComplete(null)
                }

            val chunks = OnDeviceLlmIos().stream(emptyList(), "hi").toList()

            assertEquals(listOf(LlmChunk.Text("Hi"), LlmChunk.End(null)), chunks)
        }

    @Test
    fun stream_shouldFormatTranscript_whenHistoryPresent() =
        runTest {
            val bridge = FakeBridge { it.onComplete(null) }
            AppleFoundationModelsBridgeRegistry.bridge = bridge
            val history = listOf(chatEntry(koog.chat.core.database.api.entity.ChatEntryType.USER_PROMPT, "earlier"))

            OnDeviceLlmIos().stream(history, "now").toList()

            assertEquals(listOf("User: earlier\n\nUser: now\n\nAssistant:"), bridge.prompts)
        }

    @Test
    fun stream_shouldFailWithMessage_whenCompleteReportsFailure() =
        runTest {
            AppleFoundationModelsBridgeRegistry.bridge =
                FakeBridge { listener ->
                    listener.onSnapshot("Partial")
                    listener.onComplete("model exploded")
                }

            val error =
                assertFailsWith<Exception> {
                    OnDeviceLlmIos().stream(emptyList(), "hi").toList()
                }

            assertEquals("model exploded", error.message)
        }

    @Test
    fun stream_shouldCancelHandle_whenCollectorCancelsEarly() =
        runTest {
            val bridge =
                FakeBridge { listener ->
                    listener.onSnapshot("A")
                    listener.onSnapshot("AB")
                }
            AppleFoundationModelsBridgeRegistry.bridge = bridge

            val chunks = OnDeviceLlmIos().stream(emptyList(), "hi").take(1).toList()

            assertEquals(listOf(LlmChunk.Text("A")), chunks)
            assertTrue(bridge.cancellable.isCancelled)
        }

    @Test
    fun stream_shouldCancelHandle_whenStreamCompletesNormally() =
        runTest {
            val bridge = FakeBridge { it.onComplete(null) }
            AppleFoundationModelsBridgeRegistry.bridge = bridge

            OnDeviceLlmIos().stream(emptyList(), "hi").toList()

            assertTrue(bridge.cancellable.isCancelled)
        }

    @Test
    fun cancellable_shouldNotBeCancelled_whenStreamNeverCollected() {
        val bridge = FakeBridge()
        AppleFoundationModelsBridgeRegistry.bridge = bridge

        OnDeviceLlmIos().stream(emptyList(), "hi")

        assertFalse(bridge.cancellable.isCancelled)
    }
}
