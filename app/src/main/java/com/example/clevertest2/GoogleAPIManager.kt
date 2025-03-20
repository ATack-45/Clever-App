package com.example.clevertest2

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Events
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.joda.time.format.DateTimeFormat
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt

class GoogleApiManager(private val activity: MainActivity) {

    var fetchTodayThread: Thread? = null
    var fetchCalendarThread: Thread? = null

    // presentation ID


    fun saveCalendarIds(calendarIds: List<String>) {
        val sharedPreferences = activity.getSharedPreferences("YourAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Convert the list to a JSON string
        val json = Gson().toJson(calendarIds)

        // Save the JSON string in SharedPreferences
        editor.putString("calendar_ids_key", json)
        editor.apply()
    }
    fun savePresentationId(presentationId: String) {
        val sharedPreferences = activity.getSharedPreferences("YourAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save the presentation ID
        editor.putString("presentation_id_key", presentationId)
        editor.apply()
    }

    fun savePageObjectId(pageObjectId: String) {
        val sharedPreferences = activity.getSharedPreferences("YourAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save the page object ID
        editor.putString("page_object_id_key", pageObjectId)
        editor.apply()
    }


    fun getCalendarIds(): List<String> {
        val sharedPreferences = activity.getSharedPreferences("YourAppPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("calendar_ids_key", null)

        return if (json != null) {
            // Convert the JSON string back to a list
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, type)
        } else {
            // Default value if no data is stored
            emptyList()
        }
    }
    fun getPresentationId(): String? {
        val sharedPreferences = activity.getSharedPreferences("YourAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("presentation_id_key", null) // Default is `null` if not set
    }

    fun getPageObjectId(): String? {
        val sharedPreferences = activity.getSharedPreferences("YourAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("page_object_id_key", null) // Default is `null` if not set
    }



    // Fetch the document to update the background
    fun fetchTodayDocument(): Thread {

        val ConstraintLayout = activity.findViewById<ConstraintLayout>(R.id.main_layout)

        // Stop the previous thread if it's running
        fetchTodayThread?.interrupt()

        fetchTodayThread = Thread {
            while (!Thread.interrupted()) {
                // Load the service account key JSON file from res/raw
                val inputStream: InputStream =
                    activity.resources.openRawResource(R.raw.service_account)
                val credentials: GoogleCredentials = ServiceAccountCredentials
                    .fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/presentations.readonly"))

                // Refresh token if it’s expired
                credentials.refreshIfExpired()

                val accessToken = credentials.accessToken.tokenValue
                val presentation = getPresentationId()
                val slide = getPageObjectId()

                // Construct the URL for Google Slides API
                val url =
                    URL("https://slides.googleapis.com/v1/presentations/$presentation/pages/$slide/thumbnail?thumbnailProperties.mimeType=PNG&thumbnailProperties.thumbnailSize=LARGE")

                // Open a connection to the API endpoint
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.setRequestProperty("Accept", "application/json")

                // Check the response code and handle the response
                try {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read the response
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonResponse = JSONObject(response)
                        val thumbnailUrl = jsonResponse.getString("contentUrl")

                        Thread {
                            try {
                                val input = URL(thumbnailUrl).openStream()
                                val bitmap = BitmapFactory.decodeStream(input)

                                activity.runOnUiThread {
                                    ConstraintLayout.setBackground(
                                        BitmapDrawable(
                                            activity.resources,
                                            bitmap
                                        )
                                    )
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }.start()

                    } else {
                        val errorResponse =
                            connection.errorStream.bufferedReader().use { it.readText() }
                        println("Error fetching document: $errorResponse")
                    }
                } finally {
                    // Disconnect the connection after the request
                    connection.disconnect()
                }
                // Update document every 10 minutes
                try {
                    Thread.sleep(600000) // Sleep for 10 minutes
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()  // Restore the interruption status
                    break  // Exit the loop if interrupted
                }
            }
        }
        fetchTodayThread?.start()  // Start the new thread
        return fetchTodayThread!!
    }
    // Fetch Google Calendar events and update the UI
    fun fetchCalendarEvents(): Thread {
        // Interrupt any existing fetch thread.
        fetchCalendarThread?.interrupt()

        fetchCalendarThread = Thread {
            while (!Thread.interrupted()) {
                try {
                    // Load service account credentials from the raw resource.
                    val inputStream: InputStream = activity.resources.openRawResource(R.raw.service_account)
                    val credentials = ServiceAccountCredentials
                        .fromStream(inputStream)
                        .createScoped(listOf("https://www.googleapis.com/auth/calendar"))

                    credentials.refreshIfExpired()
                    // (Optional) Access token if needed.
                    val accessToken = credentials.accessToken.tokenValue

                    // Build the Google Calendar API service.
                    val calendarService = Calendar.Builder(
                        NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        HttpCredentialsAdapter(credentials)
                    )
                        .setApplicationName("YourAppName")
                        .build()

                    // Get all the calendar IDs you need to query.
                    val calendarIds = getCalendarIds().distinct() // Remove duplicates
                    // Convert HSL to RGB
                    fun hslToColor(h: Float, s: Float, l: Float): Int {
                        val c = (1 - Math.abs(2 * l - 1)) * s
                        val x = c * (1 - Math.abs((h / 60) % 2 - 1))
                        val m = l - c / 2
                        val (r, g, b) = when {
                            h < 60 -> Triple(c, x, 0f)
                            h < 120 -> Triple(x, c, 0f)
                            h < 180 -> Triple(0f, c, x)
                            else -> Triple(0f, x, c)
                        }
                        return Color.rgb(
                            ((r + m) * 255).roundToInt(),
                            ((g + m) * 255).roundToInt(),
                            ((b + m) * 255).roundToInt()
                        )
                    }

                    // Function to generate readable light greens
                    fun getReadableLightGreen(index: Int): Int {
                        val hueRange = listOf(95f, 110f, 130f, 145f, 160f) // Vary hues
                        val lightnessOptions = listOf(0.5f, 0.6f, 0.75f) // CHANGING SHADE OF GREEN
                        val saturation = 0.4f // Lower saturation for readability

                        val hue = hueRange[index % hueRange.size]
                        val lightness = lightnessOptions[index % lightnessOptions.size]

                        return hslToColor(hue, saturation, lightness)
                    }


                    // Wipe previous colors by creating a fresh assignment each time
                    val calendarColors = mutableMapOf<String, Int>().apply {
                        clear()
                        calendarIds.forEachIndexed { index, id ->
                            this[id] = getReadableLightGreen(index)
                        }
                    }



                    // Use a default color if the calendar ID is not in the mapping.
                    val defaultColor = Color.GREEN

                    val batch = calendarService.batch()

                    // Get "today" at the start of the day.
                    val now = org.joda.time.DateTime.now().withTimeAtStartOfDay()

                    // Calculate the start of the week (Monday) and each day of the current week.
                    val monday = now.withDayOfWeek(1)
                    val tuesday = now.withDayOfWeek(2)
                    val wednesday = now.withDayOfWeek(3)
                    val thursday = now.withDayOfWeek(4)
                    val friday = now.withDayOfWeek(5)
                    val saturday = now.withDayOfWeek(6)
                    val sunday = now.withDayOfWeek(7)

                    // Define the API time boundaries: from Monday at start-of-day to next Monday (exclusive).
                    val mondayApiDateTime = DateTime(monday.toDate())
                    val nextMondayApiDateTime = DateTime(monday.plusDays(7).toDate())

                    // Formatter for displaying dates (e.g., "Feb 24").
                    val formatter = DateTimeFormat.forPattern("MMM d")
                    val mondayFormatted = monday.toString(formatter)
                    val tuesdayFormatted = tuesday.toString(formatter)
                    val wednesdayFormatted = wednesday.toString(formatter)
                    val thursdayFormatted = thursday.toString(formatter)
                    val fridayFormatted = friday.toString(formatter)
                    val saturdayFormatted = saturday.toString(formatter)
                    val sundayFormatted = sunday.toString(formatter)

                    // Update the UI on the main thread.
                    activity.runOnUiThread {
                        // Set the title for each day.
                        activity.findViewById<TextView>(R.id.mondayTitle).text = "Monday, $mondayFormatted"
                        activity.findViewById<TextView>(R.id.tuesdayTitle).text = "Tuesday, $tuesdayFormatted"
                        activity.findViewById<TextView>(R.id.wednesdayTitle).text = "Wednesday, $wednesdayFormatted"
                        activity.findViewById<TextView>(R.id.thursdayTitle).text = "Thursday, $thursdayFormatted"
                        activity.findViewById<TextView>(R.id.fridayTitle).text = "Friday, $fridayFormatted"

                        // Clear previous event lists.
                        activity.findViewById<LinearLayout>(R.id.mondayEventList).removeAllViews()
                        activity.findViewById<LinearLayout>(R.id.tuesdayEventList).removeAllViews()
                        activity.findViewById<LinearLayout>(R.id.wednesdayEventList).removeAllViews()
                        activity.findViewById<LinearLayout>(R.id.thursdayEventList).removeAllViews()
                        activity.findViewById<LinearLayout>(R.id.fridayEventList).removeAllViews()


                        // Reset backgrounds for all day containers.



                        // Set the background for today's event list.
                        when (now.dayOfWeek().get()) {
                            1 -> activity.findViewById<LinearLayout>(R.id.mondaySection)
                                .setBackgroundResource(R.drawable.border)
                            2 -> activity.findViewById<LinearLayout>(R.id.tuesdaySection)
                                .setBackgroundResource(R.drawable.border)
                            3 -> activity.findViewById<LinearLayout>(R.id.wednesdaySection)
                                .setBackgroundResource(R.drawable.border)
                            4 -> activity.findViewById<LinearLayout>(R.id.thursdaySection)
                                .setBackgroundResource(R.drawable.border)
                            5 -> activity.findViewById<LinearLayout>(R.id.fridaySection)
                                .setBackgroundResource(R.drawable.border)

                        }
                    }

                    val uniqueCalendarIds = calendarIds.toSet().toList()
                    // Loop through each calendar and queue up the request.
                    uniqueCalendarIds.forEach { calendarId ->
                        val request = calendarService.events().list(calendarId)
                            .setTimeMin(mondayApiDateTime)
                            .setTimeMax(nextMondayApiDateTime)
                            .setSingleEvents(true)
                            .setOrderBy("startTime")

                        request.queue(batch, object : JsonBatchCallback<Events>() {
                            override fun onSuccess(events: Events, responseHeaders: HttpHeaders) {
                                events.items.forEach { event ->
                                    val summary = event.summary ?: "No Title"
                                    val startTime = event.start?.dateTime?.toString() ?: "No Start Time"

                                    // Determine the event date. For all-day events, use the date value.
                                    val eventDateMillis: Long = if (event.start?.date != null) {
                                        val startDateMillis = event.start.date.value ?: 0L
                                        val endDateMillis = event.end?.date?.value ?: startDateMillis
                                        endDateMillis
                                    } else {
                                        event.start?.dateTime?.value ?: 0L
                                    }
                                    val eventDate = org.joda.time.DateTime(eventDateMillis).withTimeAtStartOfDay()

                                    // Determine the color for this event based on its calendar.
                                    val eventColor = calendarColors[calendarId] ?: defaultColor

                                    // Update the appropriate day's event list based on the event date.
                                    activity.runOnUiThread {
                                        when {
                                            eventDate.isEqual(monday) -> {
                                                updateEventCard("Monday", summary, startTime, mondayFormatted, eventColor)
                                            }
                                            eventDate.isEqual(tuesday) -> {
                                                updateEventCard("Tuesday", summary, startTime, tuesdayFormatted, eventColor)
                                            }
                                            eventDate.isEqual(wednesday) -> {
                                                updateEventCard("Wednesday", summary, startTime, wednesdayFormatted, eventColor)
                                            }
                                            eventDate.isEqual(thursday) -> {
                                                updateEventCard("Thursday", summary, startTime, thursdayFormatted, eventColor)
                                            }
                                            eventDate.isEqual(friday) -> {
                                                updateEventCard("Friday", summary, startTime, fridayFormatted, eventColor)
                                            }
                                            eventDate.isEqual(saturday) -> {
                                                updateEventCard("Saturday", summary, startTime, saturdayFormatted, eventColor)
                                            }
                                            eventDate.isEqual(sunday) -> {
                                                updateEventCard("Sunday", summary, startTime, sundayFormatted, eventColor)
                                            }
                                        }

                                        startContinuousScroll(activity.findViewById(R.id.mondayScroll))
                                        startContinuousScroll(activity.findViewById(R.id.tuesdayScroll))
                                        startContinuousScroll(activity.findViewById(R.id.wednesdayScroll))
                                        startContinuousScroll(activity.findViewById(R.id.thursdayScroll))
                                        startContinuousScroll(activity.findViewById(R.id.fridayScroll))

                                    }
                                }
                            }

                            override fun onFailure(e: GoogleJsonError?, responseHeaders: HttpHeaders?) {
                                Log.e("CalendarAPI", "Failed to fetch events: ${e?.message}")
                            }
                        })
                    }

                    // Set a click listener for the calendar card.
                    // Set up collapse behavior:
                    val calendarCard = activity.findViewById<CardView>(R.id.calendarCard)
                    val calView = activity.findViewById<HorizontalScrollView>(R.id.calView)
                    // Instead of only setting click listeners on specific views,
                    // attach the collapse listener recursively to every view in the calendarCard.
                    activity.runOnUiThread {
                        attachCollapseListenerToAllViews(calendarCard)
                    }

                    // Execute the batched requests.
                    batch.execute()
                    Log.d("CalendarAPI", "Calendar events updated!")

                    // If needed, add a delayed handler here to restart fetchCalendarEvents() after some time.
                    break  // Exit the while loop (a delayed restart can be scheduled externally).

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fetchCalendarThread?.start()
        return fetchCalendarThread!!
    }
    private fun attachCollapseListenerToAllViews(view: View) {
        // (Optional: if you want to exclude certain views from triggering collapse,
        // you can add an "if" check here.)
        view.setOnClickListener {
            makeCalendarCardCollapsible()
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                attachCollapseListenerToAllViews(view.getChildAt(i))
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startContinuousScroll(scrollView: ScrollView) {
        val contentHeight = scrollView.getChildAt(0).height

        val animator = ObjectAnimator.ofInt(scrollView, "scrollY", 0, contentHeight).apply {
            duration = 40000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
        }

        // Pause scroll when user touches
        scrollView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> animator.pause()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> animator.resume()
            }
            false
        }

        animator.start()
    }


    // Update the event card in the correct section

    private fun updateEventCard(dateLabel: String, eventSummary: String?, eventStartTime: String?, formattedDate: String, eventColor: Int
    ) {
        // Choose the appropriate event list container based on the day label.
        val eventListView: LinearLayout = when (dateLabel.toLowerCase()) {
            "monday" -> activity.findViewById(R.id.mondayEventList)
            "tuesday" -> activity.findViewById(R.id.tuesdayEventList)
            "wednesday" -> activity.findViewById(R.id.wednesdayEventList)
            "thursday" -> activity.findViewById(R.id.thursdayEventList)
            "friday" -> activity.findViewById(R.id.fridayEventList)

            else -> return
        }


        // Create a drawable for rounded corners and background color
        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f // Rounded corners
            setColor(eventColor) // Background color
        }


        val specificString = "Daily Logic"


        val shouldAddToTop = eventSummary?.contains(specificString, ignoreCase = true) ?: false

        // Create TextView with margins and styling
        val eventTextView = TextView(activity).apply {
            text = eventSummary
            textSize = 14F
            setTextColor(Color.parseColor("#303030"))
            background = backgroundDrawable
            setPadding(30, 20, 30, 20)

            // Set margins
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8) // Margins around each event card
            }
            this.layoutParams = layoutParams
        }
        if (shouldAddToTop) {
            eventListView.addView(eventTextView, 0) // Insert at the top
        } else {
            eventListView.addView(eventTextView) // Add to the bottom
        }


    }

    private var isCollapsed = false  // Track collapse state
    var isDragging = false  // Track dragging state

    @SuppressLint("ClickableViewAccessibility")
    fun makeCalendarCardCollapsible() {
        Log.d("CalendarCard", "Collapsible method triggered")
        val calendarCard = activity.findViewById<CardView>(R.id.calendarCard)  // Get the calendar card
        val calView = activity.findViewById<HorizontalScrollView>(R.id.calView)
        val collapseIcon: ImageView = activity.findViewById(R.id.collapseIcon)


        if (calendarCard == null || calView == null) {
            return
        }



        // Values in px
        val fullHeightPx = 400
        val collapsedHeightPx = 50
        val fullWidthPx = 1230
        val collapsedWidthPx = 50

        // Convert px to dp using the screen density
        val density = activity.resources.displayMetrics.density
        val fullHeightDp = (fullHeightPx * density).toInt()
        val collapsedHeightDp = (collapsedHeightPx * density).toInt()
        val fullWidthDp = (fullWidthPx * density).toInt()
        val collapsedWidthDp = (collapsedWidthPx * density).toInt()

        try {
            calendarCard.post {
                val heightAnimator: ValueAnimator
                val widthAnimator: ValueAnimator

                if (isCollapsed) {
                    // Expand the card
                    heightAnimator = ValueAnimator.ofInt(collapsedHeightDp, fullHeightDp).apply {
                        duration = 300 // Animation duration in milliseconds
                        addUpdateListener { animator ->
                            val value = animator.animatedValue as Int
                            calendarCard.layoutParams.height = value // Adjust the height to expand the card
                            calendarCard.requestLayout() // Request layout update
                        }
                    }
                    widthAnimator = ValueAnimator.ofInt(collapsedWidthDp, fullWidthDp).apply {
                        duration = 300 // Animation duration in milliseconds
                        addUpdateListener { animator ->
                            val value = animator.animatedValue as Int
                            calendarCard.layoutParams.width = value // Adjust the width to expand the card
                            calendarCard.requestLayout() // Request layout update
                        }
                    }

                    // Ensure visibility for expansion
                    calView.visibility = View.VISIBLE
                    collapseIcon.visibility = View.GONE

                    // Start animations
                    heightAnimator.start()
                    widthAnimator.start()
                } else {
                    // Collapse the card
                    heightAnimator = ValueAnimator.ofInt(fullHeightDp, collapsedHeightDp).apply {
                        duration = 300 // Animation duration in milliseconds
                        addUpdateListener { animator ->
                            val value = animator.animatedValue as Int
                            calendarCard.layoutParams.height = value // Adjust the height to collapse the card
                            calendarCard.requestLayout() // Request layout update
                        }
                        doOnEnd {
                            calView.visibility = View.GONE // Hide the content after collapsing
                            collapseIcon.visibility = View.VISIBLE
                        }
                    }
                    widthAnimator = ValueAnimator.ofInt(fullWidthDp, collapsedWidthDp).apply {
                        duration = 300 // Animation duration in milliseconds
                        addUpdateListener { animator ->
                            val value = animator.animatedValue as Int
                            calendarCard.layoutParams.width = value // Adjust the width to collapse the card
                            calendarCard.requestLayout() // Request layout update
                        }
                    }

                    // Start animations
                    heightAnimator.start()
                    widthAnimator.start()
                }

                // Toggle the state after the animations
                isCollapsed = !isCollapsed  // This will be done after the animation ends
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            calendarCard.setOnTouchListener(object : View.OnTouchListener {
                private var dX = 0f
                private var dY = 0f
                private var lastAction = 0

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            dX = v.x - event.rawX
                            dY = v.y - event.rawY
                            lastAction = MotionEvent.ACTION_DOWN
                            isDragging = false // Start dragging, set to false initially
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            isDragging = true // Mark as dragging
                            v.animate()
                                .x(event.rawX + dX)
                                .y(event.rawY + dY)
                                .setDuration(0)
                                .start()
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            lastAction = MotionEvent.ACTION_UP
                            // Only trigger collapse/expand if not dragging
                            if (!isDragging) {
                                v.performClick()  // Trigger the click for collapse/expand
                            }
                            isDragging = false // Reset dragging after touch
                            return true
                        }
                        else -> return false
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}