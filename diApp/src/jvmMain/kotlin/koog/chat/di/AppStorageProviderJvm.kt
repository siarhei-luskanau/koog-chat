package koog.chat.di

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import koog.chat.core.common.DispatcherSet
import koog.chat.core.pref.StorageProvider
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.annotation.Single
import java.io.File

@Single
internal class AppStorageProviderJvm(
    private val dispatcherSet: DispatcherSet,
) : StorageProvider {
    override fun <T> getStorage(serializer: OkioSerializer<T>): Storage<T> =
        OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = {
                runBlocking(dispatcherSet.ioDispatcher()) {
                    File.createTempFile("temp_", "app.pref.json").absolutePath.toPath()
                }
            },
        )
}
