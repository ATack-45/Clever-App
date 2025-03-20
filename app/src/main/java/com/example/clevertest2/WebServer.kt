package com.example.clevertest2
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.util.UUID


// File: WebServer.kt


const val MIME_JSON = "application/json"

class WebServer(
    port: Int,
    private val restartCallback: (String, Boolean) -> Unit,
    private val fetchQueueFunction: () -> List<Map<String, String>>,
    private val removeSongFromQueue: (uri: String?, playlist: String?) -> Triple<List<Map<String, String>>, List<Map<String, String>>, List<Map<String, String>>>,
    private val getStudents: () -> Response,
    private val addToQueue: (song: String, artist: String) -> Boolean,
    private val updateVars: (id: String, type: String) -> Boolean,
    private val boxBreath: () -> Boolean,
    private val context: MainActivity // Pass context to access assets
) : NanoHTTPD(port) {

    val utils = Utils(context)

    @SuppressLint("NewApi")
    override fun serve(session: IHTTPSession?): Response? {
        val uri = session?.uri ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        //uncomment to enable blacklisting, can add not to change to whitelisting. Can also change to just whitelist for the admin dashboard

//
//        if (isBlacklisted(clientIp)) {
//            return newFixedLengthResponse(
//                Response.Status.FORBIDDEN,
//                MIME_PLAINTEXT,
//                "Your IP has been blacklisted."
//            )
//        }


        return when {
            //homepage
            uri == "/" -> serveAssetFile("Home/index.html", "text/html")
            uri == "/mainPage.js" -> serveAssetFile("Home/mainPage.js", "application/javascript")


            //admin dash
            uri == "/Dash"  -> loadDashboard(session)
            uri == "/script.js" -> serveAssetFile("AdminDash/script.js", "application/javascript")
            uri == "/get-Banned" -> getBanned()
            uri == "/setBreather" -> setBreather(session)

            //DB pannel
            uri == "/DB" || uri == "/db" -> loadDB(session)
            uri == "/confetti.js" -> serveAssetFile("DBPanel/confetti.js", "application/javascript")
            uri == "/student.js" -> serveAssetFile("DBPanel/student.js", "application/javascript")
            uri == "/styles/dashboard.css" -> serveAssetFile("DBPanel/styles/dashboard.css", "text/css")
            uri == "/styles/index.css" -> serveAssetFile("DBPanel/styles/index.css", "text/css")
            uri == "/styles/minipage.css" -> serveAssetFile("DBPanel/styles/minipage.css", "text/css")
            uri == "/styles/student.css" -> serveAssetFile("DBPanel/styles/student.css", "text/css")
            uri == "/client.js" -> serveAssetFile("DBPanel/scripts/client.js", "application/javascript")
            uri == "/scripts/weeklyActivity.js" -> serveAssetFile("DBPanel/scripts/weeklyActivity.js", "application/javascript")
            uri == "/scripts/client.js" -> serveAssetFile("DBPanel/scripts/client.js", "application/javascript")
            uri == "/images/dashboard.png" -> serveAssetFile("DBPanel/images/dashboard.png", "text/png")
            uri == "/bundle.js" -> serveAssetFile("DBPanel/scripts/bundle.js", "application/javascript")
            uri == "/chart.js/dist/chart.umd.js" -> serveAssetFile("DBPanel/chart.js/dist/chart.umd.js", "application/javascript")
            uri == "/fetch-students" -> fetchStudentsFromAzure()

            uri == "/styles.css" -> serveAssetFile("styles.css", "text/css")

           //API endpoints
            uri == "/restart-thread" -> handleRestartThread(session)
            uri == "/get-queue" -> handleGetQueue()
            uri == "/remove-from-queue" -> handleRemoveFromQueue(session)
            uri == "/login" -> handleLogin(session)
            uri == "/validate-session" -> handleValidateSession(session)
            uri == "/add-song" -> handleAddToQueue(session)
            uri == "/ban-song" -> handleBan(session)
            uri == "/update-vars" -> handleVars(session)
            uri == "/box-breath" -> boxBreather()
            uri == "/toggleQueue" -> toggleQueue()

            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File Not Found")
        }
    }

    var queueToggle = true
    private fun toggleQueue(): NanoHTTPD.Response? {
       if (queueToggle == true) {
           queueToggle = false
           return newFixedLengthResponse("Queue is now disabled")
       }
       else {
           queueToggle = true
           return newFixedLengthResponse("Queue is now enabled")
       }
    }

    private fun setBreather(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        val postData = HashMap<String, String>()

    // Parse the request body
        session.parseBody(postData)

    // Extract the raw JSON string from the "postData" key
        val jsonString = postData["postData"] ?: "{}" // Default to empty JSON if null

    // Parse JSON and extract "name"
        val jsonObject = JSONObject(jsonString)
        val name = jsonObject.optString("name", "")

        utils.strictName = name.toString()
        return newFixedLengthResponse("Breather set to $name")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun boxBreather(): NanoHTTPD.Response? {
        utils.boxBreather()
        return newFixedLengthResponse("Breathing!")
    }

    private fun handleVars(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        val postData = HashMap<String, String>()
        session.parseBody(postData)

        val formData = session.parameters
        val inputLink = formData["inputLink"]?.get(0) ?: ""

        val extractedData = utils.extractGoogleIds(inputLink)

        if (extractedData != null) {
            val (presentationId, slideId, type) = extractedData

            // Update variables based on type
            when (type) {
                "Google Slideshow ID" -> {
                    if (presentationId != null) updateVars(presentationId, "Google Slideshow ID")
                }
                "Google Slide ID" -> {
                    if (presentationId != null) updateVars(presentationId, "Google Slideshow ID")
                    if (slideId != null) updateVars(slideId, "Google Slide ID")
                }
                "Google Calendar ID" -> {
                    if (presentationId != null) updateVars(presentationId, "Google Calendar ID")
                }
            }

            return newFixedLengthResponse("Google variables updated successfully")
        }

        return newFixedLengthResponse("Invalid Google link")
    }


    //rate limiting security
    private val requestTimestamps = mutableMapOf<String, MutableList<Long>>()
    private val MAX_REQUESTS = 50// Maximum requests allowed
    private val TIME_FRAME_MS = 60000 // 1 minute in milliseconds

    private fun isRateLimited(ip: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val timestamps = requestTimestamps.getOrPut(ip) { mutableListOf() }

        // Remove old timestamps outside the time frame
        timestamps.removeIf { it < currentTime - TIME_FRAME_MS }

        // Check if the number of requests exceeds the limit
        if (timestamps.size >= MAX_REQUESTS) {
            return true
        }

        // Record the current request timestamp
        timestamps.add(currentTime)
        return false
    }

    //Blacklisting
    private val blacklistedIps = setOf("10.10.20.11", "10.10.12.111")

    private fun isBlacklisted(ip: String): Boolean {
        return blacklistedIps.contains(ip)
    }


    // Method to serve files from the assets folder
    private fun serveAssetFile(fileName: String, mimeType: String): Response {
        return try {
            // Open the file from assets
            val inputStream = context.assets.open(fileName)
            // Use 'bufferedReader()' and explicitly specify the type of 'reader'
            val content = inputStream.bufferedReader(Charsets.UTF_8).use { reader: BufferedReader ->
                reader.readText() // 'reader' is explicitly inferred as BufferedReader
            }
            newFixedLengthResponse(Response.Status.OK, mimeType, content)
        } catch (e: Exception) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File Not Found")
        }
    }

    private fun handleAddToQueue(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if(queueToggle) {
            val params = session.parameters

            val songName = params["song"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Song name is required")
            val artistName = params["artist"]?.firstOrNull() ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Artist name is required")
            // Process the song and artist
            val banned = utils.isBanned(songName, artistName)
            if (banned) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Song / Artist is banned")
            }
            if (addToQueue(songName, artistName)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Song added to queue successfully")
            } else {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Failed to add song to queue")
            }
        }
        else {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "adding to the queue is disabled")
        }
    }
    private fun handleBan(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response  {
        val params = session.parameters
        val songName = params["song"]?.firstOrNull() ?: null
        val artistName = params["artist"]?.firstOrNull() ?: null


        // Optionally, persist the banned list to a file (appending)
        utils.ban(songName,artistName)

        val response = utils.isBanned(songName,artistName)


        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "Song '$songName' by '$artistName' has been banned.")
    }


    private fun handleRestartThread(session: IHTTPSession): Response {
        val params = session.parameters

        // Extract the function ID from the request parameters
        val funId = params["id"]?.firstOrNull() ?: return newFixedLengthResponse(
            Response.Status.BAD_REQUEST,
            MIME_PLAINTEXT,
            "Missing thread ID"
        )

        // Extract the override parameter, defaulting to false if not provided
        val override = params["override"]?.firstOrNull()?.toBoolean() ?: false

        // Pass function ID and override to the callback
        restartCallback(funId, override)

        return newFixedLengthResponse("Thread $funId restarted successfully with override: $override!")
    }

    private fun handleGetQueue(): Response {
        return try {
            // Fetch the queue using the provided function
            val queue = fetchQueueFunction()

            // Create a JSON response
            val jsonResponse = JSONArray()
            for (song in queue) {
                val songObj = JSONObject()
                songObj.put("name", song["name"])
                songObj.put("artist", song["artist"])
                songObj.put("uri", song["uri"])
                jsonResponse.put(songObj)
            }

            newFixedLengthResponse(Response.Status.OK, MIME_JSON, jsonResponse.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error fetching queue")
        }
    }
    private fun handleRemoveFromQueue(session: IHTTPSession): NanoHTTPD.Response {
        val params = session.parameters

        // Extract parameters
        val songUri = params["uri"]?.firstOrNull() ?: return newFixedLengthResponse(
            Response.Status.BAD_REQUEST,
            MIME_PLAINTEXT,
            "Missing URI"
        )
        val playlistLink = params["playlistLink"]?.firstOrNull() ?: return newFixedLengthResponse(
            Response.Status.BAD_REQUEST,
            MIME_PLAINTEXT,
            "Missing Playlist Link"
        )

        // Proceed to handle the removal logic
        val (updatedQueue, filteredQueue, originalQueue) = removeSongFromQueue(songUri, playlistLink)

        // Function to convert a list of songs to a JSON array
        fun convertToJsonArray(songList: List<Map<String, String>>): JSONArray {
            val jsonArray = JSONArray()
            for (song in songList) {
                val songObj = JSONObject()
                songObj.put("name", song["name"])
                songObj.put("artist", song["artist"])
                songObj.put("uri", song["uri"])
                jsonArray.put(songObj)
            }
            return jsonArray
        }

        // Create a structured JSON response
        val jsonResponse = JSONObject()
        jsonResponse.put("updatedQueue", convertToJsonArray(updatedQueue))  // Songs that were successfully re-added
        jsonResponse.put("filteredQueue", convertToJsonArray(filteredQueue))  // Songs remaining after removal
        jsonResponse.put("originalQueue", convertToJsonArray(originalQueue))  // The original queue before modification

        return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse.toString(4)) // Pretty print JSON
    }

    private val sessionTokens = mutableSetOf<String>()

    private fun handleLogin(session: IHTTPSession): NanoHTTPD.Response {
        val password = "3e3f214d3c1c345ff29b3ce37ec33a9297eb6f16538b0e8857f28d6edf107e31"
        val params = session.parameters
        val pass = params["pass"]?.firstOrNull() ?: return newFixedLengthResponse(
            Response.Status.BAD_REQUEST,
            MIME_PLAINTEXT,
            "Missing password"
        )
        val clientIp = session.remoteIpAddress ?: "unknown"
        if (isRateLimited(clientIp)) {
            return newFixedLengthResponse(
                Response.Status.TOO_MANY_REQUESTS,
                MIME_PLAINTEXT,
                "Too many requests. Please try again later."
            )
        }

        return if (pass == password) {
            val token = UUID.randomUUID().toString() // Generate a unique session token
            sessionTokens.add(token) // Store the token in the session store

            // Create a response with a proper message (not interfering with the cookie)
            val response = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "Logged in successfully")

            // Set the session token in the cookie header
            response.addHeader("Set-Cookie", "sessionToken=$token; Path=/; HttpOnly")
            response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate")
            response.addHeader("Pragma", "no-cache")
            response.addHeader("Expires", "0")
            response.addHeader("Surrogate-Control", "no-store")

            response
        } else {
            newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_PLAINTEXT, "Incorrect password")
        }

    }
    private fun handleValidateSession(session: IHTTPSession): NanoHTTPD.Response {
        val cookies = session.cookies
        val token = cookies.read("sessionToken") ?: return newFixedLengthResponse(
            Response.Status.UNAUTHORIZED,
            MIME_PLAINTEXT,
            "Missing or invalid session token"
        )

        return if (sessionTokens.contains(token)) {
            newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "Session valid")
        } else {
            newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_PLAINTEXT, "Invalid session")
        }
    }
    fun loadDashboard(session: IHTTPSession): Response {
        val params = session.parameters
        // Extract parameters
        val authToken = params["authToken"]?.firstOrNull() ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Unauthorized")
        if (authToken == "oijoewh809ui")  {
            return serveAssetFile("AdminDash/Dash.html", "text/html")
        }
        return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Not authorized to access this page.")

    }
    fun loadDB(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val params = session.parameters

        // Extract parameters
        val authToken = params["authToken"]?.firstOrNull() ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Unauthorized"
        )
        if (authToken == "jfmwow9fn140sp0")  {
            return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Not authorized to access this page.")
        }
        else {
            return serveAssetFile("DBPanel/DBindex.html", "text/html")
        }
    }

    private fun fetchStudentsFromAzure(): Response {
        return getStudents()
    }

    private fun getBanned(): Response {
        val file = File(context.filesDir, "banned_list.txt")
        val bannedSongs = JSONArray()

        if (file.exists()) {
            file.forEachLine { line ->
                val parts = line.split(" - ")
                if (parts.size == 2) {
                    val jsonObject = JSONObject()
                    jsonObject.put("song", parts[0])
                    jsonObject.put("artist", parts[1])
                    bannedSongs.put(jsonObject)
                }
            }
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", bannedSongs.toString(4)) // Pretty print JSON
    }

}



