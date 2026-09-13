package template.core.network

interface NetworkClient {
    suspend fun get(url: String): NetworkResult<String>

    suspend fun post(
        url: String,
        body: String,
    ): NetworkResult<String>
}
