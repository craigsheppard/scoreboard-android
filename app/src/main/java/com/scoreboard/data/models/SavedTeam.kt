package com.scoreboard.data.models

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class SavedTeam(
    val id: UUID = UUID.randomUUID(),
    var name: String,
    var primaryColor: Int,  // Store as Int (ARGB)
    var secondaryColor: Int,
    var fontColor: Int,
    val gameType: GameType
) {
    fun toTeamConfiguration(): TeamConfiguration {
        return TeamConfiguration(
            teamName = name,
            primaryColor = Color(primaryColor),
            secondaryColor = Color(secondaryColor),
            fontColor = Color(fontColor),
            score = 0,
            savedTeamId = id
        )
    }

    companion object {
        fun fromTeamConfiguration(team: TeamConfiguration, gameType: GameType): SavedTeam {
            return SavedTeam(
                id = team.savedTeamId ?: UUID.randomUUID(),
                name = team.teamName,
                primaryColor = team.primaryColor.toArgb(),
                secondaryColor = team.secondaryColor.toArgb(),
                fontColor = team.fontColor.toArgb(),
                gameType = gameType
            )
        }
    }
}

// Extension function for Color.toArgb() compatibility
fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
