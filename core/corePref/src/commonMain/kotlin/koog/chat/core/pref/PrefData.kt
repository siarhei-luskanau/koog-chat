package koog.chat.core.pref

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PrefData(
    @SerialName("app_mode") val appMode: String?,
    @SerialName("selected_llm_config_id") val selectedLlmConfigId: String?,
)
