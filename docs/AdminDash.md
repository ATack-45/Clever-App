#### Overview:

The **Admin Dashboard** allows administrators to manage the system's queue, ban songs, and perform various system tasks like refreshing documents and restarting services. It provides functionality for session management, queue updates, and song banning, all through a user-friendly interface.

* * * * *

#### HTML Structure:

The page layout consists of several key components:

1.  **Top Buttons**: A fixed section at the top that provides navigation links to the home page and the database dashboard.
2.  **Queue Management Section**: Displays the queue of songs with options to remove them and manage playlists.
3.  **Ban Management Section**: Allows banning songs by name, artist, and genre, and viewing the list of banned songs.
4.  **Miscellaneous Controls**: Provides buttons for restarting threads (e.g., refreshing documents and services), a form for entering Google object links, and a button for box breathing exercises.

* * * * *

#### Key Elements:

1.  **Navigation Buttons**:

    -   **Home Button**: Redirects to the homepage.
    -   **Database Dashboard Button**: Redirects to the database dashboard.
2.  **Queue Section**:

    -   **Get Queue Button**: Fetches the current song queue.
    -   **Playlist Link Input**: Accepts a Spotify playlist URL.
    -   **Queue Table**: Displays the current songs in the queue with options to remove them.
3.  **Ban Section**:

    -   **Ban Song Form**: Inputs for banning songs by name, artist, and genre.
    -   **Fetch Banned List Button**: Retrieves and displays the list of banned songs.
    -   **Banned Songs Table**: Displays the list of banned songs.
4.  **System Controls**:

    -   **Refresh Buttons**: Refresh the Today Document, Calendar, Student Tracker, and Spotify.
    -   **Box Breathing Button**: Triggers a box breathing exercise.
    -   **Variable Update Form**: Allows updating Google object links by submitting them in a form.

* * * * *

#### CSS:

-   **Fixed Buttons**: The top buttons are styled to be fixed at the top of the screen and are spaced using Flexbox.
-   **Container Margin**: The container has a `margin-top` to account for the fixed position of the top buttons, preventing overlap.
-   **Styling**: Basic Bootstrap styling is used, with additional styles defined in the `style` section for specific positioning and layout adjustments.

* * * * *

#### JavaScript Functionality:

1.  **Fetching the Queue**:

    -   When the **"Get Queue"** button is clicked, the queue is fetched from the backend through the `/get-queue` endpoint.
    -   The queue is displayed in a table, and each song can be removed by clicking the corresponding **X** button.
2.  **Removing Songs from the Queue**:

    -   When a song is removed, its URI is sent to the backend via the `/remove-from-queue` endpoint.
    -   The playlist link is passed to the backend to identify the correct playlist.
3.  **Banning Songs**:

    -   The **Ban Song Form** collects the song name, artist, and genre, then sends the data to the backend using the `/ban-song` endpoint.
    -   The **Fetch Banned List** button retrieves the list of banned songs from the backend and displays them in a table.
4.  **Refreshing Services**:

    -   The **Refresh Today Doc**, **Refresh Calendar**, **Restart Tracker**, and **Restart Spotify** buttons send requests to restart or refresh various services (e.g., document, calendar, tracker, Spotify).
5.  **Prompt for Breather Name**:

    -   The **Invisible Button** prompts the admin to input a name for the "breather" through a prompt window.
    -   The entered name is sent to the backend using the `/setBreather` endpoint.
6.  **Session Validation**:

    -   On page load, the **fetchQueue()** and **fetchBannedList()** functions are called to load the initial data (queue and banned list).
    -   Additional validation may be required for session management, ensuring that only authorized users can access the dashboard.

* * * * *

#### Interaction Flow:

-   **Queue Management**: Admin clicks "Get Queue" to load the current queue, and can remove songs by clicking the **X** button next to each entry.
-   **Song Banning**: Admin fills in the form to ban a song by name, artist, and genre, then clicks submit. The banned song is processed and added to the banned list.
-   **Service Control**: Admin can refresh various system services using the provided buttons, like refreshing the calendar, tracker, and more.
-   **Breather Management**: Admin clicks the invisible button to enter a name for the "breather," which is then sent to the backend.

* * * * *

#### Example Use Case:

-   **Use Case 1**: Admin needs to clear the queue. They click the "Get Queue" button, view the current list of songs, and click **X** to remove unwanted songs.
-   **Use Case 2**: Admin needs to ban a song. They input the song name, artist, and genre into the **Ban Song Form**, then click submit, and the song is banned and displayed in the banned list.
-   **Use Case 3**: Admin wants to refresh the system by restarting a service. They click one of the **Refresh** buttons to trigger the backend process.