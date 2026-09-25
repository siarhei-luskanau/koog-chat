package koog.chat.ui.main

import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.database.api.repository.LlmConfigRepository
import koog.chat.core.pref.PrefService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainViewModelCommonTest {
    private val testDispatcher = StandardTestDispatcher()

    private class FakeMainNavigationCallback : MainNavigationCallback {
        var goBackCount = 0

        override fun goBack() {
            goBackCount++
        }
    }

    private class FakeDispatcherSet(
        private val dispatcher: CoroutineDispatcher,
    ) : DispatcherSet {
        override fun defaultDispatcher(): CoroutineDispatcher = dispatcher

        override fun ioDispatcher(): CoroutineDispatcher = dispatcher

        override fun mainDispatcher(): CoroutineDispatcher = dispatcher
    }

    private class FakePrefService(
        private val keyFlow: MutableStateFlow<String?>,
    ) : PrefService {
        override suspend fun cleanStorage(): Unit = throw NotImplementedError()

        override fun getUserPreferenceContent(): Flow<String?> = throw NotImplementedError()

        override fun getKey(): Flow<String?> = keyFlow

        override suspend fun setKey(key: String?) {
            keyFlow.value = key
        }
    }

    private class FakeLlmConfigRepository(
        private val configsFlow: MutableStateFlow<List<LlmConfig>>,
    ) : LlmConfigRepository {
        override fun getAllFlow(): Flow<List<LlmConfig>> = configsFlow

        override suspend fun getById(id: String): LlmConfig? = throw NotImplementedError()

        override suspend fun save(config: LlmConfig): Unit = throw NotImplementedError()

        override suspend fun delete(id: String): Unit = throw NotImplementedError()

        override suspend fun setDefault(id: String): Unit = throw NotImplementedError()
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleLlmConfig() =
        LlmConfig(
            id = "config-1",
            provider = LlmProvider.OpenAI,
            modelId = "gpt-test",
            apiKey = null,
            providerUrl = null,
            isDefault = true,
        )

    private fun buildViewModel(
        navigationCallback: FakeMainNavigationCallback = FakeMainNavigationCallback(),
        keyFlow: MutableStateFlow<String?> = MutableStateFlow("initial-key"),
        configsFlow: MutableStateFlow<List<LlmConfig>> = MutableStateFlow(listOf(sampleLlmConfig())),
    ) = MainViewModel(
        initArg = "arg",
        navigationCallback = navigationCallback,
        dispatcherSet = FakeDispatcherSet(testDispatcher),
        prefService = FakePrefService(keyFlow),
        llmConfigRepository = FakeLlmConfigRepository(configsFlow),
    )

    @Test
    fun viewState_shouldBeLoading_whenNoCollectorSubscribed() =
        runTest {
            val viewModel = buildViewModel()

            assertIs<MainViewState.Loading>(viewModel.viewState.value)
        }

    @Test
    fun viewState_shouldEmitSuccess_whenCollected() =
        runTest {
            val keyFlow = MutableStateFlow<String?>("pref-key")
            val configsFlow = MutableStateFlow(listOf(sampleLlmConfig()))
            val viewModel = buildViewModel(keyFlow = keyFlow, configsFlow = configsFlow)

            backgroundScope.launch { viewModel.viewState.collect {} }
            advanceUntilIdle()

            val state = assertIs<MainViewState.Success>(viewModel.viewState.value)
            assertTrue(state.data.contains("initArg=arg"))
            assertTrue(state.data.contains("pref=pref-key"))
            assertTrue(state.data.contains("llmConfigs=1"))
        }

    @Test
    fun viewState_shouldUpdate_whenPrefKeyChanges() =
        runTest {
            val keyFlow = MutableStateFlow<String?>("first-key")
            val viewModel = buildViewModel(keyFlow = keyFlow)

            backgroundScope.launch { viewModel.viewState.collect {} }
            advanceUntilIdle()

            keyFlow.value = "second-key"
            advanceUntilIdle()

            val state = assertIs<MainViewState.Success>(viewModel.viewState.value)
            assertTrue(state.data.contains("pref=second-key"))
        }

    @Test
    fun onEvent_shouldInvokeGoBackOnce_whenNavigateBack() =
        runTest {
            val navigationCallback = FakeMainNavigationCallback()
            val viewModel = buildViewModel(navigationCallback = navigationCallback)

            viewModel.onEvent(MainViewEvent.NavigateBack)
            advanceUntilIdle()

            assertEquals(1, navigationCallback.goBackCount)
        }

    @Test
    fun error_shouldExposeThrowable_whenConstructed() {
        val throwable = IllegalStateException("boom")

        val error = MainViewState.Error(error = throwable)

        assertSame(throwable, error.error)
    }
}
