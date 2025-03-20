package com.example.physiokneeds_v3

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    lateinit var endWorkoutButton: Button

    lateinit var routineData: RoutineDataUpload

    var isReceiverRegistered = false

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

    // broadcast receiver for controlling the external display
    private var buttonPressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.END_WORKOUT") {
                endWorkoutButton.performClick() // Simulate button press
            } else if (intent?.action == "com.example.ROUTINE_DATA_SEND") {
                Log.d("SEND_DATA", "got message for routine data")
                routineData = (intent.getSerializableExtra("RoutineData") as RoutineDataUpload?)!!
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
        endWorkoutButton = findViewById(R.id.end_workout)

        progressBar = findViewById(R.id.progressBar)
        status = findViewById(R.id.status)

        // get routine from previous screen
        val routineConfig = intent.getSerializableExtra(HomeScreen.ROUTINE_TAG) as RoutineConfig?

        // broadcast receivers for external display control
        val endFilter = IntentFilter("com.example.END_WORKOUT")
        val routineDataFilter = IntentFilter("com.example.ROUTINE_DATA_SEND")


        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, endFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, routineDataFilter)

        isReceiverRegistered = true

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

        endWorkoutButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                Log.d("FINAL_CRASH", "0")

                val intent = Intent(applicationContext, WorkoutComplete::class.java)
                Log.d("FINAL_CRASH", "1")

                if (routineConfig != null) {
                    Log.d("FINAL_CRASH", "2")

                    intent.putExtra(HomeScreen.ROUTINE_TAG, routineConfig)
                }

                Log.d("FINAL_CRASH", "3")

                if (::routineData.isInitialized) {
                    Log.d("FINAL_CRASH", "4")

                    intent.putExtra("RoutineData", routineData)
                }
                Log.d("FINAL_CRASH", "5")

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Log.d("FINAL_CRASH", "6")


                val intentClose = Intent("com.example.CLOSE_EXTERNAL_ACTIVITY")
                Log.d("FINAL_CRASH", "7")

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentClose)

                Log.d("FINAL_CRASH", "8")


                displayManager.unregisterDisplayListener(displayListener)
                Log.d("FINAL_CRASH", "9")

                applicationContext.startActivity(intent)

                Log.d("FINAL_CRASH", "10")

            }
        })

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

        if (isReceiverRegistered) {
            try {
                unregisterReceiver(buttonPressReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            isReceiverRegistered = false
        }
    }
}