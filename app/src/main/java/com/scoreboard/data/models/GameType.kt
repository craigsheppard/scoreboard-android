package com.scoreboard.data.models

enum class GameType(val displayName: String) {
    HOCKEY("Hockey"),
    BASKETBALL("Basketball"),
    SOCCER("Soccer"),
    TABLE_TENNIS("Table Tennis");

    companion object {
        fun fromDisplayName(name: String): GameType? {
            return values().firstOrNull { it.displayName == name }
        }
    }
}
