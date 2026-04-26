package koog.chat.core.pref

import kotlinx.coroutines.flow.Flow

interface PrefService {
    fun getUserPreferenceContent(): Flow<String?>

    fun getKey(): Flow<String?>

    suspend fun setKey(key: String?)
}
