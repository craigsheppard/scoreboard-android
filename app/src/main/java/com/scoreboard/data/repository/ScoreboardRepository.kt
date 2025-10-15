package com.scoreboard.data.repository

import com.scoreboard.data.models.AppConfiguration
import com.scoreboard.data.models.SavedTeam
import kotlinx.coroutines.flow.Flow

interface ScoreboardRepository {
    fun getAppConfiguration(): Flow<AppConfiguration>
    suspend fun saveAppConfiguration(config: AppConfiguration)
    suspend fun saveSavedTeams(teams: List<SavedTeam>)
}
