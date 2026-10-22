package koog.chat.core.llm.ondevice

import com.google.mlkit.genai.common.FeatureStatus
import koog.chat.core.llm.OnDeviceAvailability
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OnDeviceStatusMapperAndroidHostTest {
    @Test
    fun mapFeatureStatus_shouldReturnAvailable_whenStatusAvailable() {
        assertEquals(OnDeviceAvailability.Available, mapFeatureStatus(FeatureStatus.AVAILABLE))
    }

    @Test
    fun mapFeatureStatus_shouldReturnDownloadable_whenStatusDownloadable() {
        assertEquals(OnDeviceAvailability.Downloadable, mapFeatureStatus(FeatureStatus.DOWNLOADABLE))
    }

    @Test
    fun mapFeatureStatus_shouldReturnDownloading_whenStatusDownloading() {
        assertEquals(OnDeviceAvailability.Downloading(null), mapFeatureStatus(FeatureStatus.DOWNLOADING))
    }

    @Test
    fun mapFeatureStatus_shouldReturnUnavailable_whenStatusUnavailable() {
        assertEquals(OnDeviceAvailability.Unavailable(UNSUPPORTED_REASON), mapFeatureStatus(FeatureStatus.UNAVAILABLE))
    }

    @Test
    fun mapFeatureStatus_shouldReturnUnavailable_whenStatusUnknown() {
        assertEquals(OnDeviceAvailability.Unavailable(UNSUPPORTED_REASON), mapFeatureStatus(Int.MIN_VALUE))
    }

    @Test
    fun downloadFraction_shouldReturnNull_whenTotalIsZero() {
        assertNull(downloadFraction(10L, 0L))
    }

    @Test
    fun downloadFraction_shouldReturnNull_whenTotalIsNegative() {
        assertNull(downloadFraction(10L, -5L))
    }

    @Test
    fun downloadFraction_shouldReturnRatio_whenPartiallyDownloaded() {
        assertEquals(0.25f, downloadFraction(25L, 100L))
    }

    @Test
    fun downloadFraction_shouldClampToOne_whenDownloadedExceedsTotal() {
        assertEquals(1f, downloadFraction(200L, 100L))
    }

    @Test
    fun downloadFraction_shouldClampToZero_whenDownloadedNegative() {
        assertEquals(0f, downloadFraction(-1L, 100L))
    }
}
