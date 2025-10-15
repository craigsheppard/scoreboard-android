package com.scoreboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoreboard.data.models.AppConfiguration
import com.scoreboard.data.repository.ScoreboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScoreboardUiState(
    val appConfig: AppConfiguration = AppConfiguration()
)

@HiltViewModel
class ScoreboardViewModel @Inject constructor(
    private val repository: ScoreboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoreboardUiState())
    val uiState: StateFlow<ScoreboardUiState> = _uiState.asStateFlow()

    init {
        loadConfiguration()
    }

    private fun loadConfiguration() {
        viewModelScope.launch {
            val appConfig = repository.getAppConfiguration().first()
            _uiState.value = ScoreboardUiState(appConfig = appConfig)
        }
    }

    fun updateHomeTeamScore(newScore: Int) {
        val newConfig = uiState.value.appConfig.copy(
            homeTeam = uiState.value.appConfig.homeTeam.copy(score = newScore)
        )
        _uiState.value = uiState.value.copy(appConfig = newConfig)
        saveConfiguration()
    }

    fun updateAwayTeamScore(newScore: Int) {
        val newConfig = uiState.value.appConfig.copy(
            awayTeam = uiState.value.appConfig.awayTeam.copy(score = newScore)
        )
        _uiState.value = uiState.value.copy(appConfig = newConfig)
        saveConfiguration()
    }

    private fun saveConfiguration() {
        viewModelScope.launch {
            repository.saveAppConfiguration(uiState.value.appConfig)
        }
    }
}
