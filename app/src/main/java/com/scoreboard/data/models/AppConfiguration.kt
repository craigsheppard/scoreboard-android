package com.scoreboard.data.models

import androidx.compose.ui.graphics.Color

data class AppConfiguration(
    var homeTeam: TeamConfiguration = TeamConfiguration(
        teamName = "Home",
        primaryColor = Color.Red,
        secondaryColor = Color.Blue,
        fontColor = Color.White
    ),
    var awayTeam: TeamConfiguration = TeamConfiguration(
        teamName = "Away",
        primaryColor = Color.Blue,
        secondaryColor = Color.Red,
        fontColor = Color.White
    ),
    var currentGameType: GameType = GameType.HOCKEY,
    var savedTeams: List<SavedTeam> = emptyList()
) {
    fun swapTeams() {
        val temp = homeTeam
        homeTeam = awayTeam
        awayTeam = temp
    }

    fun newGame() {
        homeTeam.score = 0
        awayTeam.score = 0
    }

    fun getTeamsForCurrentGameType(): List<SavedTeam> {
        return savedTeams.filter { it.gameType == currentGameType }
    }

    fun saveTeam(team: TeamConfiguration) {
        val existingIndex = savedTeams.indexOfFirst { it.id == team.savedTeamId }

        if (existingIndex != -1) {
            // Update existing team
            val updatedTeam = savedTeams[existingIndex].copy(
                name = team.teamName,
                primaryColor = team.primaryColor.toArgb(),
                secondaryColor = team.secondaryColor.toArgb(),
                fontColor = team.fontColor.toArgb()
            )
            savedTeams = savedTeams.toMutableList().apply {
                set(existingIndex, updatedTeam)
            }
        } else {
            // Create new team
            val newTeam = SavedTeam.fromTeamConfiguration(team, currentGameType)
            team.savedTeamId = newTeam.id
            savedTeams = savedTeams + newTeam
        }

        team.updateLastSavedState()
    }

    fun deleteTeam(team: SavedTeam) {
        savedTeams = savedTeams.filter { it.id != team.id }
    }
}
