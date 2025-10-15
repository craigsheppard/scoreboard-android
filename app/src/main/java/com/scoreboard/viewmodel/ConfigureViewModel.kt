package com.scoreboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoreboard.data.models.AppConfiguration
import com.scoreboard.data.models.GameType
import com.scoreboard.data.models.SavedTeam
import com.scoreboard.data.models.TeamConfiguration
import com.scoreboard.data.models.toArgb
import com.scoreboard.data.repository.ScoreboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigureUiState(
    val appConfig: AppConfiguration = AppConfiguration()
)

@HiltViewModel
class ConfigureViewModel @Inject constructor(
    private val repository: ScoreboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigureUiState())
    val uiState: StateFlow<ConfigureUiState> = _uiState.asStateFlow()

    init {
        observeConfiguration()
    }

    private fun observeConfiguration() {
        viewModelScope.launch {
            repository.getAppConfiguration().collect { appConfig ->
                _uiState.value = ConfigureUiState(appConfig = appConfig)
            }
        }
    }

    fun updateHomeTeam(team: TeamConfiguration) {
        val newConfig = uiState.value.appConfig.copy(homeTeam = team)
        _uiState.value = uiState.value.copy(appConfig = newConfig)
        saveConfiguration()
    }

    fun updateAwayTeam(team: TeamConfiguration) {
        val newConfig = uiState.value.appConfig.copy(awayTeam = team)
        _uiState.value = uiState.value.copy(appConfig = newConfig)
        saveConfiguration()
    }

    fun swapTeams() {
        val tempTeam = uiState.value.appConfig.homeTeam
        val newConfig = uiState.value.appConfig.copy(homeTeam = uiState.value.appConfig.awayTeam, awayTeam = tempTeam)
        _uiState.value = uiState.value.copy(appConfig = newConfig)
        saveConfiguration()
    }

    fun newGame() {
        val newConfig = uiState.value.appConfig.copy(
            homeTeam = uiState.value.appConfig.homeTeam.copy(score = 0),
            awayTeam = uiState.value.appConfig.awayTeam.copy(score = 0)
        )
        _uiState.value = uiState.value.copy(appConfig = newConfig)
        saveConfiguration()
    }

    fun setGameType(gameType: GameType) {
        val newConfig = uiState.value.appConfig.copy(currentGameType = gameType)
        _uiState.value = uiState.value.copy(appConfig = newConfig)
        saveConfiguration()
    }

    fun saveTeam(team: TeamConfiguration) {
        val newSavedTeam = SavedTeam(
            name = team.teamName,
            primaryColor = team.primaryColor.toArgb(),
            secondaryColor = team.secondaryColor.toArgb(),
            fontColor = team.fontColor.toArgb(),
            gameType = uiState.value.appConfig.currentGameType
        )
        val updatedTeams = uiState.value.appConfig.savedTeams.toMutableList().apply { add(newSavedTeam) }
        val newConfig = uiState.value.appConfig.copy(savedTeams = updatedTeams)
        _uiState.value = uiState.value.copy(appConfig = newConfig)
        saveSavedTeams()
    }

    fun deleteTeam(team: SavedTeam) {
        val updatedTeams = uiState.value.appConfig.savedTeams.toMutableList().apply { remove(team) }
        val newConfig = uiState.value.appConfig.copy(savedTeams = updatedTeams)
        _uiState.value = uiState.value.copy(appConfig = newConfig)
        saveSavedTeams()
    }

    fun getTeamsForCurrentGameType(): List<SavedTeam> {
        return uiState.value.appConfig.savedTeams.filter { it.gameType == uiState.value.appConfig.currentGameType }
    }

    private fun saveConfiguration() {
        viewModelScope.launch {
            repository.saveAppConfiguration(uiState.value.appConfig)
        }
    }

    private fun saveSavedTeams() {
        viewModelScope.launch {
            repository.saveSavedTeams(uiState.value.appConfig.savedTeams)
        }
    }
}
