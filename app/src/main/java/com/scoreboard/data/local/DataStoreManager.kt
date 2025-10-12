package com.scoreboard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scoreboard.data.models.AppConfiguration
import com.scoreboard.data.models.GameType
import com.scoreboard.data.models.SavedTeam
import com.scoreboard.data.models.TeamConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.compose.ui.graphics.Color

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scoreboard_prefs")

class DataStoreManager(private val context: Context) {
    private val gson = Gson()

    companion object {
        private val HOME_TEAM_KEY = stringPreferencesKey("home_team")
        private val AWAY_TEAM_KEY = stringPreferencesKey("away_team")
        private val GAME_TYPE_KEY = stringPreferencesKey("game_type")
        private val SAVED_TEAMS_KEY = stringPreferencesKey("saved_teams")
    }

    // Data classes for serialization
    private data class SerializableTeam(
        val teamName: String,
        val primaryColor: Int,
        val secondaryColor: Int,
        val fontColor: Int,
        val score: Int,
        val savedTeamId: String?
    )

    fun getAppConfiguration(): Flow<AppConfiguration> {
        return context.dataStore.data.map { preferences ->
            val homeTeamJson = preferences[HOME_TEAM_KEY]
            val awayTeamJson = preferences[AWAY_TEAM_KEY]
            val gameTypeString = preferences[GAME_TYPE_KEY]
            val savedTeamsJson = preferences[SAVED_TEAMS_KEY]

            val homeTeam = homeTeamJson?.let { deserializeTeam(it) }
                ?: TeamConfiguration("Home", Color.Red, Color.Blue, Color.White)

            val awayTeam = awayTeamJson?.let { deserializeTeam(it) }
                ?: TeamConfiguration("Away", Color.Blue, Color.Red, Color.White)

            val gameType = gameTypeString?.let { GameType.valueOf(it) } ?: GameType.HOCKEY

            val savedTeams = savedTeamsJson?.let { deserializeSavedTeams(it) } ?: emptyList()

            AppConfiguration(homeTeam, awayTeam, gameType, savedTeams)
        }
    }

    suspend fun saveAppConfiguration(config: AppConfiguration) {
        context.dataStore.edit { preferences ->
            preferences[HOME_TEAM_KEY] = serializeTeam(config.homeTeam)
            preferences[AWAY_TEAM_KEY] = serializeTeam(config.awayTeam)
            preferences[GAME_TYPE_KEY] = config.currentGameType.name
            preferences[SAVED_TEAMS_KEY] = serializeSavedTeams(config.savedTeams)
        }
    }

    suspend fun saveSavedTeams(teams: List<SavedTeam>) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_TEAMS_KEY] = serializeSavedTeams(teams)
        }
    }

    private fun serializeTeam(team: TeamConfiguration): String {
        val serializable = SerializableTeam(
            teamName = team.teamName,
            primaryColor = team.primaryColor.toArgb(),
            secondaryColor = team.secondaryColor.toArgb(),
            fontColor = team.fontColor.toArgb(),
            score = team.score,
            savedTeamId = team.savedTeamId?.toString()
        )
        return gson.toJson(serializable)
    }

    private fun deserializeTeam(json: String): TeamConfiguration {
        val serializable = gson.fromJson(json, SerializableTeam::class.java)
        return TeamConfiguration(
            teamName = serializable.teamName,
            primaryColor = Color(serializable.primaryColor),
            secondaryColor = Color(serializable.secondaryColor),
            fontColor = Color(serializable.fontColor),
            score = serializable.score,
            savedTeamId = serializable.savedTeamId?.let { java.util.UUID.fromString(it) }
        )
    }

    private fun serializeSavedTeams(teams: List<SavedTeam>): String {
        return gson.toJson(teams.map { team ->
            mapOf(
                "id" to team.id.toString(),
                "name" to team.name,
                "primaryColor" to team.primaryColor,
                "secondaryColor" to team.secondaryColor,
                "fontColor" to team.fontColor,
                "gameType" to team.gameType.name
            )
        })
    }

    private fun deserializeSavedTeams(json: String): List<SavedTeam> {
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val list: List<Map<String, Any>> = gson.fromJson(json, type)
        return list.map { map ->
            SavedTeam(
                id = java.util.UUID.fromString(map["id"] as String),
                name = map["name"] as String,
                primaryColor = (map["primaryColor"] as Double).toInt(),
                secondaryColor = (map["secondaryColor"] as Double).toInt(),
                fontColor = (map["fontColor"] as Double).toInt(),
                gameType = GameType.valueOf(map["gameType"] as String)
            )
        }
    }
}

// Extension to convert Color to ARGB Int
private fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
