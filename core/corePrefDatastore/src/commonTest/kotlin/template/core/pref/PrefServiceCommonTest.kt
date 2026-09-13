package template.core.pref

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.core.context.stopKoin
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PrefServiceCommonTest {
    @Test
    fun writeAndReadKey() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val service = koinApplication.koin.get<PrefService>()
            service.cleanStorage()
            assertNull(service.getKey().first())
            service.setKey("test-value")
            assertEquals("test-value", service.getKey().first())
            koinApplication.close()
        }

    @Ignore // There are multiple DataStores active for the same
    @Test
    fun persistenceAcrossKoinSessions() {
        runTest {
            val koinApplication1 = koinApplication<TestKoinApplication>()
            val service = koinApplication1.koin.get<PrefService>()
            service.cleanStorage()
            service.setKey("alice")
            stopKoin()
        }
        runTest {
            val koinApplication2 = koinApplication<TestKoinApplication>()
            val service = koinApplication2.koin.get<PrefService>()
            assertEquals("alice", service.getKey().first())
            stopKoin()
        }
    }
}
