package com.scoreboard.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scoreboard.data.local.DataStoreManager
import com.scoreboard.data.models.AppConfiguration
import com.scoreboard.data.models.GameType
import com.scoreboard.data.models.SavedTeam
import com.scoreboard.data.models.TeamConfiguration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScoreboardViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)

    var appConfig by mutableStateOf(AppConfiguration())
        private set

    init {
        loadConfiguration()
    }

    private fun loadConfiguration() {
        viewModelScope.launch {
            appConfig = dataStoreManager.getAppConfiguration().first()
        }
    }

    fun updateHomeTeamScore(newScore: Int) {
        appConfig = appConfig.copy(
            homeTeam = appConfig.homeTeam.copy(score = newScore)
        )
        saveConfiguration()
    }

    fun updateAwayTeamScore(newScore: Int) {
        appConfig = appConfig.copy(
            awayTeam = appConfig.awayTeam.copy(score = newScore)
        )
        saveConfiguration()
    }

    fun updateHomeTeam(team: TeamConfiguration) {
        appConfig = appConfig.copy(homeTeam = team)
        saveConfiguration()
    }

    fun updateAwayTeam(team: TeamConfiguration) {
        appConfig = appConfig.copy(awayTeam = team)
        saveConfiguration()
    }

    fun swapTeams() {
        appConfig.swapTeams()
        appConfig = appConfig.copy()  // Trigger recomposition
        saveConfiguration()
    }

    fun newGame() {
        appConfig.newGame()
        appConfig = appConfig.copy()  // Trigger recomposition
        saveConfiguration()
    }

    fun setGameType(gameType: GameType) {
        appConfig = appConfig.copy(currentGameType = gameType)
        saveConfiguration()
    }

    fun saveTeam(team: TeamConfiguration) {
        appConfig.saveTeam(team)
        appConfig = appConfig.copy()  // Trigger recomposition
        saveConfiguration()
        saveSavedTeams()
    }

    fun deleteTeam(team: SavedTeam) {
        appConfig.deleteTeam(team)
        appConfig = appConfig.copy()  // Trigger recomposition
        saveSavedTeams()
    }

    fun getTeamsForCurrentGameType(): List<SavedTeam> {
        return appConfig.getTeamsForCurrentGameType()
    }

    private fun saveConfiguration() {
        viewModelScope.launch {
            dataStoreManager.saveAppConfiguration(appConfig)
        }
    }

    private fun saveSavedTeams() {
        viewModelScope.launch {
            dataStoreManager.saveSavedTeams(appConfig.savedTeams)
        }
    }
}
