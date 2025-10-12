# Scoreboard Android

An Android port of the iOS Scoreboard app for tracking scores in two-team games.

## Features

- Track scores for Hockey, Basketball, Soccer, and Table Tennis
- Customizable team colors and fonts
- Tap and swipe gestures for scoring
- Special Basketball mode with 2-point and 3-point targets
- Save and manage teams locally
- Haptic feedback with multiple vibrations for multi-point scores
- Visual flash feedback on score changes
- Jersey M54 font for authentic sports scoreboard appearance

## Build Requirements

- Java 17 (required for Kotlin 2.0.21)
- Android Studio Hedgehog or later
- Android SDK 26-35 (minSdk 26, targetSdk 35)
- Kotlin 2.0.21
- Gradle 8.13

## Setup

1. Install Java 17 (if not already installed):
   ```bash
   brew install openjdk@17
   ```
2. Open project in Android Studio
3. Sync Gradle
4. Run on device or emulator

## Architecture

- **UI:** Jetpack Compose with Material Design 3
- **State Management:** ViewModel + StateFlow
- **Local Storage:** DataStore
- **Pattern:** MVVM
- **Custom Rendering:** Canvas-based text rendering with stroke outlines
