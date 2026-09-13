package template.core.database.room

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.annotation.Single

@Single
internal class AndroidRoomDatabaseProvider(
    private val context: Context,
) : RoomDatabaseProvider {
    override val database: AppDatabase by lazy {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath("cmp_template.db").also { it.parentFile?.mkdirs() }
        Room
            .databaseBuilder<AppDatabase>(context = appContext, name = dbFile.absolutePath)
            .setDriver(driver = BundledSQLiteDriver())
            .build()
    }
}
