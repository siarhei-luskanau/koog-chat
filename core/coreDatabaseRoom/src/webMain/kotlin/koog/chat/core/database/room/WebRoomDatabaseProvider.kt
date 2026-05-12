package koog.chat.core.database.room

import androidx.room3.Room
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.koin.core.annotation.Single
import org.w3c.dom.Worker
import kotlin.js.ExperimentalWasmJsInterop

@Single
internal class WebRoomDatabaseProvider : RoomDatabaseProvider {
    @OptIn(ExperimentalWasmJsInterop::class)
    override val database: AppDatabase by lazy {
        val worker = Worker(scriptURL = "")
        Room
            .databaseBuilder<AppDatabase>(name = "koog_chat.db")
            .setDriver(driver = WebWorkerSQLiteDriver(worker = worker))
            .build()
    }
}
