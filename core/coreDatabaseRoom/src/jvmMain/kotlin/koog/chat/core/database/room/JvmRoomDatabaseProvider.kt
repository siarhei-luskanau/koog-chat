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
            File(System.getProperty("user.home"), ".koog_chat/koog_chat.db")
                .also { it.parentFile?.mkdirs() }
        Room
            .databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
            .setDriver(driver = BundledSQLiteDriver())
            .setQueryCoroutineContext(context = Dispatchers.IO)
            .build()
    }
}
