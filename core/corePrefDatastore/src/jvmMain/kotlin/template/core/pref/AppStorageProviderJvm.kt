package template.core.pref

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.annotation.Single
import java.io.File

@Single
internal class AppStorageProviderJvm : StorageProvider {
    override fun getStorage(): Storage<PrefData> {
        val storageFile =
            File(
                listOf(
                    System.getProperty("user.home"),
                    ".cmp-template",
                    "datastore",
                    "app.pref.json",
                ).joinToString(separator = File.separator),
            ).also { it.parentFile?.mkdirs() }
        return OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = PrefSerializer(),
            producePath = { storageFile.absolutePath.toPath() },
        )
    }
}
