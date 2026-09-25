package koog.chat.core.pref

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.WebLocalStorage
import org.koin.core.annotation.Single

@Single
internal class AppStorageProviderWeb : StorageProvider {
    override fun getStorage(): Storage<PrefData> =
        WebLocalStorage(
            serializer = PrefSerializer(),
            name = "koog_chat_app.pref.json",
        )
}
