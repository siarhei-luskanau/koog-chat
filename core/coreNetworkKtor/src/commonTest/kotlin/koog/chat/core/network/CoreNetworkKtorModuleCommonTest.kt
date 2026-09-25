package koog.chat.core.network

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.pluginOrNull
import kotlin.test.Test
import kotlin.test.assertNotNull

internal class CoreNetworkKtorModuleCommonTest {
    @Test
    fun httpClientInstallsContentNegotiationPlugin() {
        val httpClient = CoreNetworkKtorModule().httpClient()
        assertNotNull(httpClient.pluginOrNull(ContentNegotiation))
        httpClient.close()
    }
}
