package template.core.pref

import androidx.datastore.core.Storage

internal interface StorageProvider {
    fun getStorage(): Storage<PrefData>
}
