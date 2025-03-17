package com.example.physiokneeds_v3

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class PerfromExercises : AppCompatActivity() {

    lateinit var searchButton: Button
    lateinit var connectButton: Button
    lateinit var startTrackingButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var status: TextView

    var displayCount = 0

    private lateinit var displayManager: DisplayManager
    private var externalDisplayId: Int? = null
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}

        override fun onDisplayChanged(displayId: Int) {}

        override fun onDisplayRemoved(displayId: Int) {
            Log.d("NickDebug", "onDisplayRemoved")
            if (displayId == externalDisplayId) {
                Log.d("NickDebug", "onDisplayRemoved_if")
                val intent = Intent("com.example.CLOSE_EXTERNAL_ACTIVITY")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                status.text = "External Display Disconnected"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfrom_exercises)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // initialize buttons
        searchButton = findViewById(R.id.search_devices_button)
        connectButton = findViewById(R.id.connect_button)
        startTrackingButton = findViewById(R.id.start_tracking_button)

        progressBar = findViewById(R.id.progressBar)
        status = findViewById(R.id.status)

        // get routine from previous screen
        val routineConfig = intent.getSerializableExtra(HomeScreen.ROUTINE_TAG) as RoutineConfig?

        // automatic bluetooth
        Handler(Looper.getMainLooper()).postDelayed({
            if (displayCount >= 2) {
                searchButton.performClick()
            }
        }, 2000)  // Clicks after 2 seconds

        Handler(Looper.getMainLooper()).postDelayed({
            if (displayCount >= 2) {
                connectButton.performClick()
                status.text = "Complete the workout on your external monitor or TV."
                progressBar.visibility = ProgressBar.GONE
//            connectButton.visibility = Button.VISIBLE
//            connectButton.text = "Reconnect"
            }
        }, 5000)  // Clicks after 10 seconds

        // set clickListeners
        searchButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                Log.d("NickDebug", "search")
                val intent = Intent("com.example.PRESS_SEARCH_BUTTON")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        })

        connectButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                Log.d("NickDebug", "connect")
                val intent = Intent("com.example.PRESS_CONNECT_BUTTON")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        })

        startTrackingButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val intent = Intent("com.example.PRESS_START_BUTTON")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        })

        // secondary display
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)

        val displays = displayManager.displays
        val actOptions = ActivityOptions.makeBasic()

        displayCount = displays.size

        if (displayCount >= 2) {
            externalDisplayId = displays[1].displayId
            actOptions.launchDisplayId = externalDisplayId!!

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(HomeScreen.ROUTINE_TAG, routineConfig)

            startActivity(intent, actOptions.toBundle())
        } else {
            status.text = "Please Connect To External Display"
            progressBar.visibility = ProgressBar.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent("com.example.CLOSE_EXTERNAL_ACTIVITY")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        displayManager.unregisterDisplayListener(displayListener)
    }
}