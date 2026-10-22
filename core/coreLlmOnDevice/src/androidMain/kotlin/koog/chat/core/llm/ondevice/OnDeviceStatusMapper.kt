package koog.chat.core.llm.ondevice

import com.google.mlkit.genai.common.FeatureStatus
import koog.chat.core.llm.OnDeviceAvailability

internal const val CHECKING_REASON = "Checking on-device model"
internal const val UNSUPPORTED_REASON = "Gemini Nano is not available on this device"

internal fun mapFeatureStatus(status: Int): OnDeviceAvailability =
    when (status) {
        FeatureStatus.AVAILABLE -> OnDeviceAvailability.Available
        FeatureStatus.DOWNLOADABLE -> OnDeviceAvailability.Downloadable
        FeatureStatus.DOWNLOADING -> OnDeviceAvailability.Downloading(null)
        else -> OnDeviceAvailability.Unavailable(UNSUPPORTED_REASON)
    }
