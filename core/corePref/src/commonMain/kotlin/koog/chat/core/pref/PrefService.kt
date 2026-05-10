package koog.chat.core.pref

import kotlinx.coroutines.flow.Flow

interface PrefService {
    fun getUserPreferenceContent(): Flow<String?>

    fun getAppMode(): Flow<AppMode>

    suspend fun setAppMode(mode: AppMode)

    fun getSelectedLlmConfigId(): Flow<String?>

    suspend fun setSelectedLlmConfigId(id: String?)
}

enum class AppMode { Simple, Advanced }
