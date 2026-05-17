package koog.chat.di

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import koog.chat.core.database.room.RoomDatabaseProvider
import koog.chat.di.app.IS_DATA_STUB_ENABLED
import koog.chat.navigation.NavApp
import koog.chat.navigation.navigationModule
import koog.chat.ui.common.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration
import org.koin.mp.KoinPlatform.getKoin
import org.koin.plugin.module.dsl.koinConfiguration

@Composable
fun KoinApp() =
    KoinApplication(
        configuration =
            KoinConfiguration {
                koinConfiguration<DiKoinApplication>().config.invoke(this)
                modules(navigationModule)
            },
    ) {
        val koin = getKoin()
        LaunchedEffect(Unit) {
            if (IS_DATA_STUB_ENABLED) {
                koin.get<RoomDatabaseProvider>().database.clearAllTables()
            }
        }
        Box(Modifier.semantics { contentDescription = "KoinApp" }) {
            NavApp()
        }
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun KoinAppPreviewLight() = AppTheme { KoinApp() }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun KoinAppPreviewNight() = AppTheme { KoinApp() }
