package com.scoreboard.data.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.UUID

data class TeamConfiguration(
    var teamName: String,
    var primaryColor: Color,
    var secondaryColor: Color,
    var fontColor: Color,
    var score: Int = 0,
    var savedTeamId: UUID? = null
) {
    // Track the last saved state for change detection
    var lastSavedState: TeamSavedState? = TeamSavedState(
        name = teamName,
        primaryColor = primaryColor.toArgb(),
        secondaryColor = secondaryColor.toArgb(),
        fontColor = fontColor.toArgb()
    )

    fun hasUnsavedChanges(): Boolean {
        val lastSaved = lastSavedState ?: return !teamName.isEmpty() && teamName != "New Team"

        return lastSaved.name != teamName ||
               lastSaved.primaryColor != primaryColor.toArgb() ||
               lastSaved.secondaryColor != secondaryColor.toArgb() ||
               lastSaved.fontColor != fontColor.toArgb()
    }

    fun updateLastSavedState() {
        lastSavedState = TeamSavedState(
            name = teamName,
            primaryColor = primaryColor.toArgb(),
            secondaryColor = secondaryColor.toArgb(),
            fontColor = fontColor.toArgb()
        )
    }
}

data class TeamSavedState(
    val name: String,
    val primaryColor: Int,
    val secondaryColor: Int,
    val fontColor: Int
)
