Overview
--------

The `SpotifyManager` class manages interactions with the Spotify API, including handling authentication, fetching the currently playing track, updating track progress, and controlling playback. It also handles the saving and retrieval of access and refresh tokens. The class provides functionality for starting the authorization process, refreshing tokens, and updating the UI with track information.

Variables
---------

-   **updateHandler: Handler?**\
    A handler used to periodically update the UI with the current track's progress.

-   **updateRunnable: Runnable?**\
    A runnable that fetches and updates track information every second.

-   **lastTrackProgressMs: Int**\
    The current progress (in milliseconds) of the track being played.

-   **lastTrackDurationMs: Int**\
    The total duration (in milliseconds) of the currently playing track.

-   **lastTrackName: String**\
    The name of the currently playing track.

-   **lastArtistName: String**\
    The name of the artist of the currently playing track.

-   **lastTrackURI: String**\
    The URI of the currently playing track.

-   **apiCallCounter: Int**\
    A counter used to control the frequency of API calls.

-   **MartinezId: String**\
    The client ID for the Spotify application.

-   **MartinezSecret: String**\
    The client secret for the Spotify application.

-   **IsPlaying: Boolean**\
    A boolean indicating whether the current track is playing.

-   **queueUris: MutableList<String>**\
    A list of URIs representing the current queue of tracks in Spotify.

-   **trackInfo: Map<String, Any>?**\
    A map containing information about the currently playing track, including its name, artist, progress, and URI.

-   **ClientId: String**\
    The Spotify client ID, which can be overridden by the `MartinezId` value.

-   **ClientSecret: String**\
    The Spotify client secret, which can be overridden by the `MartinezSecret` value.

* * * * *

Functions
---------

### startSpotifyAuthorization(override: Boolean): Thread

**Description**: Initiates the Spotify authorization process. If the access token or refresh token is not available, it opens the Spotify authorization URL in a browser.\
**Parameters**:

-   `override`: A boolean to force the authorization process even if the tokens are present.\
    **Example**:\
    `spotifyManager.startSpotifyAuthorization(true)`

* * * * *

### generateRandomString(length: Int): String

**Description**: Generates a random string of the specified length, used for creating the state parameter during the Spotify OAuth flow.\
**Parameters**:

-   `length`: The length of the random string to generate.\
    **Example**:\
    `val randomString = spotifyManager.generateRandomString(16)`

* * * * *

### exchangeAuthorizationCodeForAccessToken(code: String)

**Description**: Exchanges the authorization code for an access token and refresh token.\
**Parameters**:

-   `code`: The authorization code received from Spotify after user authentication.\
    **Example**:\
    `spotifyManager.exchangeAuthorizationCodeForAccessToken("authorization_code")`

* * * * *

### refreshAccessToken(refreshToken: String?, callback: (newAccessToken: String?) -> Unit)

**Description**: Refreshes the access token using the provided refresh token. The new access token is returned via a callback.\
**Parameters**:

-   `refreshToken`: The refresh token used to get a new access token.
-   `callback`: A callback function that receives the new access token.\
    **Example**:\
    `spotifyManager.refreshAccessToken("refresh_token") { newAccessToken -> // Handle new access token }`

* * * * *

### parseAccessTokenResponse(response: String)

**Description**: Parses the response from the Spotify API to extract the access token, refresh token, and other details.\
**Parameters**:

-   `response`: The response JSON string from Spotify's token endpoint.\
    **Example**:\
    `spotifyManager.parseAccessTokenResponse(response)`

* * * * *

### saveTokens(accessToken: String?, refreshToken: String?)

**Description**: Saves the access and refresh tokens to SharedPreferences.\
**Parameters**:

-   `accessToken`: The access token to save.
-   `refreshToken`: The refresh token to save.\
    **Example**:\
    `spotifyManager.saveTokens("access_token", "refresh_token")`

* * * * *

### getTokens(): Pair<String?, String?>

**Description**: Retrieves the access and refresh tokens from SharedPreferences.\
**Returns**: A pair containing the access token and refresh token.\
**Example**:\
`val tokens = spotifyManager.getTokens()`

* * * * *

### startTrackUpdates(spotifyView: TextView)

**Description**: Starts periodic updates to the UI with the current track's progress and other details.\
**Parameters**:

-   `spotifyView`: A TextView to display the track name and artist.\
    **Example**:\
    `spotifyManager.startTrackUpdates(spotifyView)`

* * * * *

### stopTrackUpdates()

**Description**: Stops the periodic track updates.\
**Example**:\
`spotifyManager.stopTrackUpdates()`

* * * * *

### fetchTrackInfoFromAPI(spotifyView: TextView): Thread

**Description**: Fetches the current track's information from the Spotify API and updates the UI.\
**Parameters**:

-   `spotifyView`: A TextView to display the track name and artist.\
    **Returns**: A Thread that performs the API request.\
    **Example**:\
    `spotifyManager.fetchTrackInfoFromAPI(spotifyView)`

* * * * *

### updateUI(spotifyView: TextView)

**Description**: Updates the UI with the current track's name, artist, progress, and other details.\
**Parameters**:

-   `spotifyView`: A TextView to display the track name and artist.\
    **Example**:\
    `spotifyManager.updateUI(spotifyView)`

* * * * *

### fetchSpotifyQueue(): List<Map<String, String>>

**Description**: Fetches the current queue of tracks from the Spotify API.\
**Returns**: A list of maps, each containing the name, artist, and URI of a track in the queue.\
**Example**:\
`val queue = spotifyManager.fetchSpotifyQueue()`

* * * * *

### removeSongFromQueue(songUri: String?, playlistLink: String?): Triple<List<Map<String, String>>, List<Map<String, String>>, List<Map<String, String>>>

**Description**: Removes a song from the current queue and optionally adds songs from a specified playlist. It fetches the current queue, removes the specified song, and clears the queue before adding back the currently playing song. The function also allows adding tracks from a playlist to the queue.\
**Parameters**:

-   `songUri`: The URI of the song to remove from the queue.
-   `playlistLink`: The URL of the Spotify playlist from which songs will be added to the queue (optional).\
    **Returns**: A Triple containing:
    -   `updatedQueue`: The updated list of songs after the removal and additions.
    -   `filteredQueue`: The list of songs after the specified song was removed.
    -   `queue`: The original queue before any modifications.\
        **Example**:\
        `val result = spotifyManager.removeSongFromQueue(songUri, playlistLink)`

* * * * *

### addToQueue(song: String, artist: String): Boolean

**Description**: Adds a song to the current playback queue based on the song name and artist. It first checks if the song is banned, then performs a search to find the song's URI and adds it to the queue.\
**Parameters**:

-   `song`: The name of the song to add to the queue.
-   `artist`: The name of the artist of the song.\
    **Returns**: Boolean indicating whether the song was successfully added to the queue.\
    **Example**:\
    `val isAdded = spotifyManager.addToQueue("Song Name", "Artist Name")`