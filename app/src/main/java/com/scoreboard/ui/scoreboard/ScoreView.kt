package com.scoreboard.ui.scoreboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.scoreboard.data.models.GameType
import com.scoreboard.data.models.ScoreSide
import com.scoreboard.data.models.TeamConfiguration
import com.scoreboard.ui.components.BasketballTargets
import com.scoreboard.ui.components.OutlinedText
import com.scoreboard.ui.theme.JerseyFont
import com.scoreboard.utils.HapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun ScoreView(
    team: TeamConfiguration,
    side: ScoreSide,
    gameType: GameType,
    onScoreChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    val scope = rememberCoroutineScope()

    var dragOffset by remember { mutableStateOf(0f) }
    var swipeCompleted by remember { mutableStateOf(false) }
    var flashColor by remember { mutableStateOf<Color?>(null) }
    var currentDragPosition by remember { mutableStateOf(Offset.Zero) }

    val density = LocalDensity.current
    // Threshold should be about the height of a number (175 fontSize * 0.33 damping ≈ 58)
    val swipeThreshold = 58f * density.density

    val backgroundColor = flashColor ?: team.primaryColor

    // Basketball-specific state
    var basketballState by remember { mutableStateOf(com.scoreboard.data.models.BasketballScoringState.INACTIVE) }
    var showTargets by remember { mutableStateOf(false) }
    var twoPointHit by remember { mutableStateOf(false) }
    var threePointHit by remember { mutableStateOf(false) }
    var initialPointScored by remember { mutableStateOf(false) }

    val isBasketballMode = gameType == GameType.BASKETBALL

    // Use rememberUpdatedState to always capture the latest team value
    val currentTeam by rememberUpdatedState(team)

    fun triggerFlash() {
        flashColor = currentTeam.secondaryColor.copy(alpha = 0.4f)
        scope.launch {
            delay(50)
            flashColor = null
        }
    }

    fun increaseScore(by: Int, hapticCount: Int = 1) {
        onScoreChange(currentTeam.score + by)
        scope.launch {
            haptic.triggerMultipleHaptics(hapticCount)
        }
        triggerFlash()
    }

    fun decreaseScore() {
        if (currentTeam.score > 0) {
            onScoreChange(currentTeam.score - 1)
            haptic.triggerHapticFeedback()
            triggerFlash()
        } else {
            haptic.triggerErrorFeedback()
        }
    }

    fun resetDrag() {
        scope.launch {
            val animatable = Animatable(dragOffset)
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = spring()
            )
            dragOffset = animatable.value
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures {
                    increaseScore(by = 1)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        swipeCompleted = false
                        dragOffset = 0f

                        if (showTargets) {
                            showTargets = false
                        }
                        basketballState = com.scoreboard.data.models.BasketballScoringState.INACTIVE
                        twoPointHit = false
                        threePointHit = false
                        initialPointScored = false
                    }
                ) { change, dragAmount ->
                    change.consume()

                    if (swipeCompleted) return@detectDragGestures

                    // Track current drag position
                    currentDragPosition = change.position

                    val horizontalLimit = if (isBasketballMode) 200f else 70f

                    if (kotlin.math.abs(dragAmount.x) < horizontalLimit) {
                        dragOffset += dragAmount.y * 0.33f

                        if (isBasketballMode) {
                            // Basketball mode drag handling
                            if (dragOffset < -30 && !showTargets && basketballState == com.scoreboard.data.models.BasketballScoringState.INACTIVE) {
                                showTargets = true
                                basketballState = com.scoreboard.data.models.BasketballScoringState.WAITING_FOR_TARGET
                            }

                            if (dragOffset < -swipeThreshold && basketballState == com.scoreboard.data.models.BasketballScoringState.WAITING_FOR_TARGET && !initialPointScored) {
                                increaseScore(by = 1, hapticCount = 1)
                                initialPointScored = true
                            }

                            // Check for target hits while dragging
                            if (basketballState == com.scoreboard.data.models.BasketballScoringState.WAITING_FOR_TARGET &&
                                showTargets && !twoPointHit && !threePointHit) {

                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                // Hit radius matches iOS target radius (70pt diameter / 2 = 35pt radius)
                                val hitRadius = 35f * density.density

                                // Calculate 2-point target position (same row as 3-point)
                                val twoPointPos = Offset(
                                    canvasWidth / 2f,
                                    canvasHeight * 0.15f
                                )

                                // Calculate 3-point target position
                                val threePointPos = when (side) {
                                    ScoreSide.LEFT -> Offset(
                                        canvasWidth * 0.15f,
                                        canvasHeight * 0.15f
                                    )
                                    ScoreSide.RIGHT -> Offset(
                                        canvasWidth * 0.85f,
                                        canvasHeight * 0.15f
                                    )
                                }

                                // Check 2-point hit
                                val distance2pt = sqrt(
                                    (currentDragPosition.x - twoPointPos.x) * (currentDragPosition.x - twoPointPos.x) +
                                    (currentDragPosition.y - twoPointPos.y) * (currentDragPosition.y - twoPointPos.y)
                                )

                                if (distance2pt <= hitRadius) {
                                    twoPointHit = true
                                    basketballState = com.scoreboard.data.models.BasketballScoringState.TARGET_HIT
                                    // If initial point wasn't scored yet, award full 2 points, otherwise add 1 more
                                    val pointsToAdd = if (initialPointScored) 1 else 2
                                    // Haptic count matches points added
                                    increaseScore(by = pointsToAdd, hapticCount = pointsToAdd)
                                    initialPointScored = true
                                    swipeCompleted = true
                                } else {
                                    // Check 3-point hit
                                    val distance3pt = sqrt(
                                        (currentDragPosition.x - threePointPos.x) * (currentDragPosition.x - threePointPos.x) +
                                        (currentDragPosition.y - threePointPos.y) * (currentDragPosition.y - threePointPos.y)
                                    )

                                    if (distance3pt <= hitRadius) {
                                        threePointHit = true
                                        basketballState = com.scoreboard.data.models.BasketballScoringState.TARGET_HIT
                                        // If initial point wasn't scored yet, award full 3 points, otherwise add 2 more
                                        val pointsToAdd = if (initialPointScored) 2 else 3
                                        // Haptic count matches points added
                                        increaseScore(by = pointsToAdd, hapticCount = pointsToAdd)
                                        initialPointScored = true
                                        swipeCompleted = true
                                    }
                                }
                            }

                            if (dragOffset > swipeThreshold && basketballState == com.scoreboard.data.models.BasketballScoringState.INACTIVE) {
                                decreaseScore()
                                swipeCompleted = true
                            }
                        } else {
                            // Standard mode drag handling
                            if (dragOffset < -swipeThreshold) {
                                increaseScore(by = 1)
                                swipeCompleted = true
                            } else if (dragOffset > swipeThreshold) {
                                decreaseScore()
                                swipeCompleted = true
                            }
                        }
                    }
                }
            }
    ) {
        // Score text
        OutlinedText(
            text = "${team.score}",
            fontFamily = JerseyFont,
            fontSize = 175f * density.density,
            textColor = team.fontColor,
            strokeColor = team.secondaryColor,
            strokeWidth = 50f,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, dragOffset.roundToInt()) }
        )

        // Basketball targets overlay
        if (isBasketballMode && showTargets) {
            BasketballTargets(
                side = side,
                twoPointHit = twoPointHit,
                threePointHit = threePointHit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
