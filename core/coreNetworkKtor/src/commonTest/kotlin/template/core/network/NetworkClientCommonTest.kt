package template.core.network

import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NetworkClientCommonTest {
    @Test
    fun getReturnsResponseBody() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val networkClient = koinApplication.koin.get<NetworkClient>()
            assertEquals(NetworkResult.Success("get response"), networkClient.get("https://example.com"))
            koinApplication.close()
        }

    @Test
    fun postReturnsResponseBody() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val networkClient = koinApplication.koin.get<NetworkClient>()
            assertEquals(
                NetworkResult.Success("post response"),
                networkClient.post("https://example.com", """{"key":"value"}"""),
            )
            koinApplication.close()
        }
}
