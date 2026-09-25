package koog.chat.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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

    @Test
    fun getReturnsFailureWhenHttpClientThrows() =
        runTest {
            val httpClient = HttpClient(MockEngine { throw IllegalStateException("boom") })
            val networkClient = NetworkClientKtor(httpClient)
            val result = networkClient.get("https://example.com")
            val failure = assertIs<NetworkResult.Failure<String>>(result)
            assertEquals("boom", failure.error.message)
            httpClient.close()
        }

    @Test
    fun postReturnsFailureWhenHttpClientThrows() =
        runTest {
            val httpClient = HttpClient(MockEngine { throw IllegalStateException("boom") })
            val networkClient = NetworkClientKtor(httpClient)
            val result = networkClient.post("https://example.com", """{"key":"value"}""")
            val failure = assertIs<NetworkResult.Failure<String>>(result)
            assertEquals("boom", failure.error.message)
            httpClient.close()
        }
}
