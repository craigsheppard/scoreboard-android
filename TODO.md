# Scoreboard Android - TODO List

## High Priority

### 1. Complete Configuration UI
- [ ] Create `ConfigureTeamComponent.kt`
  - Color picker for primary, secondary, and font colors
  - Text field for team name
  - "Save Team" button with unsaved changes detection
  - Integration with ViewModel

- [ ] Create `TeamSelectionDialog.kt`
  - Display list of saved teams for current game type
  - "New Team" option
  - Swipe-to-delete functionality
  - Unsaved changes warning

- [ ] Create `GameTypeSelectionDialog.kt`
  - Radio button list of game types
  - Apply selection to ViewModel

### 2. Basketball Target Hit Detection
- [ ] Update `ScoreView.kt` to show `BasketballTargets` when `showTargets` is true
- [ ] Implement hit detection logic:
  - Calculate distance between drag position and target centers
  - Award +1 for 2-point hit (total +2)
  - Award +2 for 3-point hit (total +3)
  - Trigger appropriate haptic feedback

### 3. Firebase Firestore Integration
- [ ] Add `google-services.json` to `/app` directory
- [ ] Create `FirestoreRepository.kt`:
  - `saveTeams()` - Upload teams to Firestore
  - `fetchTeams()` - Download teams from Firestore
  - `setupRealtimeListener()` - Listen for remote changes
  - Merge strategy for conflicts

- [ ] Update `ScoreboardViewModel.kt`:
  - Call Firestore on team save/delete
  - Handle remote change notifications

- [ ] Set up Firestore security rules

## Medium Priority

### 4. App Icon & Branding
- [ ] Design app icon
- [ ] Add launcher icon resources (all densities)
- [ ] Create splash screen

### 5. Polish & UX
- [ ] Add loading indicators
- [ ] Error handling with Snackbars
- [ ] Offline mode indicator
- [ ] Smooth transitions between screens
- [ ] Confirm dialogs for destructive actions

### 6. Testing
- [ ] Test on physical Android device
- [ ] Verify all gestures work correctly
- [ ] Test orientation changes
- [ ] Test Basketball mode thoroughly
- [ ] Test cloud sync (if implemented)
- [ ] Test with different screen sizes

## Low Priority

### 7. Advanced Features
- [ ] Settings screen
- [ ] Export/import teams
- [ ] Game history/statistics
- [ ] Dark mode support
- [ ] Tablet-specific UI

## Known Issues
- Basketball targets are displayed but hit detection not implemented yet
- Configuration screen is a skeleton, needs full team management UI
- No Firebase integration yet (local-only for now)
- Default launcher icon being used

## Files to Create
1. `ConfigureTeamComponent.kt` - Team configuration UI
2. `TeamSelectionDialog.kt` - Saved teams selection
3. `GameTypeSelectionDialog.kt` - Game type picker
4. `FirestoreRepository.kt` - Cloud sync logic

## Files to Update
1. `ScoreView.kt` - Add Basketball target hit detection
2. `ConfigureScreen.kt` - Integrate team components
3. `ScoreboardViewModel.kt` - Add Firebase calls

