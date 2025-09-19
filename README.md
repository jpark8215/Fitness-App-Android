# Workout!

## Project Overview

**Workout!** is an Android application designed to help users track their workout exercises, monitor progress, and manage fitness routines. The app features a modern Material Design interface with support for both light and dark themes.

### Key Features
- Exercise management and tracking
- Workout session timing with foreground service and notification integration
- Progress visualization with charts (7-day default view)
- Calendar view for workout history (no session grouping)
- Weight unit conversion (kg/lbs) with consistent display
- Theme customization (light/dark mode)
- Exercise archiving system
- Google AdMob integration
- Predictive back gesture support (Android 15+)
- Enhanced chart visibility in dark mode
- Smart notification system for ongoing workouts
- Disabled long-click modify during active workouts

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
- **Purpose**: Manages active workout sessions
- **Key Features**:
  - Real-time workout timing with chronometer
  - Set tracking for exercises (1-5 sets with color coding)
  - Foreground service integration with smart notification
  - Workout pause/resume functionality
  - Weight unit preference integration
  - Predictive back gesture with exit confirmation
  - Enhanced weight display consistency
  - Disabled long-click modify during workouts
  - Notification integration for workout resumption
  - Welcome back message for resumed workouts
- **Layout**: Uses shared layout with ViewStub
- **Dependencies**: WorkoutService, WorkoutRecyclerViewAdapter, WeightUnitManager
- **Recent Updates**:
  - Long-click modify dialog disabled during active workouts
  - Enhanced notification system for workout resumption
  - Improved workout data persistence

#### 3. ShowProgressActivity.java
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
- **Purpose**: Calendar interface for workout history
- **Key Features**:
  - Monthly calendar view
  - Workout history display with time and duration
  - Date-based navigation
  - Simplified exercise list (no session grouping)
- **Layout**: `activity_calendar.xml`
- **Dependencies**: CalendarRecyclerViewAdapter
- **Recent Updates**:
  - Removed session grouping (morning/afternoon/evening)
  - Simplified exercise display with time and duration
  - Cleaner calendar interface

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
  - Weight unit preference integration in dialogs
  - Enhanced weight unit consistency
- **Layout**: Uses shared layout system
- **Dependencies**: WeightUnitManager

#### 7. ColorSchemeActivity.java
- **Purpose**: Theme and color scheme management
- **Key Features**:
  - Light/dark theme switching
  - Color scheme customization
  - Theme persistence
  - Weight unit preference (kg/lbs) selection
  - Real-time preference updates
- **Layout**: `activity_color_scheme_screen.xml`
- **Dependencies**: ThemeManager, WeightUnitManager

#### 8. DayAxisValueFormatter.java
- **Purpose**: Chart axis formatting for progress charts
- **Key Features**:
  - M/d/yy date format display
  - Support for 2025-2034 date range
  - Proper date conversion for chart X-axis
- **Dependencies**: MPAndroidChart library

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
  - `updateExerciseLogsWithImprovement()`: Update exercise sets and reps with improvement tracking
  - `archiveExercise()`: Archive/unarchive exercises
  - `recordExerciseLogDuration()`: Record workout duration

### Services

#### WorkoutService.java
- **Purpose**: Manages workout timing in background
- **Key Features**:
  - Foreground service with smart notification
  - Chronometer for workout timing
  - Service binding for activity communication
  - Workout data persistence for notification resumption
- **Permissions**: FOREGROUND_SERVICE
- **Recent Updates**:
  - Enhanced notification with workout data
  - Improved service lifecycle management
  - Better workout state persistence

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
  - Progress visualization with color coding
  - Configurable long-click listener (disabled during workouts)

#### CalendarRecyclerViewAdapter.java
- **Purpose**: Manages calendar view display
- **Key Features**:
  - Date-based workout display
  - Calendar item formatting
  - Simplified exercise list display

### Data Models

#### ExerciseItem.java
- **Purpose**: Represents exercise data
- **Properties**:
  - Exercise ID, title, weight
  - Set buttons (1-5) with colors (green/red/default)
  - Weight display formatting
  - Improvement tracking
- **Dependencies**: WeightUtils

#### CalendarItem.java
- **Purpose**: Calendar display data model
- **Properties**:
  - Exercise title with time and duration
  - Workout ID and log ID
  - Date information

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
- **Purpose**: Weight conversion utilities
- **Key Features**:
  - kg to lbs conversion
  - lbs to kg conversion
  - Weight formatting with units

#### ViewAnimation.java
- **Purpose**: Animation utilities for UI elements
- **Key Features**:
  - FAB rotation animations
  - View show/hide animations

## Recent Updates and Improvements

### Notification System Enhancement
- **Smart Workout Resumption**: Notification now properly resumes ongoing workouts with all exercise data
- **Workout Data Persistence**: Service stores workout information for seamless resumption
- **Enhanced User Experience**: Welcome back message when resuming from notification

### Calendar Interface Simplification
- **Removed Session Grouping**: Calendar no longer groups exercises by time of day
- **Cleaner Display**: Simple list of exercises with time and duration
- **Better Performance**: Reduced complexity in calendar data processing

### Workout Session Improvements
- **Disabled Long-Click Modify**: Users cannot modify exercises during active workouts
- **Focus on Completion**: Interface optimized for workout completion rather than modification
- **Enhanced Exit Confirmation**: Improved dialog for workout exit with progress saving

### Technical Improvements
- **Service Lifecycle**: Better service management for seamless resumption
- **Data Consistency**: Improved weight unit handling across all dialogs
- **Error Handling**: Enhanced error handling and logging throughout the app

## Development Guidelines

### Code Style
- Follow Android development best practices
- Use meaningful variable and method names
- Implement proper error handling
- Add logging for debugging purposes

### Database Operations
- Always close cursors after use
- Use parameterized queries to prevent SQL injection
- Implement proper transaction handling for complex operations

### UI/UX Considerations
- Support both light and dark themes
- Implement proper weight unit display
- Use consistent color coding for exercise progress
- Provide clear user feedback for actions

### Performance Optimization
- Use RecyclerView for large lists
- Implement proper view recycling
- Minimize database queries
- Use background services appropriately

## Future Enhancements

### Planned Features
- Workout templates and routines
- Advanced progress analytics
- Social features and sharing
- Integration with fitness devices
- Cloud backup and sync

### Technical Improvements
- Migration to modern Android architecture components
- Enhanced offline capabilities
- Improved accessibility features
- Performance optimizations

## Support and Maintenance

### Debugging
- Use Android Studio's built-in debugging tools
- Check logcat for error messages
- Verify database integrity
- Test on multiple Android versions

### Testing
- Test on various screen sizes
- Verify theme switching functionality
- Test weight unit conversion accuracy
- Validate workout timing accuracy

### Deployment
- Ensure proper signing configuration
- Test release builds thoroughly
- Verify all permissions are correctly declared
- Check for memory leaks and performance issues

