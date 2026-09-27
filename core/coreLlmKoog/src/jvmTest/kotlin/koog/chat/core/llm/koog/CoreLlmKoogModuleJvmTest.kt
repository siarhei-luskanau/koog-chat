package koog.chat.core.llm.koog

import koog.chat.core.llm.LlmService
import koog.chat.core.llm.LlmSessionManager
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertIs

// JVM-only: Koin compile safety reports a false KOIN-D002 for commonMain @ComponentScan bindings in js/wasm/native test klibs

internal class CoreLlmKoogModuleJvmTest {
    @Test
    fun moduleBindsLlmApiToKoogImplementations() {
        val koinApplication = koinApplication<TestKoinApplication>()
        assertIs<LlmServiceKoog>(koinApplication.koin.get<LlmService>())
        assertIs<LlmSessionManagerImpl>(koinApplication.koin.get<LlmSessionManager>())
        koinApplication.close()
    }
}
