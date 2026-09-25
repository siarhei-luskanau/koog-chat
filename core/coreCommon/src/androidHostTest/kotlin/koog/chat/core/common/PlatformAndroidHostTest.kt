package koog.chat.core.common

import android.os.StrictMode
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class PlatformAndroidHostTest {
    private val platformService = PlatformServiceAndroid()
    private val dispatcherSet = DispatcherSetAndroid()

    @After
    fun resetStrictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    @Test
    fun setStrictMode_shouldEnablePenaltyDeath_whenEnabledTrue() {
        platformService.setStrictMode(true)
    }

    @Test
    fun setStrictMode_shouldSkipPenaltyDeath_whenEnabledFalse() {
        platformService.setStrictMode(false)
    }

    @Test
    fun defaultDispatcher_shouldReturnDispatchersDefault() {
        assertEquals(Dispatchers.Default, dispatcherSet.defaultDispatcher())
    }

    @Test
    fun ioDispatcher_shouldReturnDispatchersIO() {
        assertEquals(Dispatchers.IO, dispatcherSet.ioDispatcher())
    }

    @Test
    fun mainDispatcher_shouldReturnDispatchersMain() {
        assertEquals(Dispatchers.Main, dispatcherSet.mainDispatcher())
    }
}
