## Project Overview

**Jieun Workout Tracker** is an Android application designed to help users track their workout exercises, monitor progress, and manage fitness routines. The app features a modern Material Design interface with support for both light and dark themes.

### Key Features
- Exercise management and tracking
- Workout session timing with foreground service
- Progress visualization with charts
- Calendar view for workout history
- Weight unit conversion (kg/lbs)
- Theme customization (light/dark mode)
- Exercise archiving system
- Google AdMob integration

## Technical Specifications

- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 21 (Android 5.0)
- **Language**: Java
- **Database**: SQLite
- **Architecture**: Traditional Android with Activities and Services

## Project Structure

### Core Activities

#### 1. MainActivityExerciseList.java
- **Purpose**: Primary activity for managing exercises
- **Key Features**:
  - Exercise list display with RecyclerView
  - Exercise selection for workouts
  - Add/modify exercise functionality
  - Navigation drawer implementation
  - Weight unit display management
- **Layout**: `activity_main_exercise_list.xml`
- **Dependencies**: ExerciseRecyclerViewAdapter, DBManager, ThemeManager

#### 2. StartWorkoutActivity.java
- **Purpose**: Manages active workout sessions
- **Key Features**:
  - Real-time workout timing with chronometer
  - Set tracking for exercises
  - Foreground service integration
  - Workout pause/resume functionality
- **Layout**: Uses shared layout with ViewStub
- **Dependencies**: WorkoutService, WorkoutRecyclerViewAdapter

#### 3. ShowProgressActivity.java
- **Purpose**: Displays workout progress charts
- **Key Features**:
  - Weight progress tracking
  - Chart visualization using MPAndroidChart
  - Date-based progress filtering
- **Layout**: `activity_progress.xml`
- **Dependencies**: MPAndroidChart library

#### 4. ShowCalendarActivity.java
- **Purpose**: Calendar interface for workout history
- **Key Features**:
  - Monthly calendar view
  - Workout history display
  - Date-based navigation
- **Layout**: `activity_calendar.xml`
- **Dependencies**: CalendarRecyclerViewAdapter

#### 5. CalendarShowSelectedWorkout.java
- **Purpose**: Shows detailed workout information for selected dates
- **Key Features**:
  - Exercise details for specific dates
  - Workout summary display
- **Layout**: Uses shared layout system

#### 6. ArchivedExerciseList.java
- **Purpose**: Manages archived exercises
- **Key Features**:
  - View archived exercises
  - Restore exercises from archive
  - Archive management interface
- **Layout**: Uses shared layout system

#### 7. ColorSchemeActivity.java
- **Purpose**: Theme and color scheme management
- **Key Features**:
  - Light/dark theme switching
  - Color scheme customization
  - Theme persistence
- **Layout**: `activity_color_scheme_screen.xml`
- **Dependencies**: ThemeManager

### Data Layer

#### DatabaseHelper.java
- **Purpose**: SQLite database schema definition
- **Tables**:
  - `WORKOUTS`: Workout definitions
  - `EXERCISES`: Exercise definitions
  - `LOGS`: Exercise session logs
- **Version**: 23
- **Key Features**:
  - Automatic migration support
  - Archive column support

#### DBManager.java
- **Purpose**: Database CRUD operations
- **Key Methods**:
  - `getAllExercises()`: Fetch all exercises with latest weights
  - `getExerciseLogProgress()`: Get progress data for charts
  - `startSelectedExercises()`: Create workout session
  - `updateExerciseLogs()`: Update exercise sets and reps
  - `archiveExercise()`: Archive/unarchive exercises

### Services

#### WorkoutService.java
- **Purpose**: Manages workout timing in background
- **Key Features**:
  - Foreground service with notification
  - Chronometer for workout timing
  - Service binding for activity communication
- **Permissions**: FOREGROUND_SERVICE

### Adapters

#### ExerciseRecyclerViewAdapter.java
- **Purpose**: Manages exercise list display
- **Key Features**:
  - Exercise selection functionality
  - Set tracking with buttons
  - Read-only mode support
  - Weight display formatting

#### WorkoutRecyclerViewAdapter.java
- **Purpose**: Manages active workout display
- **Key Features**:
  - Real-time set tracking
  - Button interaction for reps
  - Progress visualization

#### CalendarRecyclerViewAdapter.java
- **Purpose**: Manages calendar view display
- **Key Features**:
  - Date-based workout display
  - Calendar item formatting

### Data Models

#### ExerciseItem.java
- **Purpose**: Represents exercise data
- **Properties**:
  - Exercise ID, title, weight
  - Set buttons (1-5) with colors
  - Weight display formatting
- **Dependencies**: WeightUtils

#### ExerciseSession.java
- **Purpose**: Exercise session data model
- **Note**: No direct imports found in codebase

### Utility Classes

#### ThemeManager.java
- **Purpose**: Manages app theme switching
- **Key Features**:
  - Light/dark mode switching
  - Theme persistence
  - SharedPreferences integration

#### WeightUnitManager.java
- **Purpose**: Manages weight unit conversion
- **Key Features**:
  - kg/lbs conversion
  - Unit preference persistence
  - Weight formatting

#### WeightUtils.java
- **Purpose**: Weight formatting utilities
- **Dependencies**: Used by ExerciseItem

#### ViewAnimation.java
- **Purpose**: Provides animation functions
- **Usage**: FAB menu animations

### Deprecated Files

The following files appear to be unused in the current codebase:

#### 1. Item.java
- **Purpose**: Generic item data model

#### 2. ProgressItem.java
- **Purpose**: Progress data model

#### 3. CalendarItem.java
- **Purpose**: Calendar item data model

#### 4. DayAxisValueFormatter.java
- **Purpose**: Chart axis formatting

#### 5. MergeAdapter.java
- **Purpose**: Adapter merging utility

#### 6. RecyclerViewAdapter.java
- **Purpose**: Generic RecyclerView adapter

## Database Schema

### Tables

#### WORKOUTS
```sql
CREATE TABLE WORKOUTS (
    workout_id INTEGER PRIMARY KEY AUTOINCREMENT,
    workout TEXT NOT NULL,
    archive INTEGER
);
```

#### EXERCISES
```sql
CREATE TABLE EXERCISES (
    exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
    workout_id INTEGER NOT NULL,
    exercise TEXT,
    archive INTEGER DEFAULT 0
);
```

#### LOGS
```sql
CREATE TABLE LOGS (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    exercise_id INTEGER NOT NULL,
    workout_id INTEGER NOT NULL,
    set1 INTEGER, set1_improvement INTEGER,
    set2 INTEGER, set2_improvement INTEGER,
    set3 INTEGER, set3_improvement INTEGER,
    set4 INTEGER, set4_improvement INTEGER,
    set5 INTEGER, set5_improvement INTEGER,
    weight DOUBLE,
    date DATE,
    datetime DEFAULT CURRENT_TIMESTAMP,
    duration TIME,
    notes TEXT
);
```

## Key Dependencies

### External Libraries
- **Google AdMob**: Advertisement integration
- **MPAndroidChart**: Progress chart visualization
- **AndroidX**: Modern Android support libraries
- **Material Design Components**: UI components

### Internal Dependencies
- **ThemeManager**: Theme switching across activities
- **WeightUnitManager**: Weight conversion and formatting
- **DBManager**: Database operations
- **ViewAnimation**: UI animations

## Configuration

### Permissions
- `INTERNET`: AdMob integration
- `FOREGROUND_SERVICE`: Workout timing service

### Build Configuration
- **Application ID**: `com.developerjp.jieunworkouttracker`
- **Version**: 4.1
- **Version Code**: 11

## Development Notes

### Code Quality Issues
1. **Unused Files**: Several data model classes are not being used
2. **Hardcoded Values**: Some UI elements use hardcoded values
3. **Error Handling**: Limited error handling in database operations
4. **Code Duplication**: Some adapter code could be consolidated

### Recommended Improvements
1. **Remove Unused Files**: Clean up unused Java classes
2. **Implement MVVM**: Consider migrating to MVVM architecture
3. **Add Unit Tests**: Implement comprehensive testing
4. **Error Handling**: Improve error handling and user feedback
5. **Code Documentation**: Add more comprehensive code comments

### Performance Considerations
1. **Database Queries**: Some queries could be optimized
2. **Memory Management**: Large lists should implement pagination
3. **Service Lifecycle**: Ensure proper service cleanup

## Build and Deployment

### Building the App
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

### Release Configuration
- **Signing**: Configured for release builds
- **ProGuard**: Enabled for code obfuscation
- **AdMob**: Integrated for monetization

## Support and Maintenance

### Current Version
- **Version**: 4.1
- **Last Updated**: Current
- **Status**: Active Development

### Known Issues
1. Some unused files in codebase
2. Limited error handling
3. No comprehensive testing suite

### Future Enhancements
1. Cloud sync functionality
2. Social features
3. Advanced analytics
4. Workout templates
5. Integration with fitness devices 
