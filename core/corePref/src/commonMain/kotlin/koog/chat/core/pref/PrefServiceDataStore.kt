package koog.chat.core.pref

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
internal class PrefServiceDataStore(
    @Provided private val storageProvider: StorageProvider,
) : PrefService {
    private val parser by lazy { Json { prettyPrint = true } }

    override fun getUserPreferenceContent(): Flow<String?> = getFlowFromDataStore { parser.encodeToString(PrefData.serializer(), it) }

    private val dataStore: DataStore<PrefData> by lazy {
        DataStoreFactory.create(
            storage = storageProvider.getStorage(serializer = PrefSerializer()),
            corruptionHandler = null,
            migrations = emptyList(),
        )
    }

    override fun getAppMode(): Flow<AppMode> =
        dataStore.data.map {
            it.appMode?.let { appModeString -> AppMode.valueOf(appModeString) } ?: AppMode.Simple
        }

    override suspend fun setAppMode(mode: AppMode) {
        updateDataStore { it.copy(appMode = mode.name) }
    }

    override fun getSelectedLlmConfigId(): Flow<String?> = getFlowFromDataStore { it.selectedLlmConfigId }

    override suspend fun setSelectedLlmConfigId(id: String?) {
        updateDataStore { it.copy(selectedLlmConfigId = id) }
    }

    private fun <T : Any> getFlowFromDataStore(mapData: (PrefData) -> T?): Flow<T?> = dataStore.data.map { mapData(it) }

    private suspend fun updateDataStore(update: (PrefData) -> PrefData) {
        dataStore.updateData { update(it) }
    }
}
