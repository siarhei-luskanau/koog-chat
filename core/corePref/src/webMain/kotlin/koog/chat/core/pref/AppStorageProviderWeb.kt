package koog.chat.core.pref

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.WebLocalStorage
import org.koin.core.annotation.Single

@Single
internal class AppStorageProviderWeb : StorageProvider {
    override fun <T> getStorage(serializer: OkioSerializer<T>): Storage<T> =
        WebLocalStorage(
            serializer = serializer,
            name = "app.pref.json",
        )
}
