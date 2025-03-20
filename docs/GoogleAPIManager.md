# GoogleApiManager Class

## Overview
The `GoogleApiManager` class handles interactions with Google's APIs, such as Google Calendar and Google Slides. It is responsible for managing authentication, retrieving calendar events, and handling slides' thumbnails. The class also provides functionality for saving and retrieving calendar and presentation IDs, as well as controlling the collapsible state of the calendar UI card.

## Variables

### `fetchTodayThread: Thread`
A thread that periodically fetches the thumbnail of the current slide in the Google Slides presentation every 10 minutes and updates the background.

### `fetchCalendarThread: Thread`
A thread that fetches calendar events from Google Calendar and updates the UI with event information for today and the next 2 days.

### `isCollapsed: Boolean`
A boolean indicating whether the calendar card is collapsed or expanded.

### `isDragging: Boolean`
A boolean tracking whether the calendar card is being dragged.

### `serviceAccount: ServiceAccountCredentials`
The credentials for the Google API service account used for authenticating API requests.

### `calendarIds: List<String>`
A list of Google Calendar IDs stored in `SharedPreferences` for fetching events.

### `presentationId: String?`
The ID of the Google Slides presentation stored in `SharedPreferences`.

### `pageObjectId: String?`
The ID of the specific page object (slide) within the Google Slides presentation, stored in `SharedPreferences`.

---

## Functions

### `saveCalendarIds(calendarIds: List<String>)`
**Description**: Saves a list of Google Calendar IDs to `SharedPreferences`.  
**Parameters**:  
- `calendarIds`: A list of `String` containing the Google Calendar IDs.  
**Example**:  
```kotlin
googleApiManager.saveCalendarIds(listOf("calendar1", "calendar2"))
```
### `savePresentationId(presentationId: String)`

**Description**: Saves the Google Slides presentation ID to SharedPreferences.  
**Parameters**:  
- `presentationId`: A `String` representing the Google Slides presentation ID.  
**Example**:  
```kotlin
googleApiManager.savePresentationId("presentation_id")
```

### `savePageObjectId(pageObjectId: String)`

**Description**: Saves the Google Slides page object ID to SharedPreferences.\
**Parameters**:

-   `pageObjectId`: A String representing the page object ID.\
    Example:
    ```kotlin
    googleApiManager.savePageObjectId("page_object_id")
    ```



### `getCalendarIds(): List<String>`

**Description**: Retrieves the list of Google Calendar IDs stored in SharedPreferences.\
**Returns**: A list of String containing the Google Calendar IDs.\
Example:
```kotlin
val calendarIds = googleApiManager.getCalendarIds()
```


### `getPresentationId(): String?`

**Description**: Retrieves the Google Slides presentation ID stored in SharedPreferences.\
**Returns**: A `String?` representing the presentation ID or null if not set.\
Example:
```kotlin
val presentationId = googleApiManager.getPresentationId()
```


### `getPageObjectId(): String?`

**Description**: Retrieves the Google Slides page object ID stored in SharedPreferences.\
**Returns**: A `String?` representing the page object ID or null if not set.\
Example:
```kotlin
val pageObjectId = googleApiManager.getPageObjectId()
```


### `fetchTodayDocument(): Thread`

**Description**: Fetches the thumbnail of the current slide in the Google Slides presentation every 10 minutes and updates the app's background.\
**Returns**: A Thread that performs the background task of fetching and updating the document.\
Example:
```kotlin
val fetchThread = googleApiManager.fetchTodayDocument()
```


### `fetchCalendarEvents(): Thread`

**Description**: Fetches calendar events for today and the next 2 days from the specified Google Calendar IDs and updates the UI.\
**Returns**: A Thread that performs the background task of fetching and displaying events.\
Example:
```kotlin
val fetchCalendarThread = googleApiManager.fetchCalendarEvents()
```


### `updateEventCard(dateLabel: String, eventSummary: String?, eventStartTime: String?, formattedDate: String)`

**Description**: Updates the UI with event information for a given date.
Parameters:

-   dateLabel: A String representing the label for the date (e.g., "Today", "Tomorrow").
-   eventSummary: A String? representing the event summary.
-   eventStartTime: A String? representing the event start time.
-   formattedDate: A String representing the formatted date.\
    Example:
    ```kotlin
    googleApiManager.updateEventCard("Today", "Event Title", "10:00 AM", "Feb 4")
    ```


### `makeCalendarCardCollapsible()`

**Description**: Toggles the collapsible state of the calendar card UI. When collapsed, only the header is visible; when expanded, the full calendar content is shown.\
Example:
```kotlin
googleApiManager.makeCalendarCardCollapsible()
```


### Example Usage

Here’s an example of how you can use the GoogleApiManager in your MainActivity:
```
class MainActivity : AppCompatActivity() {
private lateinit var googleApiManager: GoogleApiManager

override fun onCreate(savedInstanceState: Bundle?) {  
    super.onCreate(savedInstanceState)  
    setContentView(R.layout.activity_main)  

    googleApiManager = GoogleApiManager(this)  

    // Example: Save calendar IDs  
    val calendarIds = listOf("calendar1", "calendar2")  
    googleApiManager.saveCalendarIds(calendarIds)  

    // Example: Save presentation ID  
    googleApiManager.savePresentationId("presentation_id")  

    // Fetch and display calendar events  
    googleApiManager.fetchCalendarEvents()  

    // Fetch the thumbnail for today's slide  
    googleApiManager.fetchTodayDocument()  
}  

}
```