package com.example.clevertest2


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi


class MainActivity : ComponentActivity() {
    //instantiating other files to access functions
    val spotifyManager = SpotifyManager(this)
    val utils = Utils(this)
    val googleApiManager = GoogleApiManager(this)
    val studentTracker = StudentTracker(this)
    private var webServer: WebServer? = null


    //thread tracking
    @RequiresApi(Build.VERSION_CODES.O)
    private val funs: MutableMap<String, (Boolean) -> Unit> = mutableMapOf(
        "fetchTodayDocument" to { _ -> googleApiManager.fetchTodayDocument() },
        "fetchStudentData" to { _ -> studentTracker.fetchStudentData() },
        "startSpotify" to { override -> spotifyManager.startSpotifyAuthorization(override) },
        "fetchCalendar" to { _ -> googleApiManager.fetchCalendarEvents() }
    )
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setting up layout
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val ip = getIpAccess()
        val ipDisplay = findViewById<TextView>(R.id.IP)
        ipDisplay.text = "IP: $ip:8080"

//        googleApiManager.savePresentationId("1r_dKTQbC2YXuMF-rTnOECxkNPUCkFq8otCXMYh3QXb0" )
//        googleApiManager.savePageObjectId("g318f68937f8_2_0")
//
//        val calendarIds = googleApiManager.getCalendarIds().toMutableList()
//        calendarIds.add("c_classroomf584d709@group.calendar.google.com")
//        googleApiManager.saveCalendarIds(calendarIds)

        //fix duplication
        //make dragable
        //starting APIs
        startFun("fetchStudentData")
        startFun("fetchTodayDocument")
        startFun("startSpotify")
        startFun("fetchCalendar")

        utils.loadBannedList()

//        //starting web server
        val context = this
        webServer = WebServer(8080,
            restartCallback = { funId, override ->
                restartFun(funId, override)
            },
            fetchQueueFunction = { spotifyManager.fetchSpotifyQueue() },
            removeSongFromQueue = { uri, playlist-> spotifyManager.removeSongFromQueue(uri, playlist)},
            getStudents = {studentTracker.getStudents()},
            addToQueue = { song, artist ->  spotifyManager.addToQueue(song,artist)},
            updateVars = { id, type -> varUpdate(id,type,googleApiManager)},
            boxBreath = {utils.boxBreather()},
            context
        )
        webServer!!.start()


    }

    @SuppressLint("DefaultLocale")
    private fun getIpAccess(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        val formatedIpAddress = String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )
        return formatedIpAddress //192.168.31.2
    }

    //thread management
    @RequiresApi(Build.VERSION_CODES.O)
    private fun restartFun(funId: String, override: Boolean = false) {
        // Stop the existing thread if it exists
        when (funId) {
            "fetchTodayDocument" -> {
                googleApiManager.fetchTodayThread?.interrupt()
                googleApiManager.fetchTodayThread = null
            }
            "fetchStudentData" -> {
                studentTracker.fetchStudentThread?.interrupt()
                studentTracker.fetchStudentThread = null
            }
            "startSpotify" -> {
                spotifyManager.fetchSpotifyThread?.interrupt()
                spotifyManager.fetchSpotifyThread = null
            }
            "fetchCalendar" -> {
                googleApiManager.fetchCalendarThread?.interrupt()
                googleApiManager.fetchCalendarThread = null
            }
        }

        Thread.sleep(500)  // 500 ms to give the thread time to clean up and stop
        // Start the function again with override parameter
        startFun(funId, override)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startFun(s: String, override: Boolean = false) {
        funs[s]?.invoke(override) ?: println("Function not found")

    }
//
    private fun varUpdate(id: String, type: String, googleManager : GoogleApiManager) : Boolean {

        when (type) {
            "Google Slideshow ID" -> googleManager.savePresentationId(id)
            "Google Slide ID" -> googleManager.savePageObjectId(id)
            "Google Calendar ID" -> {
                val calendarIds = googleManager.getCalendarIds().toMutableList()
                calendarIds.add(id)
                googleManager.saveCalendarIds(calendarIds)
            }
            "removeCalendarIds" ->{
                googleManager.saveCalendarIds(emptyList())
            }
            else -> return false
        }
        return true
    }
//
//    //spotify function that needs to be in MainActivity
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.data?.let { uri ->
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            val error = uri.getQueryParameter("error")

            if (error != null) {
                // Handle authorization error
            } else if (code != null) {
                // Proceed to exchange authorization code for an access token
                spotifyManager.exchangeAuthorizationCodeForAccessToken(code)
                val spotifyPlaying = findViewById<TextView>(R.id.playingTextView)
                spotifyManager.startTrackUpdates(spotifyPlaying) // Start fetching the track after obtaining the token
            }
        }
    }

}


