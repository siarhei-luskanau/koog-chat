package template.core.pref

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
internal class PrefServiceDataStore(
    private val storageProvider: StorageProvider,
) : PrefService {
    private val parser by lazy { Json { prettyPrint = true } }

    override fun getUserPreferenceContent(): Flow<String?> = getFlowFromDataStore { parser.encodeToString(PrefData.serializer(), it) }

    private val dataStore: DataStore<PrefData> by lazy {
        DataStoreFactory.create(
            storage = storageProvider.getStorage(),
            corruptionHandler = null,
            migrations = emptyList(),
        )
    }

    override suspend fun cleanStorage() {
        dataStore.updateData { PrefData.DEFAULT }
    }

    override fun getKey(): Flow<String?> = getFlowFromDataStore { it.key }

    override suspend fun setKey(key: String?) {
        updateDataStore { it.copy(key = key) }
    }

    private fun <T : Any> getFlowFromDataStore(mapData: (PrefData) -> T?): Flow<T?> = dataStore.data.map { mapData(it) }

    private suspend fun updateDataStore(update: (PrefData) -> PrefData) {
        dataStore.updateData { update(it) }
    }
}
