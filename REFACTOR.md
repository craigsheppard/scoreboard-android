# Refactoring Recommendations for Scoreboard App

## Introduction

This document provides a formal review of the Scoreboard application codebase. The review focuses on adherence to modern Android architecture principles, including SOLID, DRY, KISS, and YAGNI.

The current implementation is a strong starting point with a clear separation of UI and data models. However, to prepare the application for the Play Store and ensure its long-term health, several key architectural improvements are recommended. These changes will increase robustness, improve testability, and make future development more efficient.

---

## 1. ViewModel Architecture (SOLID - Single Responsibility Principle)

### Deficiency
The current architecture uses a single, monolithic `ScoreboardViewModel` to manage the logic and state for two distinct screens: `ConfigureScreen` and `ScoreboardScreen`. This violates the **Single Responsibility Principle (SRP)**.

- **Coupling:** The configuration logic (e.g., `updateHomeTeam`, `swapTeams`) is tightly coupled with the active game logic (e.g., `updateHomeTeamScore`).
- **State Bloat:** The ViewModel holds state for both screens, meaning the `ScoreboardScreen` can be recomposed due to state changes relevant only to the `ConfigureScreen`, and vice-versa.
- **Maintenance Overhead:** As the app grows, this single file will become increasingly difficult to manage and debug.

### Resolution
Split the `ScoreboardViewModel` into two focused ViewModels, one for each screen.

1.  **Create `ConfigureViewModel.kt`:**
    - This ViewModel will manage the state and business logic for team configuration, game type selection, and saving/loading teams.
    - Move the following functions from `ScoreboardViewModel` to `ConfigureViewModel`:
        - `updateHomeTeam`, `updateAwayTeam`, `swapTeams`, `newGame`, `setGameType`, `saveTeam`, `deleteTeam`, `getTeamsForCurrentGameType`.

2.  **Refactor `ScoreboardViewModel.kt`:**
    - This ViewModel will be responsible only for the active scoreboard.
    - It should retain functions related to in-game actions:
        - `updateHomeTeamScore`, `updateAwayTeamScore`.
    - It will load the final team configurations to display but will not be responsible for editing them.

3.  **Update UI:**
    - Modify `ConfigureScreen.kt` to be powered by an instance of `ConfigureViewModel`.
    - Modify `ScoreboardScreen.kt` to be powered by `ScoreboardViewModel`.
    - Navigation between the two screens will pass the necessary finalized data (e.g., team configurations) as arguments, or the ViewModels can retrieve it from the repository.

---

## 2. Data Layer Abstraction (SOLID - Dependency Inversion)

### Deficiency
The `ScoreboardViewModel` directly instantiates and communicates with `DataStoreManager`. This violates the **Dependency Inversion Principle**, which states that high-level modules (ViewModels) should not depend on low-level modules (Data Sources), but on abstractions. The `repository` package exists but is unused.

- **Lack of Abstraction:** The ViewModel is aware of the specific data source implementation (`DataStore`). If you were to add a cloud backend (like Firestore), you would have to modify the ViewModel extensively.
- **Testability:** It's difficult to unit test the ViewModel without a real `Context` and `DataStore` instance.

### Resolution
Implement the Repository pattern to abstract the data source from the ViewModels.

1.  **Create `ScoreboardRepository.kt` (Interface):**
    - Define an interface in the `data/repository` package that outlines the data operations.
    ```kotlin
    interface ScoreboardRepository {
        fun getAppConfiguration(): Flow<AppConfiguration>
        suspend fun saveAppConfiguration(config: AppConfiguration)
        suspend fun saveSavedTeams(teams: List<SavedTeam>)
    }
    ```

2.  **Create `ScoreboardRepositoryImpl.kt` (Implementation):**
    - Create a concrete implementation of the repository that takes `DataStoreManager` as a dependency.
    ```kotlin
    class ScoreboardRepositoryImpl(private val dataStoreManager: DataStoreManager) : ScoreboardRepository {
        // Implement the interface methods by calling dataStoreManager
    }
    ```

3.  **Update ViewModels:**
    - The ViewModels (`ConfigureViewModel`, `ScoreboardViewModel`) should now depend on the `ScoreboardRepository` interface, not the `DataStoreManager`.
    - This dependency will be provided via Dependency Injection, as described in the next section.

---

## 3. Dependency Injection (SOLID)

### Deficiency
Dependencies like `DataStoreManager` and `ScoreboardViewModel` are instantiated manually within their consumer classes. This form of tight coupling makes the codebase rigid and difficult to test.

- **Manual Instantiation:** `new ScoreboardViewModel(application)` and `DataStoreManager(application)` create hard dependencies.
- **Testing Challenges:** You cannot easily replace `DataStoreManager` with a fake or mock version in tests for the ViewModel or Repository.

### Resolution
Integrate Hilt for dependency injection to manage the lifecycle and provisioning of dependencies automatically.

1.  **Add Hilt Dependencies:**
    - In `build.gradle.kts` (project level), add the Hilt classpath dependency.
    - In `app/build.gradle.kts`, add the Hilt plugins and implementation dependencies.

2.  **Set up Hilt:**
    - Create a custom `Application` class annotated with `@HiltAndroidApp`.
    - Annotate `MainActivity` with `@AndroidEntryPoint`.
    - Annotate the new ViewModels (`ConfigureViewModel`, `ScoreboardViewModel`) with `@HiltViewModel`.

3.  **Create a Hilt Module:**
    - Create a module to provide instances of the `DataStoreManager` and `ScoreboardRepository`.
    ```kotlin
    @Module
    @InstallIn(SingletonComponent::class)
    object AppModule {
        @Provides
        @Singleton
        fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
            return DataStoreManager(context)
        }

        @Provides
        @Singleton
        fun provideScoreboardRepository(dataStoreManager: DataStoreManager): ScoreboardRepository {
            return ScoreboardRepositoryImpl(dataStoreManager)
        }
    }
    ```

4.  **Inject Dependencies:**
    - Use `@Inject constructor(...)` in the ViewModels to receive the `ScoreboardRepository`.

---

## 4. UI State Management & Hardcoded Strings (KISS & DRY)

### Deficiency
1.  **State Handling:** `ConfigureScreen` manually manages its state with `var config by remember { mutableStateOf(viewModel.appConfig) }` and forces updates with `config = viewModel.appConfig`. This is not idiomatic Jetpack Compose and can lead to bugs. State should flow down from the ViewModel via a `StateFlow` and be collected in the UI.
2.  **Hardcoded Strings:** The `ConfigureScreen` contains hardcoded user-facing strings in the `AlertDialog` and for the swap button icon. This violates the **DRY (Don't Repeat Yourself)** principle and prevents internationalization.

### Resolution
1.  **Refine State Management:**
    - In your ViewModels, expose state using `StateFlow`.
    - Create a dedicated `UiState` data class for each screen to avoid over-composition.
    ```kotlin
    // In ConfigureViewModel.kt
    data class ConfigureUiState(
        val homeTeam: TeamConfiguration = TeamConfiguration(),
        val awayTeam: TeamConfiguration = TeamConfiguration(),
        // ... other state
    )

    private val _uiState = MutableStateFlow(ConfigureUiState())
    val uiState: StateFlow<ConfigureUiState> = _uiState.asStateFlow()
    ```
    - In the Composable, collect the state:
    ```kotlin
    // In ConfigureScreen.kt
    val uiState by viewModel.uiState.collectAsState()
    // Use uiState.homeTeam, etc.
    ```

2.  **Externalize Strings:**
    - Move all user-facing text from `.kt` files into `app/src/main/res/values/strings.xml`.
    - **Example:**
        - `"Start a new game? This will reset the score."` -> `<string name="new_game_dialog_message">Start a new game? This will reset the score.</string>`
        - `"⇅ "` -> `<string name="swap_icon">⇅</string>`
    - Reference them in the code using `stringResource(R.string.new_game_dialog_message)`.

---

## Summary of Actions

1.  **Split `ScoreboardViewModel`** into `ConfigureViewModel` and `ScoreboardViewModel`.
2.  **Create a `ScoreboardRepository` interface and implementation** to abstract `DataStoreManager`.
3.  **Integrate Hilt** for dependency injection.
4.  **Refactor ViewModels** to use `@HiltViewModel` and inject the repository.
5.  **Update UI screens** to use their dedicated ViewModels.
6.  **Adopt `StateFlow` and `UiState` data classes** for robust state management in Compose.
7.  **Move all hardcoded strings** to `strings.xml`.
