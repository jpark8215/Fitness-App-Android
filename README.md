# Jieun Workout Tracker - Developer Documentation

## Project Overview

**Jieun Workout Tracker** is an Android application designed to help users track their workout exercises, monitor progress, and manage fitness routines. The app features a modern Material Design interface with support for both light and dark themes.

### Key Features
- Exercise management and tracking
- Workout session timing with foreground service
- Progress visualization with charts (7-day default view)
- Calendar view for workout history
- Weight unit conversion (kg/lbs) with consistent display
- Theme customization (light/dark mode)
- Exercise archiving system
- Google AdMob integration
- Predictive back gesture support (Android 15+)
- Enhanced chart visibility in dark mode

## Technical Specifications

- **Target SDK**: 35 (Android 15)
- **Minimum SDK**: 23 (Android 6.0)
- **Language**: Java
- **Database**: SQLite
- **Architecture**: Traditional Android with Activities and Services
- **Predictive Back Gesture**: Enabled for Android 15+

## Project Structure

### Core Activities

#### 1. MainActivityExerciseList.java
**Status**: ✅ **ACTIVE** - Main entry point
- **Purpose**: Primary activity for managing exercises
- **Key Features**:
  - Exercise list display with RecyclerView
  - Exercise selection for workouts
  - Add/modify exercise functionality with weight unit toggle
  - Navigation drawer implementation
  - Weight unit display management
  - Predictive back gesture support
  - Enhanced weight unit consistency in dialogs
- **Layout**: `activity_main_exercise_list.xml`
- **Dependencies**: ExerciseRecyclerViewAdapter, DBManager, ThemeManager, WeightUnitManager

#### 2. StartWorkoutActivity.java
**Status**: ✅ **ACTIVE** - Workout execution
- **Purpose**: Manages active workout sessions
- **Key Features**:
  - Real-time workout timing with chronometer
  - Set tracking for exercises
  - Foreground service integration
  - Workout pause/resume functionality
  - Weight unit preference integration
  - Predictive back gesture with exit confirmation
  - Enhanced weight display consistency
- **Layout**: Uses shared layout with ViewStub
- **Dependencies**: WorkoutService, WorkoutRecyclerViewAdapter, WeightUnitManager

#### 3. ShowProgressActivity.java
**Status**: ✅ **ACTIVE** - Progress visualization
- **Purpose**: Displays workout progress charts
- **Key Features**:
  - Weight progress tracking with 7-day default view
  - Chart visualization using MPAndroidChart
  - Date-based progress filtering (M/d/yy format)
  - Dark mode chart visibility improvements
  - Weight unit preference integration
  - Predictive back gesture support
- **Layout**: `activity_progress.xml`
- **Dependencies**: MPAndroidChart library, WeightUnitManager

#### 4. ShowCalendarActivity.java
**Status**: ✅ **ACTIVE** - Calendar view
- **Purpose**: Calendar interface for workout history
- **Key Features**:
  - Monthly calendar view
  - Workout history display
  - Date-based navigation
- **Layout**: `activity_calendar.xml`
- **Dependencies**: CalendarRecyclerViewAdapter

#### 5. CalendarShowSelectedWorkout.java
**Status**: ✅ **ACTIVE** - Calendar detail view
- **Purpose**: Shows detailed workout information for selected dates
- **Key Features**:
  - Exercise details for specific dates
  - Workout summary display
- **Layout**: Uses shared layout system

#### 6. ArchivedExerciseList.java
**Status**: ✅ **ACTIVE** - Archive management
- **Purpose**: Manages archived exercises
- **Key Features**:
  - View archived exercises
  - Restore exercises from archive
  - Archive management interface
  - Weight unit preference integration in dialogs
  - Enhanced weight unit consistency
- **Layout**: Uses shared layout system
- **Dependencies**: WeightUnitManager

#### 7. ColorSchemeActivity.java
**Status**: ✅ **ACTIVE** - Theme settings
- **Purpose**: Theme and color scheme management
- **Key Features**:
  - Light/dark theme switching
  - Color scheme customization
  - Theme persistence
  - Weight unit preference (kg/lbs) selection
  - Real-time preference updates
- **Layout**: `activity_color_scheme_screen.xml`
- **Dependencies**: ThemeManager, WeightUnitManager

### Data Layer

#### DatabaseHelper.java
**Status**: ✅ **ACTIVE** - Database schema
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
**Status**: ✅ **ACTIVE** - Database operations
- **Purpose**: Database CRUD operations
- **Key Methods**:
  - `getAllExercises()`: Fetch all exercises with latest weights
  - `getExerciseLogProgress()`: Get progress data for charts
  - `startSelectedExercises()`: Create workout session
  - `updateExerciseLogs()`: Update exercise sets and reps
  - `archiveExercise()`: Archive/unarchive exercises

### Services

#### WorkoutService.java
**Status**: ✅ **ACTIVE** - Foreground service
- **Purpose**: Manages workout timing in background
- **Key Features**:
  - Foreground service with notification
  - Chronometer for workout timing
  - Service binding for activity communication
- **Permissions**: FOREGROUND_SERVICE

### Adapters

#### ExerciseRecyclerViewAdapter.java
**Status**: ✅ **ACTIVE** - Exercise list adapter
- **Purpose**: Manages exercise list display
- **Key Features**:
  - Exercise selection functionality
  - Set tracking with buttons
  - Read-only mode support
  - Weight display formatting

#### WorkoutRecyclerViewAdapter.java
**Status**: ✅ **ACTIVE** - Workout session adapter
- **Purpose**: Manages active workout display
- **Key Features**:
  - Real-time set tracking
  - Button interaction for reps
  - Progress visualization

#### CalendarRecyclerViewAdapter.java
**Status**: ✅ **ACTIVE** - Calendar adapter
- **Purpose**: Manages calendar view display
- **Key Features**:
  - Date-based workout display
  - Calendar item formatting

### Data Models

#### ExerciseItem.java
**Status**: ✅ **ACTIVE** - Exercise data model
- **Purpose**: Represents exercise data
- **Properties**:
  - Exercise ID, title, weight
  - Set buttons (1-5) with colors
  - Weight display formatting
- **Dependencies**: WeightUtils

#### ExerciseSession.java
**Status**: ❓ **POTENTIALLY UNUSED**
- **Purpose**: Exercise session data model
- **Note**: No direct imports found in codebase

### Utility Classes

#### ThemeManager.java
**Status**: ✅ **ACTIVE** - Theme management
- **Purpose**: Manages app theme switching
- **Key Features**:
  - Light/dark mode switching
  - Theme persistence
  - SharedPreferences integration

#### WeightUnitManager.java
**Status**: ✅ **ACTIVE** - Weight unit management
- **Purpose**: Manages weight unit conversion
- **Key Features**:
  - kg/lbs conversion
  - Unit preference persistence
  - Weight formatting

#### WeightUtils.java
**Status**: ✅ **ACTIVE** - Weight utility functions
- **Purpose**: Weight formatting utilities
- **Dependencies**: Used by ExerciseItem

#### ViewAnimation.java
**Status**: ✅ **ACTIVE** - Animation utilities
- **Purpose**: Provides animation functions
- **Usage**: FAB menu animations

### Unused/Deprecated Files

The following files appear to be unused in the current codebase:

#### 1. Item.java
**Status**: ❌ **UNUSED**
- **Purpose**: Generic item data model
- **Issue**: No imports found, superseded by ExerciseItem

#### 2. ProgressItem.java
**Status**: ❌ **UNUSED**
- **Purpose**: Progress data model
- **Issue**: No imports found, likely replaced by direct database queries

#### 3. CalendarItem.java
**Status**: ❌ **UNUSED**
- **Purpose**: Calendar item data model
- **Issue**: No imports found, calendar functionality uses direct database queries

#### 4. DayAxisValueFormatter.java
**Status**: ✅ **ACTIVE** - Chart axis formatting
- **Purpose**: Chart axis formatting for progress charts
- **Key Features**:
  - M/d/yy date format display
  - Support for 2025-2034 date range
  - Proper date conversion for chart X-axis
- **Dependencies**: MPAndroidChart library

#### 5. MergeAdapter.java
**Status**: ❌ **UNUSED**
- **Purpose**: Adapter merging utility
- **Issue**: No imports found, not implemented in current UI

#### 6. RecyclerViewAdapter.java
**Status**: ❌ **UNUSED**
- **Purpose**: Generic RecyclerView adapter
- **Issue**: No imports found, specific adapters used instead

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

## Recent Updates (v4.1)

### Major Improvements

#### 1. **Predictive Back Gesture Support**
- Enabled `android:enableOnBackInvokedCallback="true"` in AndroidManifest.xml
- Implemented `OnBackPressedCallback` in all main activities
- Added proper drawer handling and exit confirmations
- Enhanced navigation experience for Android 15+ devices

#### 2. **Enhanced Progress Charts**
- **7-Day Default View**: Charts now show 7 days by default for better focus
- **Improved X-Axis Formatting**: Changed to M/d/yy format (e.g., "1/15/25")
- **Dark Mode Visibility**: Fixed chart text colors for dark mode
- **Date Range Support**: Updated to support 2025-2034 date range
- **Better Chart Styling**: Enhanced grid lines, colors, and visibility

#### 3. **Weight Unit Consistency**
- **Settings Integration**: Weight unit preference now consistently applied across all activities
- **Dialog Improvements**: Add/modify exercise dialogs respect user's weight unit preference
- **Real-time Conversion**: Weight values convert in real-time when toggling units
- **Workout Display**: Exercise weights in workout screen now match user's preference
- **Progress Charts**: Chart Y-axis labels show correct weight units

#### 4. **Enhanced User Experience**
- **Exit Confirmation**: Added confirmation dialog when exiting active workouts
- **Improved Navigation**: Better back gesture handling with drawer support
- **Theme Consistency**: Enhanced dark mode support across all components
- **Error Handling**: Improved error handling and user feedback

### Technical Improvements

#### 1. **Updated Dependencies**
- Target SDK updated to 35 (Android 15)
- Minimum SDK updated to 23 (Android 6.0)
- Enhanced compatibility with latest Android features

#### 2. **Code Quality**
- Removed unused files and improved code organization
- Enhanced error handling and logging
- Improved weight unit conversion utilities
- Better separation of concerns

#### 3. **Performance Optimizations**
- Optimized chart rendering and data loading
- Improved database query efficiency
- Enhanced memory management for large datasets

## Development Notes

### Code Quality Issues
1. **Hardcoded Values**: Some UI elements use hardcoded values
2. **Error Handling**: Limited error handling in database operations
3. **Code Duplication**: Some adapter code could be consolidated

### Recommended Improvements
1. **Implement MVVM**: Consider migrating to MVVM architecture
2. **Add Unit Tests**: Implement comprehensive testing
3. **Error Handling**: Improve error handling and user feedback
4. **Code Documentation**: Add more comprehensive code comments
5. **Cloud Sync**: Add cloud backup and sync functionality

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
- **Version Code**: 11
- **Last Updated**: December 2024
- **Status**: Active Development

### Known Issues
1. Limited error handling in some database operations
2. No comprehensive testing suite
3. Some hardcoded values in UI components

### Future Enhancements
1. **Cloud Sync**: Google Drive or Firebase integration for backup
2. **Social Features**: Share workouts and achievements
3. **Advanced Analytics**: Detailed progress insights and trends
4. **Workout Templates**: Pre-defined workout routines
5. **Fitness Device Integration**: Connect with wearables and smart devices
6. **Export Features**: CSV/PDF workout reports
7. **Reminder System**: Workout scheduling and notifications 