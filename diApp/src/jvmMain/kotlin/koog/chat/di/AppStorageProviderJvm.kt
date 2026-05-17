package koog.chat.di

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import koog.chat.core.pref.StorageProvider
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.annotation.Single
import java.io.File

@Single
internal class AppStorageProviderJvm : StorageProvider {
    override fun <T> getStorage(serializer: OkioSerializer<T>): Storage<T> {
        val storageFile =
            File(
                listOf(
                    System.getProperty("user.home"),
                    ".koog-chat",
                    "datastore",
                    "app.pref.json",
                ).joinToString(separator = File.separator),
            ).also { it.parentFile?.mkdirs() }
        return OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = { storageFile.absolutePath.toPath() },
        )
    }
}
