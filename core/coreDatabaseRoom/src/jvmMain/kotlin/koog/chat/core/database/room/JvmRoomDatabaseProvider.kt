package koog.chat.core.database.room

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single
import java.io.File

@Single
internal class JvmRoomDatabaseProvider : RoomDatabaseProvider {
    override val database: AppDatabase by lazy {
        val dbFile =
            File(
                listOf(System.getProperty("user.home"), ".koog-chat-app", "room", "koog_chat_app.db")
                    .joinToString(separator = File.separator),
            ).also { it.parentFile?.mkdirs() }
        Room
            .databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
            .setDriver(driver = BundledSQLiteDriver())
            .setQueryCoroutineContext(context = Dispatchers.IO)
            .build()
    }
}
