package com.example.clevertest2

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microsoft.signalr.HubConnectionBuilder
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.CountDownLatch
import javax.net.ssl.HttpsURLConnection


lateinit var studentAdapter: StudentAdapter

data class APIStudent(val firstName: String, val lastName: String, val id: String, val nfcId: String, val currentPunchout: String)
data class Student(val name: String, var timeGone: Int = 0)

class StudentAdapter(private val cardTextViews: List<TextView>) {
    private val students = arrayOfNulls<Student>(3) // Fixed size for 3 slots
    private val timers = arrayOfNulls<Job>(3) // Coroutines for each slot

    // Define colors for checked-in and checked-out states
    private val checkedOutColor = Color.parseColor("#AFC7A2")
    private val defaultColor = Color.parseColor("#303030")

    fun checkOutStudent(studentName: String, initialTimeGone: Int = 0) {
        val index = students.indexOfFirst { it == null } // Find the first empty slot
        if (index != -1) {
            val newStudent = Student(name = studentName, timeGone = initialTimeGone)
            students[index] = newStudent
            updateCard(index, newStudent)

            // Change card color to indicate checked-out state
            cardTextViews[index].setBackgroundColor(checkedOutColor)
            cardTextViews[index].setTextColor(defaultColor)
            cardTextViews[index].setPadding(0, 32, 0, 0) // Shift text down by 16dp

            // Start timer for the student
            timers[index] = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    delay(1000)
                    newStudent.timeGone++
                    updateCard(index, newStudent)
                }
            }
        }
    }

    fun checkInStudent(studentName: String) {
        val index = students.indexOfFirst { it?.name == studentName }
        if (index != -1) {
            // Cancel the timer for this slot
            timers[index]?.cancel()
            timers[index] = null

            // Shift students and timers to the left
            for (i in index until students.size - 1) {
                students[i] = students[i + 1]
                timers[i] = timers[i + 1]
                updateCard(i, students[i]) // Update the shifted card
                // Update card color for the shifted slot
                cardTextViews[i].setBackgroundColor(if (students[i] != null) checkedOutColor else defaultColor)
            }

            // Clear the last slot
            val lastIndex = students.size - 1
            students[lastIndex] = null
            timers[lastIndex]?.cancel()
            timers[lastIndex] = null
            cardTextViews[lastIndex].text = ""
            cardTextViews[lastIndex].setBackgroundColor(defaultColor)
        }
    }

    private fun updateCard(index: Int, student: Student?) {
        if (student == null) {
            cardTextViews[index].text = ""
        } else {
            val timeText = formatTime(student.timeGone)
            cardTextViews[index].text = "${student.name} - $timeText"
        }
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    fun clearAll() {
        for (i in students.indices) {
            timers[i]?.cancel()
            timers[i] = null
            students[i] = null
            cardTextViews[i].text = "Empty"
            cardTextViews[i].setBackgroundColor(defaultColor)
        }
    }
}

class StudentTracker(private val activity: MainActivity) {
    var fetchStudentThread: Thread? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchStudentData(): Thread {
        fetchStudentThread?.interrupt()

        fetchStudentThread = Thread {
            val card1TextView = activity.findViewById<TextView>(R.id.card1Text)
            val card2TextView = activity.findViewById<TextView>(R.id.card2Text)
            val card3TextView = activity.findViewById<TextView>(R.id.card3Text)
            studentAdapter = StudentAdapter(listOf(card1TextView, card2TextView, card3TextView))
            val url = URL("https://student-tracker-api.azurewebsites.net/api/student/getStudentsOut")
            val conn = url.openConnection() as HttpsURLConnection
            conn.setRequestProperty("ApiKey", "REMOVED FOR SECURITY")

            if (conn.responseCode == 200) {
                val inputStream = conn.inputStream
                val studentData = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(studentData)

                for (i in 0 until jsonArray.length()) {
                    val studentObj = jsonArray.getJSONObject(i)

                    // Extract student name
                    val student = studentObj.getJSONObject("student")
                    val firstName = student.getString("firstName")

                    // Extract timeout from punchout object
                    val punchout = studentObj.getJSONObject("punchout")
                    val timeOutStr = punchout.getString("timeout") // e.g., "2025-02-03T17:21:37.647"

                    try {
                        // Correctly parse without needing UTC 'Z'
                        val timeOutMillis = LocalDateTime.parse(timeOutStr)
                            .toInstant(ZoneOffset.UTC) // Assume UTC
                            .toEpochMilli()
                        val currentMillis = System.currentTimeMillis()
                        val timeGone = ((currentMillis - timeOutMillis) / 1000).toInt() // Convert to seconds
                        activity.runOnUiThread {
                            studentAdapter.checkOutStudent(firstName, timeGone)
                        }
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            }
            conn.disconnect()

            val hubConnection = HubConnectionBuilder.create("https://student-tracker-api.azurewebsites.net/punchoutHub")
                .withAccessTokenProvider(Single.defer { Single.just("NFJejnqGdi") })
                .build()

            try {
                hubConnection.start().blockingAwait()
                Log.d("SignalR", "Connection established")
            } catch (e: Exception) {
                Log.e("SignalR", "Connection failed: ${e.message}")
            }

            hubConnection.on(
                "PunchoutCreated",
                { _, student ->
                    try {
                        val studentMap = student as? Map<String, Any>
                            ?: throw IllegalArgumentException("Invalid student data")
                        val firstName = studentMap["firstName"] as? String ?: ""
                        activity.runOnUiThread {
                            studentAdapter.checkOutStudent(firstName)
                        }
                    } catch (e: Exception) {
                        Log.e("SignalR", "Error processing PunchoutCreated event", e)
                    }
                },
                Any::class.java,
                Any::class.java
            )

            hubConnection.on(
                "PunchoutClosed",
                { punchout ->
                    try {
                        val punchoutMap = punchout as? Map<String, Any>
                            ?: throw IllegalArgumentException("Invalid Punchout data")
                        val studentMap = punchoutMap["student"] as? Map<String, Any>
                            ?: throw IllegalArgumentException("Missing 'student' data")
                        val firstName = studentMap["firstName"] as? String ?: "Unknown"
                        activity.runOnUiThread {
                            studentAdapter.checkInStudent(firstName)
                        }
                    } catch (e: Exception) {
                        Log.e("SignalR", "Error processing PunchoutClosed event", e)
                    }
                },
                Any::class.java
            )
        }
        fetchStudentThread?.start()
        return fetchStudentThread!!
    }



fun getStudents(): NanoHTTPD.Response {
        val client = OkHttpClient()
        val url = "https://student-tracker-api.azurewebsites.net/api/student/getall"
        val request = Request.Builder()
            .url(url)
            .header("Host", "student-tracker-api.azurewebsites.net") // Add the original hostname
            .header("ApiKey", "REMOVED FOR SECURITY")
            .build()

        return try {
            // Execute the request and get the response
            val response : okhttp3.Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Extract the response body as a string
                val responseBody = response.peekBody(Long.MAX_VALUE).string()


                // Parse the JSON response into a list of APIStudent objects
                val gson = Gson()
                val studentListType = object : TypeToken<List<APIStudent>>() {}.type
                val students = gson.fromJson<List<APIStudent>>(responseBody, studentListType)

                // Create a response with the parsed data
                newFixedLengthResponse(Response.Status.OK, MIME_JSON, Gson().toJson(students))
            } else {
                // Handle HTTP error codes
                newFixedLengthResponse(Response.Status.OK, MIME_JSON, emptyList<APIStudent>().toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Return an empty response in case of failure
            newFixedLengthResponse(Response.Status.OK, MIME_JSON, emptyList<APIStudent>().toString())
        }
    }

@RequiresApi(Build.VERSION_CODES.O)
fun getSessionStudents(): List<String>  {
    val latch = CountDownLatch(1) // Latch to wait for the result
    val currentTime = LocalTime.now()
    val session = if (currentTime.isBefore(LocalTime.of(11, 30))) "AM" else "PM"
    val names = mutableListOf<String>()
    Thread{
        val url = URL("https://student-tracker-api.azurewebsites.net/api/student/get${session}")
        val conn = url.openConnection() as HttpsURLConnection
        try {
            conn.setRequestProperty("ApiKey", "REMOVED FOR SECURITY")

            if (conn.responseCode == 200) {
                val inputStream = conn.inputStream
                val studentData = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(studentData)

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val firstName = obj.getString("firstName")
                    val lastName = obj.getString("lastName")
                    names.add("$firstName $lastName")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log the error
        }finally {
            latch.countDown() // Signal that the thread has completed
            conn.disconnect()
        }
    }.start()

    latch.await() // Block until the background thread finishes
    return names
}
}






