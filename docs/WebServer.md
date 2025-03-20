Overview
--------

This file implements a web server for managing a variety of tasks including serving assets, handling API endpoints, and managing student and queue data. The server is built using the NanoHTTPD library and supports various administrative and student management functions.

### Main Features:

-   Serve static assets like HTML, CSS, and JS files.
-   Handle API endpoints for managing student data, adding/removing songs to/from a queue, banning songs, and more.
-   Session management with login and token validation.
-   Rate limiting and blacklisting of IP addresses.
-   Integration with Google APIs for managing variables (e.g., Google Slideshow IDs).

* * * * *

Variables
---------

-   **MIME_JSON**: The MIME type for JSON content.
-   **utils**: An instance of the `Utils` class, used for utility functions like extracting Google IDs, banning songs, etc.
-   **MAX_REQUESTS**: Maximum number of requests allowed per IP within a given time frame (1 minute).
-   **TIME_FRAME_MS**: The time frame (in milliseconds) for rate limiting requests.
-   **sessionTokens**: A set of valid session tokens for managing user sessions.

* * * * *

Functions
---------

### `serve(session: IHTTPSession?): Response?`

-   **Description**: Main method that handles incoming HTTP requests. It serves static assets and routes to various API endpoints based on the URI.
-   **Parameters**:
    -   `session`: The HTTP session containing the request details.
-   **Returns**: A `Response` object that contains the result of processing the request.
-   **Example**:\
    For the homepage, it serves the `index.html` file. For the API, it checks for specific URIs like `/restart-thread` or `/get-queue` and calls the appropriate handler methods.

* * * * *

### `handleAddToQueue(session: IHTTPSession): Response`

-   **Description**: Handles requests to add a song to the queue.

-   **Parameters**:

    -   `session`: The HTTP session containing the request parameters.
-   **Returns**: A `Response` object indicating whether the song was successfully added or if an error occurred.

-   **Example**:\
    The handler expects `song` and `artist` parameters from the request and checks if the song is banned before adding it to the queue.

    `val songName = params["song"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Song name is required")`

* * * * *

### `handleRemoveFromQueue(session: IHTTPSession): Response`

-   **Description**: Handles requests to remove a song from the queue.

-   **Parameters**:

    -   `session`: The HTTP session containing the request parameters.
-   **Returns**: A `Response` object indicating whether the song was successfully removed or if an error occurred.

-   **Example**:\
    This handler looks for a `songId` parameter in the request and removes the corresponding song from the queue if it exists.

    `val songId = params["songId"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Song ID is required")`

Overview
--------

This file implements a web server for managing a variety of tasks including serving assets, handling API endpoints, and managing student and queue data. The server is built using the NanoHTTPD library and supports various administrative and student management functions.

### Main Features:

-   Serve static assets like HTML, CSS, and JS files.
-   Handle API endpoints for managing student data, adding/removing songs to/from a queue, banning songs, and more.
-   Session management with login and token validation.
-   Rate limiting and blacklisting of IP addresses.
-   Integration with Google APIs for managing variables (e.g., Google Slideshow IDs).

* * * * *

Variables
---------

-   **MIME_JSON**: The MIME type for JSON content.
-   **utils**: An instance of the `Utils` class, used for utility functions like extracting Google IDs, banning songs, etc.
-   **MAX_REQUESTS**: Maximum number of requests allowed per IP within a given time frame (1 minute).
-   **TIME_FRAME_MS**: The time frame (in milliseconds) for rate limiting requests.
-   **sessionTokens**: A set of valid session tokens for managing user sessions.

* * * * *

Functions
---------

### `serve(session: IHTTPSession?): Response?`

-   **Description**: Main method that handles incoming HTTP requests. It serves static assets and routes to various API endpoints based on the URI.
-   **Parameters**:
    -   `session`: The HTTP session containing the request details.
-   **Returns**: A `Response` object that contains the result of processing the request.
-   **Example**:\
    For the homepage, it serves the `index.html` file. For the API, it checks for specific URIs like `/restart-thread` or `/get-queue` and calls the appropriate handler methods.

* * * * *

### `handleAddToQueue(session: IHTTPSession): Response`

-   **Description**: Handles requests to add a song to the queue.

-   **Parameters**:

    -   `session`: The HTTP session containing the request parameters.
-   **Returns**: A `Response` object indicating whether the song was successfully added or if an error occurred.

-   **Example**:\### `handleBanSong(session: IHTTPSession): Response`

-   **Description**: Bans a song from being added to the queue in the future.

-   **Parameters**:

    -   `session`: The HTTP session containing the request parameters.
-   **Returns**: A `Response` object indicating whether the song was successfully banned or if an error occurred.

-   **Example**:\
    This function expects a `songId` in the request and adds it to a banned list.

    `val songId = params["songId"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Song ID is required")`

* * * * *

### `handleGetQueue(session: IHTTPSession): Response`

-   **Description**: Returns the current queue of songs as a JSON response.

-   **Parameters**:

    -   `session`: The HTTP session containing the request details.
-   **Returns**: A `Response` object with the queue in JSON format.

-   **Example**:\
    This function generates a JSON response with all the songs in the queue.

    `val queue = getQueueFromDatabase()` `val jsonResponse = JSONObject().put("queue", queue)` `return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, jsonResponse.toString())`

* * * * *

### `handleGetStudents(session: IHTTPSession): Response`

-   **Description**: Returns a list of students currently checked in or out, formatted as JSON.

-   **Parameters**:

    -   `session`: The HTTP session containing the request details.
-   **Returns**: A `Response` object with the list of students in JSON format.

-   **Example**:\
    This function queries the database for checked-in or checked-out students and returns the result as a JSON object.

    `val students = getStudentsFromDatabase()` `val jsonResponse = JSONObject().put("students", students)` `return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, jsonResponse.toString())`

* * * * *

### `handleLogin(session: IHTTPSession): Response`

-   **Description**: Handles the login process by verifying credentials and generating a session token.

-   **Parameters**:

    -   `session`: The HTTP session containing the request details, including login credentials.
-   **Returns**: A `Response` object indicating whether login was successful or if an error occurred.

-   **Example**:\
    The handler expects `username` and `password` parameters from the request and compares them against stored credentials.

    `val username = params["username"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Username is required")`

    `val password = params["password"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Password is required")`

    If the credentials match, a session token is generated and returned to the user.

* * * * *

### `handleLogout(session: IHTTPSession): Response`

-   **Description**: Logs the user out by invalidating the session token.

-   **Parameters**:

    -   `session`: The HTTP session containing the session token to be invalidated.
-   **Returns**: A `Response` object confirming that the user has been logged out.

-   **Example**:\
    This handler checks for a valid session token, and if it exists, it removes it from the session store.

    `val token = session.headers["Authorization"] ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Unauthorized")`

    `invalidateSessionToken(token)`

    The user is successfully logged out, and the session is invalidated.

* * * * *

### `handleRestartThread(session: IHTTPSession): Response`

-   **Description**: Restarts a specific thread, such as one managing song queue or student tracking.

-   **Parameters**:

    -   `session`: The HTTP session containing the details of which thread to restart.
-   **Returns**: A `Response` object indicating whether the thread was successfully restarted.

-   **Example**:\
    The handler checks for an optional `threadId` parameter and restarts the corresponding thread.

    `val threadId = params["threadId"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Thread ID is required")`

    Restart the thread associated with the `threadId`.

* * * * *

### `handleGetStudentById(session: IHTTPSession): Response`

-   **Description**: Retrieves information about a specific student by their ID.

-   **Parameters**:

    -   `session`: The HTTP session containing the student ID.
-   **Returns**: A `Response` object with the student's information in JSON format.

-   **Example**:\
    This handler looks for the `studentId` parameter and returns detailed information about the student.

    `val studentId = params["studentId"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Student ID is required")`

    Query the database for the student's details and return them as a JSON response.
    The handler expects `song` and `artist` parameters from the request and checks if the song is banned before adding it to the queue.

    `val songName = params["song"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Song name is required")`

* * * * *

### `handleRemoveFromQueue(session: IHTTPSession): Response`

-   **Description**: Handles requests to remove a song from the queue.

-   **Parameters**:

    -   `session`: The HTTP session containing the request parameters.
-   **Returns**: A `Response` object indicating whether the song was successfully removed or if an error occurred.

-   **Example**:\
    This handler looks for a `songId` parameter in the request and removes the corresponding song from the queue if it exists.

    `val songId = params["songId"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Song ID is required")`