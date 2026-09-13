package template.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.annotation.Single

@Single
internal class NetworkClientKtor(
    private val httpClient: HttpClient,
) : NetworkClient {
    override suspend fun get(url: String): NetworkResult<String> =
        runCatching { httpClient.get(url).bodyAsText() }
            .fold(
                onSuccess = { NetworkResult.Success(it) },
                onFailure = { NetworkResult.Failure(it) },
            )

    override suspend fun post(
        url: String,
        body: String,
    ): NetworkResult<String> =
        runCatching {
            httpClient
                .post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }.bodyAsText()
        }.fold(
            onSuccess = { NetworkResult.Success(it) },
            onFailure = { NetworkResult.Failure(it) },
        )
}
