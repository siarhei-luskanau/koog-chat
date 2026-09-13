package template.core.database.room

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import template.core.database.DatabaseRecord
import template.core.database.DatabaseRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DatabaseRepositoryCommonTest {
    @Test
    fun saveAndGetAll() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val dao =
                koinApplication.koin
                    .get<RoomDatabaseProvider>()
                    .database
                    .exampleDao()
            dao.getAll().first().forEach { dao.deleteById(it.id) }
            val repository = koinApplication.koin.get<DatabaseRepository>()

            repository.save(DatabaseRecord(id = "1", tag = "test"))

            assertEquals(listOf(DatabaseRecord(id = "1", tag = "test")), repository.getAll().first())
            koinApplication.close()
        }

    @Test
    fun deleteById() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val dao =
                koinApplication.koin
                    .get<RoomDatabaseProvider>()
                    .database
                    .exampleDao()
            dao.getAll().first().forEach { dao.deleteById(it.id) }
            val repository = koinApplication.koin.get<DatabaseRepository>()

            repository.save(DatabaseRecord(id = "2", tag = "to-delete"))
            repository.delete("2")

            assertTrue(repository.getAll().first().isEmpty())
            koinApplication.close()
        }

    @Test
    fun saveUpdatesExistingRecord() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val dao =
                koinApplication.koin
                    .get<RoomDatabaseProvider>()
                    .database
                    .exampleDao()
            dao.getAll().first().forEach { dao.deleteById(it.id) }
            val repository = koinApplication.koin.get<DatabaseRepository>()

            repository.save(DatabaseRecord(id = "3", tag = "original"))
            repository.save(DatabaseRecord(id = "3", tag = "updated"))

            assertEquals(listOf(DatabaseRecord(id = "3", tag = "updated")), repository.getAll().first())
            koinApplication.close()
        }
}
