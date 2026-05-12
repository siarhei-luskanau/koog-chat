package koog.chat.core.database.room

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Single
internal class IosRoomDatabaseProvider : RoomDatabaseProvider {
    @OptIn(ExperimentalForeignApi::class)
    override val database: AppDatabase by lazy {
        val documentDirectory =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )

        val dbFilePath = requireNotNull(documentDirectory?.path) + "/koog_chat.db"
        Room
            .databaseBuilder<AppDatabase>(name = dbFilePath)
            .setDriver(driver = BundledSQLiteDriver())
            .build()
    }
}
