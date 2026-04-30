package koog.chat.ui.llmconfig.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.repository.LlmConfigRepository
import koog.chat.ui.llmconfig.LlmConfigNavigationCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KoinViewModel
class LlmConfigDetailsViewModel(
    @InjectedParam private val configId: String?,
    @Provided private val navigationCallback: LlmConfigNavigationCallback,
    @Provided private val repository: LlmConfigRepository,
) : ViewModel() {
    val viewState: StateFlow<LlmConfigDetailsViewState>
        field =
            MutableStateFlow(
                LlmConfigDetailsViewState(isNew = configId == null, canDelete = configId != null),
            )

    init {
        val id = configId
        if (id != null) {
            viewModelScope.launch {
                repository.getById(id)?.let { config -> viewState.value = config.toViewState() }
            }
        }
    }

    fun onEvent(event: LlmConfigDetailsViewEvent) {
        viewModelScope.launch {
            when (event) {
                is LlmConfigDetailsViewEvent.ProviderChanged -> {
                    viewState.value = viewState.value.copy(provider = event.provider)
                }

                is LlmConfigDetailsViewEvent.ModelIdChanged -> {
                    viewState.value = viewState.value.copy(modelId = event.value)
                }

                is LlmConfigDetailsViewEvent.ApiKeyChanged -> {
                    viewState.value = viewState.value.copy(apiKey = event.value)
                }

                is LlmConfigDetailsViewEvent.ProviderUrlChanged -> {
                    viewState.value = viewState.value.copy(providerUrl = event.value)
                }

                LlmConfigDetailsViewEvent.ToggleDefault -> {
                    viewState.value = viewState.value.copy(isDefault = !viewState.value.isDefault)
                }

                LlmConfigDetailsViewEvent.Save -> {
                    saveConfig()
                }

                LlmConfigDetailsViewEvent.Delete -> {
                    configId?.let { repository.delete(it) }
                    navigationCallback.goBack()
                }

                LlmConfigDetailsViewEvent.NavigateBack -> {
                    navigationCallback.goBack()
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun saveConfig() {
        val state = viewState.value
        val id = configId ?: Uuid.random().toString()
        val config =
            LlmConfig(
                id = id,
                provider = state.provider,
                modelId = state.modelId,
                apiKey = state.apiKey.ifBlank { null },
                providerUrl = state.providerUrl.ifBlank { null },
                isDefault = state.isDefault,
            )
        repository.save(config)
        if (state.isDefault) {
            repository.setDefault(id)
        }
        navigationCallback.goBack()
    }

    private fun LlmConfig.toViewState() =
        LlmConfigDetailsViewState(
            provider = provider,
            modelId = modelId,
            apiKey = apiKey.orEmpty(),
            providerUrl = providerUrl.orEmpty(),
            isDefault = isDefault,
            isNew = false,
            canDelete = true,
        )
}
