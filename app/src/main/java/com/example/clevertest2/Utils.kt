package com.example.clevertest2

import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

class Utils(private val activity: MainActivity) {
    val bannedList = mutableListOf<Pair<String, String>>() // List of banned songs and artists
    var strictName: String? = null

    fun formatTime(ms: Int): String {
        val minutes = ms / 60000
        val seconds = (ms % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun boxBreather(): Boolean{
        val studentAPI = StudentTracker(activity)
        val students = studentAPI.getSessionStudents()
        val randomNames =pickTwoRandomNames(students)
        showNamesWithAnimation(randomNames)
        return true
    }
    fun pickTwoRandomNames(names: List<String>): Pair<String, String?> {
        if (names.size < 2) throw IllegalArgumentException("At least 2 names are required")
        val shuffled = names.shuffled()
        if (strictName != null){
            val randomNumber = Random.nextInt(1, 6)  // Generates a random number between 1 and 5 (inclusive)
            if (randomNumber == 1){
                return Pair(shuffled[0], "Emmanuel Martinez")
            }
            return Pair(shuffled[0], strictName)
        }
        else {
            return Pair(shuffled[0], shuffled[1])
        }
    }
    fun scrambleText(original: String): String {
        return original.toCharArray()
            .apply { shuffle() } // Shuffle the characters in the original string
            .joinToString("")
    }


    fun showNamesWithAnimation(names: Pair<String, String?>) {
        val nameLayout: LinearLayout = activity.findViewById(R.id.nameLayout)
        val nameTextView1: TextView = activity.findViewById(R.id.nameTextView)
        val nameTextView2: TextView = activity.findViewById(R.id.nameTextView2)
        val closeButton: ImageButton = activity.findViewById(R.id.closeButton)

        nameLayout.visibility = View.VISIBLE

        animateUnscrambleText(nameTextView1, names.first)
        names.second?.let { animateUnscrambleText(nameTextView2, it) }

        // Automatically hide the layout after 1 minute
        CoroutineScope(Dispatchers.Main).launch {
            delay(60_000) // 1 minute in milliseconds
            nameLayout.visibility = View.GONE
            nameLayout.requestLayout()  // Request layout update
            nameLayout.invalidate()    // Force re-draw of the layout
        }

        // Set up close button to hide the container
        closeButton.setOnClickListener {
            Log.d("CloseButton", "Button clicked")
            nameLayout.visibility = View.GONE
            nameLayout.requestLayout()  // Request layout update
            nameLayout.invalidate()    // Force re-draw of the layout
        }
        strictName = null
    }

    fun animateUnscrambleText(textView: TextView, name: String, totalDuration: Long = 6000L) {
        // Extract prefix and name part

        val totalSteps = name.length * 2 // Scrambling + unscrambling phases
        val delayPerStep = totalDuration / totalSteps

        CoroutineScope(Dispatchers.Main).launch {
            val currentText = CharArray(name.length) { ' ' }
            val scrambledChars = name.toCharArray().toMutableList().apply { shuffle() }

            // Phase 1: Scrambling phase
            for (step in 0 until name.length) {
                val randomText = generateRandomChars(name.length)
                textView.text = buildString {
                    append(randomText.mapIndexed { index, _ ->
                        if (index <= step) scrambledChars[index] else randomText[index]
                    }.joinToString(""))
                }
                delay(delayPerStep)
            }

            // Phase 2: Unscrambling phase
            for (step in 0 until name.length) {
                currentText[step] = name[step]
                textView.text = buildString {
                    append(currentText.mapIndexed { index, c ->
                        if (index > step) scrambledChars[index] else c
                    }.joinToString(""))
                }
                delay(delayPerStep)
            }

            textView.text = name // Ensure the final text is correct
        }
    }

    // Helper function to generate random strings
    fun generateRandomChars(length: Int): String {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length).map { chars.random() }.joinToString("")
    }




    fun generateRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }
    // Load the banned list from a file
    fun loadBannedList() {
        val file = File(activity.filesDir, "banned_list.txt")

        if (!file.exists()) {
            file.createNewFile()
        }
        if (file.exists()) {
            file.forEachLine { line ->
                val parts = line.split(" - ")
                if (parts.size == 2) {
                    bannedList.add(parts[0] to parts[1]) // songName to artistName
                }
            }
        }
    }

    fun ban(song : String?, artist : String?) : Boolean{
        bannedList.add((song to artist) as Pair<String, String>)

        try{
            val file = File(activity.filesDir, "banned_list.txt")
            file.appendText("$song - $artist\n") // Append to the file

        } catch(e: Exception){
            e.printStackTrace()
            return false
        }

        return true

    }

    // Check if a song or artist is banned
    fun isBanned(songName: String?, artistName: String?): Boolean {
        loadBannedList()

        // Check if either the song name or artist name is banned
        return bannedList.any {
            it.first.equals(songName, true) || it.second.equals(artistName, true)
        }
    }


    fun extractGoogleIds(url: String): Triple<String?, String?, String>? {
        val slideshowRegex = Regex("https://docs.google.com/presentation/d/([a-zA-Z0-9-_]+)")
        val slideRegex = Regex("https://docs.google.com/presentation/d/([a-zA-Z0-9-_]+)/edit#slide=id.([a-zA-Z0-9-_]+)")
        val calendarRegex = Regex("https://calendar.google.com/calendar/embed\\?src=([a-zA-Z0-9%_.-]+)")

        return when {
            slideRegex.containsMatchIn(url) -> {
                val match = slideRegex.find(url)
                val presentationId = match?.groupValues?.get(1)
                val slideId = match?.groupValues?.get(2)
                Triple(presentationId, slideId, "Google Slide ID")
            }
            slideshowRegex.containsMatchIn(url) -> {
                val match = slideshowRegex.find(url)
                val presentationId = match?.groupValues?.get(1)
                Triple(presentationId, null, "Google Slideshow ID")
            }
            calendarRegex.containsMatchIn(url) -> {
                val match = calendarRegex.find(url)
                val calendarId = match?.groupValues?.get(1)?.replace("%40", "@")
                Triple(calendarId, null, "Google Calendar ID")
            }
            else -> null
        }
    }


}