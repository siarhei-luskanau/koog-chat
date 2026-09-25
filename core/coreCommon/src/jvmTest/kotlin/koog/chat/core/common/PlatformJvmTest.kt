package koog.chat.core.common

import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformJvmTest {
    private val platformService = PlatformServiceJvm()
    private val dispatcherSet = DispatcherSetJvm()

    @Test
    fun setStrictMode_shouldDoNothing_whenEnabledTrue() {
        platformService.setStrictMode(true)
    }

    @Test
    fun setStrictMode_shouldDoNothing_whenEnabledFalse() {
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
