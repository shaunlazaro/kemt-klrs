package com.example.physiokneeds_v3

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
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

        // set clickListeners
        searchButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val intent = Intent("com.example.PRESS_SEARCH_BUTTON")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);
            }
        })

        connectButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
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

        // secondary display test
        val displays = (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).displays
        val actOptions = ActivityOptions.makeBasic()

        if (displays.size >= 2) {
            actOptions.launchDisplayId = displays[1].displayId

            startActivity(Intent(this, MainActivity::class.java), actOptions.toBundle())
        }
    }
}