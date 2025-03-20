package com.example.clevertest2

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch

//global spotify variables
private var updateHandler: Handler? = null
private var updateRunnable: Runnable? = null
private var lastTrackProgressMs = 0
private var lastTrackDurationMs = 0
private var lastTrackName = ""
private var lastArtistName = ""
private var lastTrackURI = ""
private var apiCallCounter = 0
val MartinezId = "REMOVED FOR SECURITY"
val MartinezSecret = "REMOVED FOR SECURITY"
var IsPlaying = true;
private val queueUris: MutableList<String> = mutableListOf()
private var trackInfo: Map<String, Any>? = null

//change these based on spotify playlist
var ClientId =MartinezId
var ClientSecret = MartinezSecret

class SpotifyManager(private val activity: MainActivity) {
    var fetchSpotifyThread: Thread? = null
    var trackSpotifyThread: Thread? = null
    val utils = Utils(activity)


    fun startSpotifyAuthorization(override: Boolean): Thread {
        fetchSpotifyThread = Thread {
            val (accessToken, refreshToken) = getTokens()
            if (accessToken == null || refreshToken == null || override) {
                val clientId = ClientId
                val redirectUri = "myapp://callback"
                val state = generateRandomString(16)
                val scope =
                    "user-read-private user-read-email user-read-currently-playing user-read-playback-state user-modify-playback-state playlist-modify-private app-remote-control "

                val authorizationUrl = Uri.Builder()
                    .scheme("https")
                    .authority("accounts.spotify.com")
                    .appendPath("authorize")
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("client_id", clientId)
                    .appendQueryParameter("redirect_uri", redirectUri)
                    .appendQueryParameter("state", state)
                    .appendQueryParameter("scope", scope)
                    .appendQueryParameter("show_dialog", "true")
                    .build()
                    .toString()

                // Open the authorization URL in the browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
                activity.startActivity(intent)
            } else {
                val spotifyPlaying = activity.findViewById<TextView>(R.id.playingTextView)
                startTrackUpdates(spotifyPlaying)
            }
        }

        fetchSpotifyThread?.start()  // Start the new thread
        return fetchSpotifyThread!!
    }

    fun generateRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }


    fun exchangeAuthorizationCodeForAccessToken(code: String) {
        val clientId = ClientId
        val clientSecret = ClientSecret
        val redirectUri = "myapp://callback"

        val url = URL("https://accounts.spotify.com/api/token")
        val postParams = "grant_type=authorization_code" +
                "&code=$code" +
                "&redirect_uri=$redirectUri"

        // Base64 encode the client ID and client secret
        val authHeader = "Basic " + Base64.encodeToString(
            "$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP
        )

        Thread {
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", authHeader)
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true

                // Write the POST parameters to the output stream
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(postParams)
                writer.flush()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    // Parse the response to get the access token
                    parseAccessTokenResponse(response)
                } else {
                    Log.d("Spotify", "Error: ${connection.responseCode}")
                }
            } finally {
                connection.disconnect()
            }
        }.start()

        // Fetch the currently playing track

    }

    fun refreshAccessToken(refreshToken: String?, callback: (newAccessToken: String?) -> Unit) {
        val clientId = ClientId
        val clientSecret = ClientSecret

        val url = URL("https://accounts.spotify.com/api/token")
        val postParams = "grant_type=refresh_token&refresh_token=$refreshToken"

        val authHeader = "Basic " + Base64.encodeToString(
            "$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP
        )

        Thread {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", authHeader)
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(postParams)
                writer.flush()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val newAccessToken = jsonResponse.getString("access_token")

                    // Save the new access token for future use
                    saveTokens(newAccessToken, refreshToken)

                    // Pass the new token back via the callback
                    callback(newAccessToken)
                } else if (connection.responseCode == 400) {
                    callback(null)
                    startSpotifyAuthorization(true)
                } else {
                    Log.d("Spotify", "Error refreshing token: ${connection.responseCode}")
                    callback(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }

    fun parseAccessTokenResponse(response: String) {
        try {
            // Parse the response string as a JSON object
            val jsonObject = JSONObject(response)

            // Extract the tokens and other details
            val accessToken = jsonObject.getString("access_token")
            val tokenType = jsonObject.getString("token_type")
            val expiresIn = jsonObject.getInt("expires_in")
            val refreshToken = jsonObject.optString(
                "refresh_token",
                null
            ) // Nullable as it might not always be present
            val scope = jsonObject.getString("scope")

            // Log or store the tokens as needed
            println("Access Token: $accessToken")
            println("Token Type: $tokenType")
            println("Expires In: $expiresIn")
            println("Refresh Token: $refreshToken")
            println("Scope: $scope")

            // Save the access token and refresh token securely (e.g., in SharedPreferences)
            saveTokens(accessToken, refreshToken)
            //fetch song


        } catch (e: Exception) {
            e.printStackTrace()
            // Handle parsing error
            println("Error parsing access token response: ${e.message}")
        }
    }

    fun saveTokens(accessToken: String?, refreshToken: String?) {
        // Example: Use SharedPreferences to store the tokens securely
        val sharedPreferences = activity.getSharedPreferences("SpotifyTokens", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("ACCESS_TOKEN", accessToken)
        refreshToken?.let { editor.putString("REFRESH_TOKEN", it) } // Save only if not null
        editor.apply()


    }

    fun getTokens(): Pair<String?, String?> {
        // Retrieve the SharedPreferences
        val sharedPreferences = activity.getSharedPreferences("SpotifyTokens", MODE_PRIVATE)

        // Get the access token and refresh token
        val accessToken = sharedPreferences.getString("ACCESS_TOKEN", null)
        val refreshToken = sharedPreferences.getString("REFRESH_TOKEN", null)

        return Pair(accessToken, refreshToken)
    }

    fun startTrackUpdates(spotifyView: TextView) {
        stopTrackUpdates()
        // Create a new handler and runnable
        updateHandler = Handler(Looper.getMainLooper())
        updateRunnable = object : Runnable {
            override fun run() {
                if (apiCallCounter % 3 == 0) {
                    // Make an API call every 3rd iteration (every 3 seconds)
                    fetchTrackInfoFromAPI(spotifyView)
                } else {
                    // Update UI locally using cached progress
                    updateUI(spotifyView)
                }
                apiCallCounter++
                updateHandler?.postDelayed(this, 1000) // Run every 1 second
            }
        }
        updateHandler?.post(updateRunnable!!)  // Start the new runnable
    }

     fun stopTrackUpdates() {
        // Remove any existing callbacks
        updateHandler?.removeCallbacks(updateRunnable!!)
        updateHandler = null  // Clear the handler
        updateRunnable = null  // Clear the runnable
    }

     fun fetchTrackInfoFromAPI(spotifyView: TextView): Thread {
        trackSpotifyThread = Thread {
            val (accessToken, refreshToken) = getTokens()
            val url = URL("https://api.spotify.com/v1/me/player/currently-playing")
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    lastTrackName = jsonResponse.getJSONObject("item").getString("name")
                    lastArtistName = jsonResponse.getJSONObject("item").getJSONArray("artists")
                        .getJSONObject(0).getString("name")
                    lastTrackProgressMs = jsonResponse.getInt("progress_ms")
                    lastTrackDurationMs = jsonResponse.getJSONObject("item").getInt("duration_ms")
                    IsPlaying = jsonResponse.getBoolean("is_playing")
                    lastTrackURI = jsonResponse.getJSONObject("item").getString("uri")

                    // Update trackInfo
                    trackInfo = mapOf(
                        "name" to lastTrackName,
                        "artist" to lastArtistName,
                        "progressMs" to lastTrackProgressMs,
                        "durationMs" to lastTrackDurationMs,
                        "isPlaying" to IsPlaying,
                        "uri" to lastTrackURI
                    )


                    activity.runOnUiThread {
                        updateUI(spotifyView)
                    }
                } else if (connection.responseCode == 401) {
                    // Token expired, refresh the token
                    refreshAccessToken(refreshToken) { newAccessToken ->
                        if (newAccessToken != null) {
                            // Retry the request with the new access token
                            fetchTrackInfoFromAPI(spotifyView)
                        } else {
                            Log.d("Spotify", "Failed to refresh access token")
                        }
                    }
                } else if (connection.responseCode == 403) {
                    saveTokens(null, null)
                } else {
                    Log.d("Spotify API", "Error: ${connection.responseCode}")
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        trackSpotifyThread?.start()  // Start the new thread
        return trackSpotifyThread!!
    }

     fun updateUI(spotifyView: TextView) {
        if (!IsPlaying) {
            return
        } else {

            activity.runOnUiThread {
                spotifyView.text = "$lastTrackName - $lastArtistName"

                val songProgressBar = activity.findViewById<SeekBar>(R.id.songProgressBar)
                val currentTimeTextView = activity.findViewById<TextView>(R.id.currentTime)
                val totalTimeTextView = activity.findViewById<TextView>(R.id.totalTime)

                // Set the progress bar max value to the song's duration
                songProgressBar.max = lastTrackDurationMs

                // Update the current time and total time text
                currentTimeTextView.text = utils.formatTime(lastTrackProgressMs)
                totalTimeTextView.text = utils.formatTime(lastTrackDurationMs)

                // Set the progress
                songProgressBar.progress = lastTrackProgressMs

                // Increment progress for the next second
                lastTrackProgressMs += 1000
                if (lastTrackProgressMs > lastTrackDurationMs) {
                    lastTrackProgressMs = lastTrackDurationMs // Cap at max duration
                }
            }
        }
    }

    fun fetchSpotifyQueue(): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        val latch = CountDownLatch(1)

        Thread {
            val (accessToken, _) = getTokens()
            val url = URL("https://api.spotify.com/v1/me/player/queue")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")

            try {
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val queueItems = jsonResponse.getJSONArray("queue")

                    for (i in 0 until queueItems.length()) {
                        val item = queueItems.getJSONObject(i)
                        val trackName = item.getString("name")
                        val artistName = item.getJSONArray("artists").getJSONObject(0).getString("name")
                        val songUri = item.getString("uri")
                        result.add(
                            mapOf(
                                "name" to trackName,
                                "artist" to artistName,
                                "uri" to songUri
                            )
                        )
                    }
                } else {
                    println("Error fetching queue: ${connection.responseCode} ${connection.responseMessage}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connection.disconnect()
                latch.countDown()
            }
        }.start()

        latch.await()
        return result
    }


    fun removeSongFromQueue(songUri: String?, playlistLink: String?): Triple<List<Map<String, String>>, List<Map<String, String>>, List<Map<String, String>>> {
        val latch = CountDownLatch(1)
        val updatedQueue = mutableListOf<Map<String, String>>()
        var filteredQueue = mutableListOf<Map<String, String>>()
        var queue = mutableListOf<Map<String, String>>()


        Thread {
            try {
                val (accessToken, _) = getTokens()

                // Fetch the current queue
                queue = fetchSpotifyQueue().toMutableList()

                // Remove the song from the queue
                 filteredQueue = queue.filter { it["uri"] != songUri }.toMutableList()

                // Get the currently playing song
                val playbackStateUrl = URL("https://api.spotify.com/v1/me/player")
                val playbackConnection = playbackStateUrl.openConnection() as HttpURLConnection
                playbackConnection.requestMethod = "GET"
                playbackConnection.setRequestProperty("Authorization", "Bearer $accessToken")

                val playbackResponse = if (playbackConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    playbackConnection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    println("Failed to fetch playback state: ${playbackConnection.responseMessage}")
                    return@Thread
                }
                playbackConnection.disconnect()

                val playbackJson = JSONObject(playbackResponse)
                val currentlyPlayingUri = playbackJson.getJSONObject("item").getString("uri")

                // Clear the queue and add back only the currently playing song
                val clearQueueRequestBody = """
            {
                "uris": ["$currentlyPlayingUri"]
            }
            """.trimIndent()

                val playUrl = URL("https://api.spotify.com/v1/me/player/play")
                val playConnection = playUrl.openConnection() as HttpURLConnection
                playConnection.requestMethod = "PUT"
                playConnection.setRequestProperty("Authorization", "Bearer $accessToken")
                playConnection.setRequestProperty("Content-Type", "application/json")
                playConnection.doOutput = true
                playConnection.outputStream.write(clearQueueRequestBody.toByteArray())

                if (playConnection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    println("Error clearing queue: ${playConnection.responseMessage}")
                    return@Thread
                }
                playConnection.disconnect()

                // Add the filtered queue back
                for (song in filteredQueue) {
                    val songUri = song["uri"] ?: continue
                    val addToQueueUrl = URL("https://api.spotify.com/v1/me/player/queue?uri=$songUri")
                    val addToQueueConnection = addToQueueUrl.openConnection() as HttpURLConnection
                    addToQueueConnection.requestMethod = "POST"
                    addToQueueConnection.setRequestProperty("Authorization", "Bearer $accessToken")

                    if (addToQueueConnection.responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        println("Added ${song["name"]} back to the queue.")
                        updatedQueue.add(song)
                    } else {
                        println("Error adding song back to the queue: ${addToQueueConnection.responseMessage}")
                    }
                    addToQueueConnection.disconnect()
                }

                // Add songs from the playlist if provided
                if (!playlistLink.isNullOrBlank()) {
                    val playlistId = extractPlaylistIdFromLink(playlistLink)
                    val playlistTracksUrl = URL("https://api.spotify.com/v1/playlists/$playlistId/tracks")
                    val playlistConnection = playlistTracksUrl.openConnection() as HttpURLConnection
                    playlistConnection.requestMethod = "GET"
                    playlistConnection.setRequestProperty("Authorization", "Bearer $accessToken")

                    val playlistResponse = if (playlistConnection.responseCode == HttpURLConnection.HTTP_OK) {
                        playlistConnection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        println("Failed to fetch playlist: ${playlistConnection.responseMessage}")
                        return@Thread
                    }
                    playlistConnection.disconnect()

                    val playlistJson = JSONObject(playlistResponse)
                    val tracks = playlistJson.getJSONArray("items")
                    for (i in 0 until tracks.length()) {
                        val track = tracks.getJSONObject(i).getJSONObject("track")
                        val trackUri = track.getString("uri")

                        val addToQueueUrl = URL("https://api.spotify.com/v1/me/player/queue?uri=$trackUri")
                        val addToQueueConnection = addToQueueUrl.openConnection() as HttpURLConnection
                        addToQueueConnection.requestMethod = "POST"
                        addToQueueConnection.setRequestProperty("Authorization", "Bearer $accessToken")

                        if (addToQueueConnection.responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                            println("Added ${track.getString("name")} from playlist.")
                        } else {
                            println("Error adding song from playlist: ${addToQueueConnection.responseMessage}")
                        }
                        addToQueueConnection.disconnect()
                    }
                }

                println("Queue updated successfully.")

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                latch.countDown()
            }
        }.start()

        latch.await()
        return Triple(updatedQueue, filteredQueue, queue)
    }
    private fun extractPlaylistIdFromLink(link: String): String? {
        val regex = Regex("(?<=playlist/)[^?]+|(?<=spotify:playlist:)[^?]+")
        return regex.find(link)?.value
    }




    fun addToQueue(song: String, artist: String): Boolean{
        val latch = CountDownLatch(1) // Latch to wait for the result
        val (accessToken, _) = getTokens()
        var result = false

        if(utils.isBanned(song,artist)){
            return false
        }

        Thread {
            try {
                val query = "$song artist:$artist"
                val url = URL("https://api.spotify.com/v1/search?q=${URLEncoder.encode(query, "UTF-8")}&type=track&limit=1")

                var songURI: String? = null
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val tracks = json.getJSONObject("tracks").getJSONArray("items")
                    if (tracks.length() > 0) {
                         songURI = tracks.getJSONObject(0).getString("uri") // Return the first track's URI
                    }
                }

                val addURL = URL("https://api.spotify.com/v1/me/player/queue?uri=$songURI")
                val connection2 = addURL.openConnection() as HttpURLConnection
                connection2.requestMethod = "POST"
                connection2.setRequestProperty("Authorization", "Bearer $accessToken")
                connection2.connect()

                if(connection2.responseCode == HttpURLConnection.HTTP_OK){
                    result = true
                }
                else{
                    Log.d("spotify", "error ${connection2.responseMessage}")
                    result = false
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                latch.countDown() // Signal that the thread has completed
            }
        }.start()

        latch.await() // Wait for the background thread to finish
        return result
    }

}