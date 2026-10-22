package koog.chat.core.llm.ondevice

import koog.chat.core.llm.OnDeviceAvailability
import kotlin.test.Test
import kotlin.test.assertEquals

class ChromeAvailabilityWebTest {
    @Test
    fun chromeAvailability_shouldReturnAvailable_whenValueAvailable() {
        assertEquals(OnDeviceAvailability.Available, chromeAvailability("available"))
    }

    @Test
    fun chromeAvailability_shouldReturnDownloadable_whenValueDownloadable() {
        assertEquals(OnDeviceAvailability.Downloadable, chromeAvailability("downloadable"))
    }

    @Test
    fun chromeAvailability_shouldReturnDownloadingWithoutProgress_whenValueDownloading() {
        assertEquals(OnDeviceAvailability.Downloading(progress = null), chromeAvailability("downloading"))
    }

    @Test
    fun chromeAvailability_shouldReturnMissingReason_whenValueNull() {
        assertEquals(OnDeviceAvailability.Unavailable(CHROME_AI_MISSING_REASON), chromeAvailability(null))
    }

    @Test
    fun chromeAvailability_shouldIncludeValueInReason_whenValueUnknown() {
        assertEquals(
            OnDeviceAvailability.Unavailable("Chrome built-in AI is unavailable: unavailable"),
            chromeAvailability("unavailable"),
        )
    }
}
