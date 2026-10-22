package koog.chat.core.llm

sealed interface OnDeviceAvailability {
    data object Available : OnDeviceAvailability

    data object Downloadable : OnDeviceAvailability

    data class Downloading(
        val progress: Float?,
    ) : OnDeviceAvailability

    data class Unavailable(
        val reason: String,
    ) : OnDeviceAvailability
}
