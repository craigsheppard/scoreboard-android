package com.scoreboard.data.models

enum class BasketballScoringState {
    INACTIVE,
    WAITING_FOR_TARGET,
    TARGET_HIT
}

enum class TargetType {
    TWO_POINT,
    THREE_POINT
}

enum class ScoreSide {
    LEFT,
    RIGHT
}
