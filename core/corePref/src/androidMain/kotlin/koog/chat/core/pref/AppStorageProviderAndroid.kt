package koog.chat.core.pref

import android.content.Context
import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import koog.chat.core.common.DispatcherSet
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
internal class AppStorageProviderAndroid(
    private val context: Context,
    @Provided private val dispatcherSet: DispatcherSet,
) : StorageProvider {
    override fun <T> getStorage(serializer: OkioSerializer<T>): Storage<T> =
        OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = {
                runBlocking(dispatcherSet.ioDispatcher()) {
                    context.filesDir
                        .resolve("app.pref.json")
                        .absolutePath
                        .toPath()
                }
            },
        )
}
