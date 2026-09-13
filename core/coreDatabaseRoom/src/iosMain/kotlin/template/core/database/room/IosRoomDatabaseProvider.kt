package template.core.database.room

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
        val dbPath =
            NSFileManager.defaultManager
                .URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = true,
                    error = null,
                )?.URLByAppendingPathComponent("cmp_template.db")
                ?.path ?: "cmp_template.db"
        Room
            .databaseBuilder<AppDatabase>(name = dbPath)
            .setDriver(driver = BundledSQLiteDriver())
            .build()
    }
}
