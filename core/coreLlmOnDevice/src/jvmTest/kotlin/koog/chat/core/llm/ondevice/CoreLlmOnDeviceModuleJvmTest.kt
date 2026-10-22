package koog.chat.core.llm.ondevice

import koog.chat.core.llm.OnDeviceLlm
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertIs

class CoreLlmOnDeviceModuleJvmTest {
    @Test
    fun module_shouldBindOnDeviceLlmJvm_whenStartedOnJvm() {
        val koinApplication = koinApplication<TestKoinApplication>()
        assertIs<OnDeviceLlmJvm>(koinApplication.koin.get<OnDeviceLlm>())
        koinApplication.close()
    }
}
