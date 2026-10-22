package koog.chat.core.llm.ondevice

import koog.chat.core.llm.OnDeviceAvailability

internal const val CHECKING_REASON = "Checking on-device model"

internal const val CHROME_AI_MISSING_REASON = "Chrome built-in AI is not available in this browser"

internal fun chromeAvailability(value: String?): OnDeviceAvailability =
    when (value) {
        "available" -> OnDeviceAvailability.Available
        "downloadable" -> OnDeviceAvailability.Downloadable
        "downloading" -> OnDeviceAvailability.Downloading(progress = null)
        null -> OnDeviceAvailability.Unavailable(CHROME_AI_MISSING_REASON)
        else -> OnDeviceAvailability.Unavailable("Chrome built-in AI is unavailable: $value")
    }
