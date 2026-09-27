package koog.chat.core.llm.koog

import ai.koog.http.client.KoogHttpClient
import ai.koog.http.client.ktor.KtorKoogHttpClient
import io.ktor.client.HttpClient
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan(value = ["koog.chat.core.llm.koog"])
class CoreLlmKoogModule {
    @Single
    fun koogHttpClientFactory(): KoogHttpClient.Factory = KtorKoogHttpClient.Factory(HttpClient())
}
