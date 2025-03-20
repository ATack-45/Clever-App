Overview
--------

The `Utils` class provides various utility functions for the application, including time formatting, box breathing, random text , banned song management, and extracting Google IDs from URLs.

Properties
----------

-   **bannedList**: A mutable list storing banned songs and their respective artists.

    -   Example: `[("Song A", "Artist X"), ("Song B", "Artist Y")]`
-   **strictName**: A name that overrides the second randomly selected name when picking two names.

    -   Example: `strictName = "John Doe"`

Functions
---------

### **Time Formatting**

-   **formatTime(ms: Int): String**\
    Converts milliseconds into a formatted time string (`MM:SS`).
    -   Example: `formatTime(125000)` → Returns `"02:05"`

### **Random Name Selection**

-   **pickTwoRandomNames(names: List<String>): Pair<String, String?>**\
    Selects two random names from the list. If `strictName` is set, it replaces the second name.
    -   Example: `pickTwoRandomNames(["Alice", "Bob", "Charlie"])` → Returns `("Alice", "Bob")`

### **Text Manipulation**

-   **scrambleText(original: String): String**\
    Randomly shuffles the characters in a string.

    -   Example: `scrambleText("hello")` → Returns `"ohlle"` (output varies)
-   **showNamesWithAnimation(names: Pair<String, String?>)**\
    Displays two names with an animated unscramble effect and auto-hides after one minute.

    -   Example: `showNamesWithAnimation(Pair("Alice", "Bob"))`
-   **animateUnscrambleText(textView: TextView, name: String, totalDuration: Long = 6000L)**\
    Animates the text from a scrambled state to its original form.

    -   Example: `animateUnscrambleText(textView, "Alice")`
-   **generateRandomChars(length: Int): String**\
    Generates a random alphanumeric string of the specified length.

    -   Example: `generateRandomChars(8)` → Returns `"A1bC3dE4"` (output varies)
-   **generateRandomString(length: Int): String**\
    Similar to `generateRandomChars`, but uses a fixed character set.

    -   Example: `generateRandomString(5)` → Returns `"xZ3aB"` (output varies)

### **Banned Song Management**

-   **loadBannedList()**\
    Loads banned songs from a file and populates `bannedList`.

-   **ban(song: String?, artist: String?): Boolean**\
    Adds a song and artist to the banned list and saves it to a file.

    -   Example: `ban("Song A", "Artist X")`
-   **isBanned(songName: String?, artistName: String?): Boolean**\
    Checks if a song or artist is in the banned list.

    -   Example: `isBanned("Song A", "Artist X")` → Returns `true`

### **Google ID Extraction**

-   **extractGoogleIds(url: String): Triple<String?, String?, String>?**\
    Extracts IDs from Google Slides and Google Calendar URLs.
    -   Example:
        -   `extractGoogleIds("https://docs.google.com/presentation/d/xyz123")` → Returns `("xyz123", null, "Google Slideshow ID")`
        -   `extractGoogleIds("https://calendar.google.com/calendar/embed?src=abc%40gmail.com")` → Returns `("abc@gmail.com", null, "Google Calendar ID")`