package com.scoreboard.data.repository

import com.scoreboard.data.local.DataStoreManager
import com.scoreboard.data.models.AppConfiguration
import com.scoreboard.data.models.SavedTeam
import kotlinx.coroutines.flow.Flow

class ScoreboardRepositoryImpl(private val dataStoreManager: DataStoreManager) : ScoreboardRepository {

    override fun getAppConfiguration(): Flow<AppConfiguration> {
        return dataStoreManager.getAppConfiguration()
    }

    override suspend fun saveAppConfiguration(config: AppConfiguration) {
        dataStoreManager.saveAppConfiguration(config)
    }

    override suspend fun saveSavedTeams(teams: List<SavedTeam>) {
        dataStoreManager.saveSavedTeams(teams)
    }
}
