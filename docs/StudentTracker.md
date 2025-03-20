Overview
--------

The `StudentTracker` class manages student check-in and check-out data, integrating with an API and handling real-time updates using SignalR. It maintains a UI with three slots to track checked-out students and provides API endpoints for fetching student data.

Data Classes
------------

-   **APIStudent**: Represents a student retrieved from the API, including first name, last name, ID, NFC ID, and punch-out time.

    -   Example: `APIStudent("John", "Doe", "123", "A1B2C3", "2025-02-03T17:21:37.647")`
-   **Student**: Represents a student within the UI, including their name and the time they have been gone.

    -   Example: `Student("Jane Doe", 300)`

StudentAdapter
--------------

Handles the UI representation of checked-out students.

### Properties

-   **cardTextViews**: A list of `TextView` elements representing student slots.
-   **students**: An array storing up to three checked-out students.
-   **timers**: An array of coroutines that track how long each student has been checked out.
-   **checkedOutColor**: Color used for checked-out students.
-   **defaultColor**: Default background color for cards.

### Functions

-   **checkOutStudent(studentName: String, initialTimeGone: Int = 0)**\
    Adds a student to the first available slot and starts a timer to track time gone.

    -   Example: `studentAdapter.checkOutStudent("Alice", 120)`
-   **checkInStudent(studentName: String)**\
    Removes a student, shifts remaining students to the left, and stops the timer.

    -   Example: `studentAdapter.checkInStudent("Alice")`
-   **updateCard(index: Int, student: Student?)**\
    Updates the UI to display student names and time gone.

    -   Example: `updateCard(0, Student("Bob", 60))`
-   **formatTime(seconds: Int): String**\
    Converts elapsed time into a minutes and seconds format.

    -   Example: `formatTime(125)` → Returns `"2:05"`
-   **clearAll()**\
    Resets all student slots and timers.

    -   Example: `studentAdapter.clearAll()`

StudentTracker
--------------

Manages data retrieval, API interactions, and real-time updates using SignalR.

### Properties

-   **fetchStudentThread**: Background thread handling student data retrieval.

### Functions

-   **fetchStudentData(): Thread**\
    Starts a new thread to fetch currently checked-out students from the API, updates the UI, and listens for real-time updates via SignalR.

    -   Example: `val thread = studentTracker.fetchStudentData()`
-   **getStudents(): NanoHTTPD.Response**\
    Fetches all students from the API and returns them as a JSON response.

    -   Example: `val response = studentTracker.getStudents()`
-   **getSessionStudents(): List<String>**\
    Retrieves a list of students based on the current session (AM or PM) and returns their names.

    -   Example: `val students = studentTracker.getSessionStudents()`