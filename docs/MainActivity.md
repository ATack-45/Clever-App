# MainActivity

## Overview
`MainActivity` serves as the main entry point for the app. It handles initialization, sets up necessary services (e.g., Spotify, Google APIs, student tracking), and manages threading for specific operations. The activity also starts a local web server for API interactions and controls app orientation to landscape mode for a larger display experience.

## Key Variables

### Instantiating Other Classes
- `spotifyManager`: An instance of the `SpotifyManager` class, responsible for managing Spotify-related functionality.
- `utils`: An instance of the `Utils` class, which provides various utility functions (e.g., loading banned lists).
- `googleApiManager`: An instance of the `GoogleApiManager` class, handling communication with Google APIs (e.g., Google Slides, Google Calendar).
- `studentTracker`: An instance of the `StudentTracker` class, which tracks student data and attendance.
- `webServer`: An instance of the `WebServer` class, responsible for running a web server on a specific port and providing API endpoints for external communication.

### Thread Tracking - `funs` Mutable Map
- `funs`: A mutable map that stores function names as keys and their corresponding actions as values. This is used for managing background tasks (e.g., fetching student data, starting Spotify, etc.).
  - `"fetchTodayDocument"`: Calls the `googleApiManager.fetchTodayDocument()` function.
  - `"fetchStudentData"`: Calls the `studentTracker.fetchStudentData()` function.
  - `"startSpotify"`: Calls the `spotifyManager.startSpotifyAuthorization(override)` function.
  - `"fetchCalendar"`: Calls the `googleApiManager.fetchCalendarEvents()` function.

## Key Responsibilities
- Manages the lifecycle of the app, initializing APIs and services upon creation.
- Launches background threads for specific operations (e.g., fetching student data, interacting with Google APIs, starting Spotify).
- Initializes and manages a web server that interacts with the app and provides API access to external requests.
- Handles thread management for restarting certain functions in case of failures.

## Key Methods/Functions

### `onCreate(savedInstanceState: Bundle?)`
- **Description**: Initializes the activity, sets the layout, requests landscape orientation, and starts the necessary services and background tasks.
- **Functionality**:
    - Sets up the layout with `setContentView(R.layout.activity_main)`.
    - Requests landscape orientation (`setRequestedOrientation`).
    - Starts important background tasks like fetching student data, documents, and calendar events.
    - Initializes the web server and passes required functions for interaction.

### `restartFun(funId: String, override: Boolean = false)`
- **Description**: Manages restarting specific functions in case of failures.
- **Functionality**: Stops the existing thread for a given function and restarts it with an optional override parameter to control execution.
- **Parameters**:
    - `funId`: The function identifier (e.g., `"fetchTodayDocument"`, `"fetchStudentData"`).
    - `override`: A boolean indicating if the function should override the current execution.
  
### `startFun(s: String, override: Boolean = false)`
- **Description**: Starts the function corresponding to the provided identifier (`funId`).
- **Functionality**: Finds the appropriate function in the `funs` map and executes it, passing the `override` parameter if needed.

### `varUpdate(id: String, type: String, googleManager: GoogleApiManager): Boolean`
- **Description**: Updates Google API-related variables (e.g., slide ID, calendar ID) based on the provided type.
- **Functionality**: 
    - Handles different types of updates: Google Slideshow ID, Slide ID, and Calendar ID.
    - Saves or removes relevant data through the `GoogleApiManager`.
- **Parameters**:
    - `id`: The ID to be saved or removed.
    - `type`: The type of update to perform (`Google Slideshow ID`, `Google Slide ID`, `Google Calendar ID`, or `removeCalendarIds`).
    - `googleManager`: The `GoogleApiManager` instance to handle the update.

### `onNewIntent(intent: Intent)`
- **Description**: Handles new intents, specifically for Spotify authorization.
- **Functionality**: 
    - Extracts the authorization code from the intent's data.
    - If successful, exchanges the authorization code for an access token and starts track updates.
    - Displays the currently playing track using a `TextView`.
