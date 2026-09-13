package template.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal class TestCoreNetworkKtorModule {
    @Single
    fun httpClient(): HttpClient =
        HttpClient(
            MockEngine { request ->
                when (request.method) {
                    HttpMethod.Get -> {
                        respond(
                            content = "get response",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                        )
                    }

                    HttpMethod.Post -> {
                        respond(
                            content = "post response",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                        )
                    }

                    else -> {
                        respond(
                            content = "",
                            status = HttpStatusCode.OK,
                        )
                    }
                }
            },
        )

    @Single
    fun networkClient(httpClient: HttpClient): NetworkClient = NetworkClientKtor(httpClient)
}
