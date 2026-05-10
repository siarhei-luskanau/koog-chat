package koog.chat.core.pref

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.core.context.stopKoin
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

expect fun cleanUpTestStorage()

internal class PrefServiceCommonTest {
    @Test
    fun writeAndReadAppMode() =
        runTest {
            cleanUpTestStorage()
            val koinApplication = koinApplication<TestKoinApplication>()
            val service = koinApplication.koin.get<PrefService>()
            assertEquals(AppMode.Simple, service.getAppMode().first())
            service.setAppMode(AppMode.Advanced)
            assertEquals(AppMode.Advanced, service.getAppMode().first())
            koinApplication.close()
        }

    @Ignore // There are multiple DataStores active for the same
    @Test
    fun persistenceAcrossKoinSessions() {
        cleanUpTestStorage()
        runTest {
            val koinApplication1 = koinApplication<TestKoinApplication>()
            koinApplication1.koin.get<PrefService>().setAppMode(AppMode.Advanced)
            stopKoin()
        }
        runTest {
            val koinApplication2 = koinApplication<TestKoinApplication>()
            val service = koinApplication2.koin.get<PrefService>()
            assertEquals(AppMode.Advanced, service.getAppMode().first())
            stopKoin()
        }
    }
}
