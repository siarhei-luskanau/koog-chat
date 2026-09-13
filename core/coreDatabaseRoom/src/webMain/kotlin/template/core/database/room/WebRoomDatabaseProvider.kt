package template.core.database.room

import androidx.room3.Room
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.koin.core.annotation.Single
import org.w3c.dom.Worker
import kotlin.js.ExperimentalWasmJsInterop

@Single
@OptIn(ExperimentalWasmJsInterop::class)
internal class WebRoomDatabaseProvider : RoomDatabaseProvider {
    override val database: AppDatabase by lazy {
        Room
            .databaseBuilder<AppDatabase>(name = "cmp_template.db")
            .setDriver(driver = WebWorkerSQLiteDriver(worker = createWorker()))
            .build()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun createWorker(): Worker = js("""new Worker(new URL("sql-js-worker/worker.js", import.meta.url))""")
