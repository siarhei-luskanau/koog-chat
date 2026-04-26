package koog.chat.core.pref

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PrefData(
    @SerialName("key") val key: String?,
)
