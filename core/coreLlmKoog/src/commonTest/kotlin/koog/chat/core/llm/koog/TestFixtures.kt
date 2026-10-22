package koog.chat.core.llm.koog

import ai.koog.http.client.KoogHttpClient
import ai.koog.http.client.ktor.KtorKoogHttpClient
import androidx.paging.PagingSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineCapability
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.plugins.sse.DefaultClientSSESession
import io.ktor.client.plugins.sse.SSECapability
import io.ktor.client.plugins.sse.SSEClientContent
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.isSseRequest
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.database.api.repository.ChatEntryRepository
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.OnDeviceAvailability
import koog.chat.core.llm.OnDeviceLlm
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

internal class FakeDispatcherSet(
    private val default: CoroutineDispatcher = Dispatchers.Default,
    private val io: CoroutineDispatcher = Dispatchers.Default,
) : DispatcherSet {
    override fun defaultDispatcher() = default

    override fun ioDispatcher() = io

    override fun mainDispatcher() = default
}

// Mirrors production DispatcherSet.ioDispatcher(): Koog's streaming flowOn must match it (docs/DECISIONS.md)
internal expect fun platformIoDispatcherSet(): FakeDispatcherSet

internal class FakeOnDeviceLlm(
    availability: OnDeviceAvailability = OnDeviceAvailability.Available,
    override val maxContextTokens: Int = 4096,
    private val chunks: List<LlmChunk> = listOf(LlmChunk.Text("Hello"), LlmChunk.End(tokensUsed = null)),
    private val failure: Throwable? = null,
) : OnDeviceLlm {
    val streamCalls = mutableListOf<Pair<List<ChatEntry>, String>>()

    override val availability: StateFlow<OnDeviceAvailability> = MutableStateFlow(availability)

    override suspend fun refreshAvailability() = Unit

    override suspend fun download() = Unit

    override fun stream(
        history: List<ChatEntry>,
        prompt: String,
    ): Flow<LlmChunk> {
        streamCalls += history to prompt
        return flow {
            chunks.forEach { emit(it) }
            failure?.let { throw it }
        }
    }
}

internal class FakeChatEntryRepository(
    initialEntries: List<ChatEntry> = emptyList(),
) : ChatEntryRepository {
    val entries = initialEntries.toMutableList()
    val updates = mutableListOf<ChatEntry>()
    var failOnGetAll: Exception? = null

    override fun pagingSource(chatId: String): PagingSource<Int, ChatEntry> = throw UnsupportedOperationException()

    override suspend fun getAll(chatId: String): List<ChatEntry> {
        failOnGetAll?.let { throw it }
        return entries.filter { it.chatId == chatId }
    }

    override suspend fun save(entry: ChatEntry) {
        entries += entry
    }

    override suspend fun update(entry: ChatEntry) {
        updates += entry
        val index = entries.indexOfFirst { it.id == entry.id }
        if (index >= 0) entries[index] = entry
    }
}

internal class RecordingMockEngine(
    private val handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
) {
    val requests = mutableListOf<HttpRequestData>()

    val httpClientFactory: KoogHttpClient.Factory =
        KtorKoogHttpClient.Factory(
            HttpClient(
                SseMockEngine(
                    MockEngine { request ->
                        requests += request
                        handler(request)
                    },
                ),
            ),
        )
}

@OptIn(InternalAPI::class)
private class SseMockEngine(
    private val delegate: MockEngine,
) : HttpClientEngineBase("sse-mock") {
    override val config: HttpClientEngineConfig = delegate.config

    override val supportedCapabilities: Set<HttpClientEngineCapability<*>> =
        delegate.supportedCapabilities + SSECapability

    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val response = delegate.execute(data)
        if (!data.isSseRequest()) return response
        return HttpResponseData(
            statusCode = response.statusCode,
            requestTime = response.requestTime,
            headers = response.headers,
            version = response.version,
            body = DefaultClientSSESession(data.body as SSEClientContent, response.body as ByteReadChannel),
            callContext = response.callContext,
        )
    }

    override fun close() {
        delegate.close()
        super.close()
    }
}

internal val koogProviders = LlmProvider.entries - LlmProvider.OnDevice

internal fun llmConfig(
    provider: LlmProvider,
    apiKey: String? = "test-key",
    providerUrl: String? = null,
) = LlmConfig(
    id = "config-${provider.name}",
    provider = provider,
    modelId = "model-${provider.name.lowercase()}",
    apiKey = apiKey,
    providerUrl = providerUrl,
    isDefault = true,
)

internal fun chatEntry(
    id: String,
    chatId: String,
    type: ChatEntryType,
    content: String,
) = ChatEntry(
    id = id,
    chatId = chatId,
    type = type,
    content = content,
    thinkingContent = null,
    llmConfigId = null,
    llmProvider = LlmProvider.Ollama.name,
    llmModelId = "model",
    tokensUsed = null,
    tokensPerSecond = null,
    responseTimeMs = null,
    timestamp = 0L,
)
