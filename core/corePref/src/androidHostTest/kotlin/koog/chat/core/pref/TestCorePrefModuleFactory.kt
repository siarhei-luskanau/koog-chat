package koog.chat.core.pref

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.annotation.Single
import java.io.File

private val TEST_PREF_FILE = File(System.getProperty("java.io.tmpdir"), "test.app.pref.json")

@Single
internal class TestStorageProvider : StorageProvider {
    override fun <T> getStorage(serializer: OkioSerializer<T>): Storage<T> =
        OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = { TEST_PREF_FILE.absolutePath.toPath() },
        )
}

actual fun cleanUpTestStorage() {
    TEST_PREF_FILE.delete()
}
