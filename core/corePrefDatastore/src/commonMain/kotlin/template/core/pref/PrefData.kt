package template.core.pref

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PrefData(
    @SerialName("key") val key: String?,
) {
    companion object {
        val DEFAULT = PrefData(key = null)
    }
}
