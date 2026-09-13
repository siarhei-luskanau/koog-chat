package template.core.pref

import kotlinx.coroutines.flow.Flow

interface PrefService {
    suspend fun cleanStorage()

    fun getUserPreferenceContent(): Flow<String?>

    fun getKey(): Flow<String?>

    suspend fun setKey(key: String?)
}
