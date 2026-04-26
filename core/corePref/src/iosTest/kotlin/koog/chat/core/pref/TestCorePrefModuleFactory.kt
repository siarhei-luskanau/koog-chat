package koog.chat.core.pref

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.annotation.Single
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory

@OptIn(ExperimentalForeignApi::class)
private val TEST_PREF_PATH = NSTemporaryDirectory() + "test.app.pref.json"

@Single
internal class TestStorageProvider : StorageProvider {
    @OptIn(ExperimentalForeignApi::class)
    override fun <T> getStorage(serializer: OkioSerializer<T>): Storage<T> =
        OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = { TEST_PREF_PATH.toPath() },
        )
}

@OptIn(ExperimentalForeignApi::class)
actual fun cleanUpTestStorage() {
    NSFileManager.defaultManager.removeItemAtPath(TEST_PREF_PATH, error = null)
}
