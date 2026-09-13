package template.core.pref

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioStorage
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Single
internal class AppStorageProviderIos : StorageProvider {
    @OptIn(ExperimentalForeignApi::class)
    override fun getStorage(): Storage<PrefData> =
        OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = PrefSerializer(),
            producePath = {
                (
                    NSFileManager.defaultManager
                        .URLForDirectory(
                            directory = NSDocumentDirectory,
                            inDomain = NSUserDomainMask,
                            appropriateForURL = null,
                            create = false,
                            error = null,
                        )?.path +
                        Path.DIRECTORY_SEPARATOR +
                        "app.pref.json"
                ).toPath()
            },
        )
}
