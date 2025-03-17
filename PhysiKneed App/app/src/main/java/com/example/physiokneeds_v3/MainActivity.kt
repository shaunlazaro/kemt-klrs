package com.example.physiokneeds_v3

import ConnectThread
import ConnectedThread
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.chaquo.python.Python
import com.google.gson.Gson
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.google.mlkit.vision.pose.PoseLandmark
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    // global variables
    lateinit var routineConfig: RoutineConfig
    var exerciseTrackers = mutableListOf<ExerciseTracker>()
    var repDataLists = mutableListOf<MutableList<RepData>>()

    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var handler:Handler
    lateinit var handlerThread: HandlerThread
    lateinit var overlayView:OverlayView
    lateinit var bitmap: Bitmap

    lateinit var leftText: TextView

    lateinit var textView_knee: TextView
    lateinit var textView_reps: TextView

    lateinit var reps_text: TextView

    lateinit var instructionsLayout : LinearLayout
//    lateinit var start_button: Button
//    lateinit var camera_button: Button

    lateinit var cameraDevice: CameraDevice
    lateinit var cameraCaptureSession: CameraCaptureSession
    val FRONT_CAMERA = 1
    val BACK_CAMERA = 0

    val CONFIDENCE_THRESHOLD = 0.6f

    // set which pose estimation model being used (lite/full/heavy)
    val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath("pose_landmarker_heavy.task")

    // set pose estimator options
    val optionsBuilder =
        PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .setMinPoseDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setNumPoses(1)
            .setRunningMode(RunningMode.IMAGE)

    val options = optionsBuilder.build()
    var poseLandmarker: PoseLandmarker? = null

    // bluetooth connectivity
    val TAG: String = "BTErrorLog"
    val REQUEST_ENABLE_BT: Int = 1
//    lateinit var handlerBT: Handler
    var arduinoBTModule: BluetoothDevice? = null
    var arduinoUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    lateinit var search_button: Button
    lateinit var bt_button: Button
    lateinit var connection_text: TextView
    lateinit var send_coords: Button

    lateinit var titleText: TextView
    lateinit var popupMenu: FrameLayout
    lateinit var exerciseTitle: TextView
    lateinit var exerciseNumberText: TextView
    lateinit var progressBarPopup: ProgressBar
    lateinit var leftImage: ImageView
    lateinit var alertBox: TextView
    lateinit var repTimer: TextView

    lateinit var connectedThreadWrite: ConnectedThread

    var startTime = 0.0.toLong()

    var isConnected = false

    var startTracking = false

    val COUNT_MAX = 10
    var counts = 0

    var currentPos = "90,45"

    var isReceiverRegistered = false

    var doneExercise = false

    // frame height and width
    var frameWidth by Delegates.notNull<Int>()
    var frameHeight by Delegates.notNull<Int>()


    // For exercise flow
    var state = 0
    var trackUserCount = 0
    var currentExerciseIndex = 0

    // set which camera the app opens first (FOR DEBUGGING)
    var current_camera = 0

    // For data tracking (end)
    lateinit var routineData: RoutineData

    // broadcast receiver for controlling the external display
    private var buttonPressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            textView_reps.text = "Received"
            Log.d(TAG, "Received")

            if (intent?.action == "com.example.PRESS_SEARCH_BUTTON") {
                search_button.performClick() // Simulate button press
            }
            if (intent?.action == "com.example.PRESS_CONNECT_BUTTON") {
                bt_button.performClick() // Simulate button press
                state = 1 // TODO remove when using mount
            }
            if (intent?.action == "com.example.PRESS_START_BUTTON") {
                Log.d(TAG, "Start Tracking Called")
                startTracking = true
                if (isConnected) {
                    Toast.makeText(
                        applicationContext,
                        "Tracking Will Now Start",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Bluetooth Mount Not Connected Please Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (intent?.action == "com.example.CLOSE_EXTERNAL_ACTIVITY") {
                finish()
            }
        }
    }

    // helper map
    val helperMap = mapOf(
        "nose" to PoseLandmark.NOSE,
        "left_eye_inner" to PoseLandmark.LEFT_EYE_INNER,
        "left_eye" to PoseLandmark.LEFT_EYE,
        "left_eye_outer" to PoseLandmark.LEFT_EYE_OUTER,
        "right_eye_inner" to PoseLandmark.RIGHT_EYE_INNER,
        "right_eye" to PoseLandmark.RIGHT_EYE,
        "right_eye_inner" to PoseLandmark.RIGHT_EYE_OUTER,
        "left_ear" to PoseLandmark.LEFT_EAR,
        "right_ear" to PoseLandmark.RIGHT_EAR,
        "left_mouth" to PoseLandmark.LEFT_MOUTH,
        "right_mouth" to PoseLandmark.RIGHT_MOUTH,
        "left_shoulder" to PoseLandmark.LEFT_SHOULDER,
        "right_shoulder" to PoseLandmark.RIGHT_SHOULDER,
        "left_elbow" to PoseLandmark.LEFT_ELBOW,
        "right_elbow" to PoseLandmark.RIGHT_ELBOW,
        "left_wrist" to PoseLandmark.LEFT_WRIST,
        "right_wrist" to PoseLandmark.RIGHT_WRIST,
        "left_pinky" to PoseLandmark.LEFT_PINKY,
        "right_pinky" to PoseLandmark.RIGHT_PINKY,
        "left_index" to PoseLandmark.LEFT_INDEX,
        "right_index" to PoseLandmark.RIGHT_INDEX,
        "left_thumb" to PoseLandmark.LEFT_THUMB,
        "right_thumb" to PoseLandmark.RIGHT_THUMB,
        "left_hip" to PoseLandmark.LEFT_HIP,
        "right_hip" to PoseLandmark.RIGHT_HIP,
        "left_knee" to PoseLandmark.LEFT_KNEE,
        "right_knee" to PoseLandmark.RIGHT_KNEE,
        "left_ankle" to PoseLandmark.LEFT_ANKLE,
        "right_ankle" to PoseLandmark.RIGHT_ANKLE,
        "left_heel" to PoseLandmark.LEFT_HEEL,
        "right_heel" to PoseLandmark.RIGHT_HEEL,
        "left_foot_index" to PoseLandmark.LEFT_FOOT_INDEX,
        "right_foot_index" to PoseLandmark.RIGHT_FOOT_INDEX)

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // get routine from previous screen
        routineConfig = (intent.getSerializableExtra(HomeScreen.ROUTINE_TAG) as RoutineConfig?)!!

        // create exercise tracker list and routine data tracker
        for (ex in routineConfig.exercises) {
            exerciseTrackers.add(ExerciseTracker(ex.exercise))
            repDataLists.add(mutableListOf<RepData>())
        }

        // Routine Data
        var routineComponentDataList = emptyList<RoutineComponentData>()
        routineData = RoutineData(routineConfig, routineComponentDataList)

        // connect to bluetooth
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter

        search_button = findViewById(R.id.search_button)
        bt_button = findViewById(R.id.bt_button)
        bt_button.isEnabled = false
        connection_text = findViewById(R.id.status_text)
        send_coords = findViewById(R.id.send_coords)

        leftText = findViewById(R.id.left_text)

        reps_text = findViewById(R.id.rep_count_text)

        // BT observable
        val connectToBTObservable = Observable.create<String> { emitter ->
            // Call the constructor of the ConnectThread class
            // Passing the Arguments: an Object that represents the BT device,
            // the UUID and then the handler to update the UI
            val connectThread = ConnectThread(arduinoBTModule, arduinoUUID, handler)
            connectThread.run()

            // Check if Socket connected
            if (connectThread.mmSocket.isConnected) {
                // The pass the Open socket as arguments to call the constructor of ConnectedThread
                connectedThreadWrite = ConnectedThread(connectThread.mmSocket)
                connectedThreadWrite.run()

                emitter.onNext("Connected")
                Log.d(TAG, "Arduino Device Connected")
            }
            // We could Override the onComplete function
            emitter.onComplete()
        }

        // broadcast receivers for external display control
        val searchFilter = IntentFilter("com.example.PRESS_SEARCH_BUTTON")
        val connectFilter = IntentFilter("com.example.PRESS_CONNECT_BUTTON")
        val startFilter = IntentFilter("com.example.PRESS_START_BUTTON")
        val closeFilter = IntentFilter("com.example.CLOSE_EXTERNAL_ACTIVITY")


        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, searchFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, connectFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, startFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, closeFilter)

        isReceiverRegistered = true

        // connect to ESP32
        bt_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (arduinoBTModule != null) {
                    Log.d(TAG, "Arduino Device Found")
                    // We subscribe to the observable until the onComplete() is called
                    // We also define control the thread management with
                    // subscribeOn:  the thread in which you want to execute the action
                    // observeOn: the thread in which you want to get the response
                    connectToBTObservable.observeOn(AndroidSchedulers.mainThread()).
                    subscribeOn(Schedulers.io()).
                    subscribe({ valueRead ->
                        // valueRead returned by the onNext() from the Observable
                        connection_text.setText(valueRead)
                        isConnected = true
                        Toast.makeText(
                            applicationContext,
                            "BT Mount Connected",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                } else {
                    Toast.makeText(
                        applicationContext,
                        "BT Mount Could Not Connect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        search_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                //Check if the phone supports BT
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                    Log.d(TAG, "Device doesn't support Bluetooth")
                } else {
                    Log.d(TAG, "Device support Bluetooth")
                    //Check BT enabled. If disabled, we ask the user to enable BT
                    if (!bluetoothAdapter.isEnabled) {
                        Log.d(TAG, "Bluetooth is disabled")
                        var enableBtIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        if (ActivityCompat.checkSelfPermission( applicationContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                        } else {
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                        }

                    } else {
                        Log.d(TAG, "Bluetooth is enabled")
                    }

                    val pairedDevices = bluetoothAdapter.bondedDevices

                    var deviceFound = false

                    if (pairedDevices.size > 0) {
                        for (device in pairedDevices) {
                            Log.d(TAG, device.name)
                            if (device.name == "RePose-BT") {
                                arduinoUUID = device.uuids[0].uuid
                                arduinoBTModule = device
                                bt_button.isEnabled = true
                                deviceFound = true

                                Toast.makeText(
                                    applicationContext,
                                    "BT Mount Found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    if (!deviceFound) {
                        Toast.makeText(
                            applicationContext,
                            "BT Mount Not Found In Paired Devices",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Log.d(TAG, "Button Pressed")
            }
        })

        send_coords.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
//                val random1 = (40..60).shuffled().last().toString()
//                val random2 = (40..60).shuffled().last().toString()
//                connectedThreadWrite.write(random1 + "," + random2)
                connectedThreadWrite.write("90,45")
                currentPos = "90,45"

            }
        })

        // get camera permissions from the user
        get_permissions()

        // define global variables
        textureView = findViewById(R.id.camera_feed)
        textureView.scaleX = -1f // mirror since front camera
//        textureView.rotation = 90f // Rotate 90 degrees clockwise
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        overlayView = findViewById(R.id.overlayView)
        overlayView.scaleX = -1f // mirror since front camera
//        overlayView.rotation = 90f // Rotate 90 degrees clockwise
        poseLandmarker = PoseLandmarker.createFromOptions(this, options)

        textView_knee = findViewById(R.id.kneeAngle)
        textView_reps = findViewById(R.id.repsCount)

        instructionsLayout = findViewById(R.id.instructions_layout)

        titleText = findViewById(R.id.title_text)
        popupMenu = findViewById(R.id.popup_panel)
        exerciseTitle = findViewById(R.id.exercise_title)
        exerciseNumberText = findViewById(R.id.exercise_number_text)
        progressBarPopup = findViewById(R.id.progress_bar)
        alertBox = findViewById(R.id.alert_box)
        repTimer = findViewById(R.id.rep_timer)

        // set the camera resolution to half the width
        val screenHeight = resources.displayMetrics.heightPixels
        val screenWidth = resources.displayMetrics.widthPixels
        val newWidth = round(screenHeight / (1.0/1.0)).toInt()

        Log.d("SCREEN_SIZE", newWidth.toString() + "x" + screenHeight)

        instructionsLayout.post {
            val layoutParams = instructionsLayout.layoutParams
            layoutParams.width = screenWidth - newWidth
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            instructionsLayout.layoutParams = layoutParams
            instructionsLayout.requestLayout()
            instructionsLayout.visibility = View.VISIBLE
        }

        textureView.post {
            val layoutParams = textureView.layoutParams
            layoutParams.width = newWidth
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            textureView.layoutParams = layoutParams
            textureView.requestLayout()
            textureView.visibility = View.VISIBLE
        }

        overlayView.post {
            val layoutParams = overlayView.layoutParams
            layoutParams.width = newWidth
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT  // Keep height flexible

            overlayView.layoutParams = layoutParams
            overlayView.requestLayout()
            overlayView.visibility = View.VISIBLE
        }

        // set frame height and width
        val layoutParams = textureView.layoutParams
        frameHeight = layoutParams.height
        frameWidth = layoutParams.width

//        start_button = findViewById(R.id.start_button)
//
//        // set up switch camera button (FOR DEBUGGING)
//        camera_button = findViewById(R.id.camera_button)
//        camera_button.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(view: View?) {
//                close_camera()
//                if (current_camera == 0) {
//                    open_camera(1)
//                    current_camera = 1
//                } else {
//                    open_camera(0)
//                    current_camera = 0
//                }
//            }
//        })

        // adjust camera preview
        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                open_camera(FRONT_CAMERA)
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

                Log.d("STATE_INFO", state.toString())

                // process each image from the camera feed
                bitmap = textureView.bitmap!!

                val mpImage = BitmapImageBuilder(bitmap).build()
                val result = poseLandmarker?.detect(mpImage)

                // reset frame width and height
                frameHeight = mpImage.height
                frameWidth = mpImage.width

                // display the keypoints and lines
                result?.let { overlayView.setResults(it, mpImage.height, mpImage.width) }

                // EXERCISE START

                if (state == 0) {
                    // update text
                    titleText.text = "Get Positioned"
                    leftText.text = "Stand 2 m away from camera \n\n Your entire body should be visible on the screen"
                    // TODO more polish for UI
                    // switch image to bottom
//                    val lastView = instructionsLayout.getChildAt(instructionsLayout.childCount - 1)
//                    instructionsLayout.removeView(lastView)
//                    instructionsLayout.addView(lastView, 0)
                    // TODO update image

                    if (result != null && result.landmarks().size > 0 && counts > COUNT_MAX) {
                        val landmarks = result.landmarks()[0]
                        val visibility = getLandmarksVisibility(landmarks)
                        val exerciseDetail = routineConfig.exercises[currentExerciseIndex].exercise

                        val keypointVisible = exerciseDetail.repKeypoints.all { kp ->
                            (visibility[kp] ?: 0f) > CONFIDENCE_THRESHOLD
                        }
                        if (keypointVisible) {
                            track_user(result, mpImage.height, mpImage.width)
                        } else {
                            // TODO add pop up that keypoints aren't visible (light, in frame, stay still etc.)
                        }
                        counts = 0
                    }
                    // update counter
                    counts += 1
                }
                else if (state == 1) {
                    // switch image to top
//                    val lastView = instructionsLayout.getChildAt(instructionsLayout.childCount - 1)
//                    instructionsLayout.removeView(lastView)
//                    instructionsLayout.addView(lastView, 0)

                    titleText.text = routineConfig.exercises[currentExerciseIndex].exercise.displayName
                    leftText.text = "1. Sit facing sideways to the camera.\n\n2. Raise and lower your leg slowly"
                    reps_text.visibility = View.VISIBLE
                    repTimer.visibility = View.VISIBLE

                    popupMenu.visibility = View.VISIBLE
                    exerciseTitle.text = routineConfig.exercises[currentExerciseIndex].exercise.displayName
                    exerciseNumberText.text = "Exercise #" + (currentExerciseIndex+1)

//                    while (progressBarPopup.progress < progressBarPopup.max) {
//                        Thread.sleep(10)
//                        progressBarPopup.progress += 10
////                        Log.d("NICK_STATE", progressBarPopup.progress.toString())
//                    }

                    progressBarPopup.progress = 1000

                    Handler(Looper.getMainLooper()).postDelayed({
                        state = 2 // start next state
                        popupMenu.visibility = View.GONE // hide pop up
                    }, 3000)  // update progress

                    startTime = System.currentTimeMillis() / 1000
                }
                else if (state == 2) {

                    // update rep timer
                    updateRepTimer()

                    // TODO update to the instructions in the exercise class
                    // perform exercise
                    if (result != null && result.landmarks().size > 0) {
//                        Log.d("PORT_DEBUG", result.landmarks().toString())
                        val landmarks = result.landmarks()[0]
                        val visibility = getLandmarksVisibility(landmarks)
                        val exerciseDetail = routineConfig.exercises[currentExerciseIndex].exercise

                        val keypointVisible = exerciseDetail.repKeypoints.all { kp ->
                            (visibility[kp] ?: 0f) > CONFIDENCE_THRESHOLD
                        }

                        if (keypointVisible) {
//                            Log.d("ROUTINE_DEBUG", landmarks.size.toString())

                            val trackingResults = processExerciseMetrics(
                                landmarks, exerciseDetail
                            )

                            val repData = exerciseTrackers[currentExerciseIndex]
                                .detectReps(trackingResults, exerciseDetail, resultToPose(result))

                            // update rep data
                            if (repData != null) {
                                repDataLists[currentExerciseIndex].add(repData)

                                // show alerts from repData (kinda jank)
                                Log.d("ALERT_LOG", "Rep Number: " + repData.repNumber)
                                Log.d("ALERT_LOG", "Max Angle: " + repData.maxExtension)
                                Log.d("ALERT_LOG", "Alert Size: " + repData.alerts.size.toString())

                                if (repData.alerts.size > 0) {
                                    var alertText = ""
                                    var alertCount = 1
                                    for (alert in repData.alerts) {
                                        if (alertCount == repData.alerts.size) {
                                            alertText = alertText + alert
                                        } else {
                                            alertText = alertText + alert + " & "
                                        }
                                        Log.d("ALERT_LOG", "Alert:" + alert)
                                        alertCount++
                                    }
                                    alertBox.text = alertText
                                    alertBox.visibility = View.VISIBLE

                                    Handler(Looper.getMainLooper()).postDelayed({
                                        alertBox.visibility = View.GONE
                                    }, 1500)  // update progress
                                }
                            }

                            // TODO add smooth angle
                            // REVISIT the equals check condition
//                            val repTrackingAngle = trackingResults
//                                .firstOrNull { it.detail.trackingType == exerciseDetail.repTracking.trackingType }?.angle
//
////                            val repTrackingAngle = trackingResults[0].angle
//
//                            Log.d(
//                                "ROUTINE_DEBUG",
//                                "repTrackingAngle " + repTrackingAngle.toString()
//                            )
                        }
                    }

                    // even when keypoints aren't visible we will still update UI
                    reps_text.text =
                        exerciseTrackers[currentExerciseIndex].repCount.toString() +
                                " / " + routineConfig.exercises[currentExerciseIndex].reps + " reps     "

                    // check if exercise is done
                    if (exerciseTrackers[currentExerciseIndex].repCount == routineConfig.exercises[currentExerciseIndex].reps) {
                        // done exercise
                        if (currentExerciseIndex < routineConfig.exercises.size - 1) {
                            currentExerciseIndex++ // update index
                            state = 3 // next exercise
                        } else {
                            state = 4 // done workout
                        }
                    }
                }
                else if (state == 3) {
                    reps_text.text =
                        "0 / " + routineConfig.exercises[currentExerciseIndex].reps + " reps     "
                    popupMenu.visibility = View.VISIBLE
                    exerciseNumberText.visibility = View.GONE
                    exerciseTitle.text = "Exercise Completed"

//                    while (progressBarPopup.progress < progressBarPopup.max) {
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            progressBarPopup.progress += 10
//                        }, 10)  // update progress
//                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        state = 1 // start next state
                        exerciseNumberText.visibility = View.VISIBLE
//                        popupMenu.visibility = View.GONE // hide pop up
                    }, 3000)  // update progress
                }
                else if (state == 4) {
                    popupMenu.visibility = View.VISIBLE
                    exerciseTitle.text = "Workout Complete"
                    exerciseNumberText.text = "Nice Work!"
//                    while (progressBarPopup.progress < progressBarPopup.max) {
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            progressBarPopup.progress += 10
//                        }, 10)  // update progress
//                    }
                    // end process

                    // send data format
                    var routineDataList = mutableListOf<RoutineComponentData>()
                    var index = 0
                    for (repDataList in repDataLists) {
                        val routineComponentData = RoutineComponentData(routineConfig.exercises[index], repDataList)
                        routineDataList.add(routineComponentData)
                        index++
                    }

                    val routineData = RoutineData(routineConfig, routineDataList)
//                    sendData(routineData)

                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(applicationContext, WorkoutComplete::class.java)
                        intent.putExtra(HomeScreen.ROUTINE_TAG, routineConfig)

                        startActivity(intent)
                        finish()
                    }, 10000)  // finish after 10 seconds
                }
            }

//                // check if any keypoints are detected before processing
//                if (result != null && result.landmarks().size > 0 && result.landmarks().get(0).get(0).visibility().orElse(0.0f) > 0.6f) {
//                    // process and show results for the specified exercise
//                    seated_knee_extension(result)
//                    if (counts > COUNT_MAX && !doneExercise) {
//                        track_user(result, mpImage.height, mpImage.width)
//                        counts = 0
//                    }
//                }

//                textView_reps.text = counts.toString()
        }
    }

    private fun updateRepTimer() {
        val time = (System.currentTimeMillis() / 1000 - startTime).toInt()
        if (time < 60) {
            if (time < 10) {
                repTimer.text = "   0:0" + time.toString() + "   "
            } else {
                repTimer.text = "   0:" + time.toString() + "   "
            }
        } else {
            if (time%60 < 10) {
                repTimer.text = "   "+ time.floorDiv(60) + ":0" + time%60 + "   "
            } else {
                repTimer.text = "   "+ time.floorDiv(60) + ":" + time%60 + "   "
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to avoid memory leaks
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(buttonPressReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            isReceiverRegistered = false
        }
        // close socket
//        connectedThreadWrite.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun get_permissions() {
        // camera permissions
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(kotlin.arrayOf(android.Manifest.permission.CAMERA), 101)
        }

        // bluetooth permissions
        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(kotlin.arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: kotlin.Int,
        permissions: kotlin.Array<out kotlin.String>,
        grantResults: kotlin.IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            get_permissions()
        }
    }

    @SuppressLint("MissingPermission")
    fun open_camera(camera_index: Int) {
        cameraManager.openCamera(cameraManager.cameraIdList[camera_index], object:CameraDevice.StateCallback() {
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0
                val captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                val surface = Surface(textureView.surfaceTexture)

                captureRequest.addTarget(surface)

                p0.createCaptureSession(listOf(surface), object:CameraCaptureSession.StateCallback() {
                    override fun onConfigured(p0: CameraCaptureSession) {
                        cameraCaptureSession = p0
                        p0.setRepeatingRequest(captureRequest.build(), null, null)
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {

                    }

                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {
                p0.close()
            }

            override fun onError(p0: CameraDevice, p1: Int) {
                p0.close()
            }

        }, handler)
    }

    fun close_camera() {
        try {
            cameraCaptureSession.close()
            cameraDevice.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun track_user(result: PoseLandmarkerResult, height: Int, width: Int) {
        // Get coordinates of all points
        val allPoints = mutableListOf<Array<Float>>()
        for (landmark in result.landmarks()[0]) {
            allPoints.add(arrayOf(landmark.x(), landmark.y()))
        }
        // Get coords to send
        try {
            val motorCoords = getCoords(allPoints, height, width, currentPos)
            if (motorCoords != "DNM") {
                currentPos = motorCoords
                Log.d("CUSTOMTAG", currentPos)
                connectedThreadWrite.write(motorCoords)
                trackUserCount = 0
            } else if (trackUserCount >= 10) {
                // if the camera hasn't moved for 10 iterations, stop moving camera
                state = 1
                trackUserCount = 0
            } else {
                // increase trackUserCount if not moved and less than 10
                Log.d("CUSTOMTAG", currentPos)
                trackUserCount += 1
            }
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred:", e)
        }


//        try {
//            // initialize python (Chaquopy)
//            val py = Python.getInstance()
//            val motorCoords = py.getModule("processing")
//                .callAttr("get_coords", allPoints.toTypedArray(), height, width, currentPos)
//
//            if (motorCoords.toString() != "DNM") {
////                textView_knee.text = motorCoords.toString()
//                currentPos = motorCoords.toString()
//                Log.d(TAG, currentPos)
//                connectedThreadWrite.write(motorCoords.toString())
//            }
//
//        } catch (e: Exception) {
//            // Log the stack trace to Logcat
//            Log.e("PythonError", "An error occurred:", e)
//        }
    }

    fun seated_knee_extension(result: PoseLandmarkerResult) {
        val CONFIDENCE_THRESHOLD = 0.6f  // Adjust based on desired accuracy

        // if all points are visible with above the threshold
        if (result.landmarks().get(0).get(PoseLandmark.RIGHT_HIP).visibility()
                .orElse(0.0f) > CONFIDENCE_THRESHOLD &&
            result.landmarks().get(0).get(PoseLandmark.RIGHT_KNEE).visibility()
                .orElse(0.0f) > CONFIDENCE_THRESHOLD &&
            result.landmarks().get(0).get(PoseLandmark.RIGHT_ANKLE).visibility()
                .orElse(0.0f) > CONFIDENCE_THRESHOLD) {

            // get 3d keypoints of the three landmarks
            val rightHip = listOf(result.landmarks().get(0).get(PoseLandmark.RIGHT_HIP).x(),
                result.landmarks().get(0).get(PoseLandmark.RIGHT_HIP).y(),
                result.landmarks().get(0).get(PoseLandmark.RIGHT_HIP).z()).toTypedArray()

            val rightKnee = listOf(result.landmarks().get(0).get(PoseLandmark.RIGHT_KNEE).x(),
                result.landmarks().get(0).get(PoseLandmark.RIGHT_KNEE).y(),
                result.landmarks().get(0).get(PoseLandmark.RIGHT_KNEE).z()).toTypedArray()

            val rightAnkle = listOf(result.landmarks().get(0).get(PoseLandmark.RIGHT_ANKLE).x(),
                result.landmarks().get(0).get(PoseLandmark.RIGHT_ANKLE).y(),
                result.landmarks().get(0).get(PoseLandmark.RIGHT_ANKLE).z()).toTypedArray()

            // calculate the angle and count reps
            try {
                // initialize python (Chaquopy)
                val py = Python.getInstance()
                val kneeAngle = py.getModule("processing")
                    .callAttr("calculate_angle", rightHip, rightKnee, rightAnkle)

//                val reps = py.getModule("processing")
//                    .callAttr("detect_reps", kneeAngle)

//                val repsList = exerciseTrackerKneeExt.detectReps(kneeAngle.toDouble())

                Log.d("ExerciseTracker", kneeAngle.toDouble().toString())

//                textView_knee.text = "Right Knee Angle: " + kneeAngle.toString()
//                reps_text.text = "Rep " + repsList[0].toString() + "/ 10"

            } catch (e: Exception) {
                // Log the stack trace to Logcat
                Log.e("PythonErrorKnee", "An error occurred:", e)
            }
        }
    }


    fun getMotorCoordsOffset(midpoint: Int, frameSize: Int): Double {
        val MOTOR_RATIO = 23.0 / (frameSize / 2.0) // Distance from center to edge in motor coords (angle)
        return (midpoint - frameSize / 2.0) * MOTOR_RATIO
    }

    fun getCoords(allPoints: List<Array<Float>>, h: Int, w: Int, current: String): String {
        val FRAME_WIDTH = w
        val FRAME_HEIGHT = h

        val TOLERANCE_X = 80 * 23.0 / (FRAME_WIDTH / 2.0)
        val TOLERANCE_Y = 80 * 23.0 / (FRAME_HEIGHT / 2.0)

        val MAX_MOVE_DISTANCE: Int = 25

        // Parse current motor positions
        val currentSplit =
            current.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val currentX = currentSplit[0].toInt()
        val currentY = currentSplit[1].toInt()

        var motorX = currentX // Horizontal motor
        var motorY = currentY // Vertical motor

        // Calculate the midpoint of all landmarks
        var sumX = 0
        var sumY = 0
        for (point in allPoints) {
            sumX += point[0].toInt()
            sumY += point[1].toInt()
        }
        val midpointX = sumX.toDouble() / allPoints.size
        val midpointY = sumY.toDouble() / allPoints.size

        // Convert midpoint to pixel coordinates
        val pixelMidpointX = (midpointX * w).toInt()
        val pixelMidpointY = (midpointY * h).toInt()

        // Calculate offsets from frame center
        val offsetX = getMotorCoordsOffset(pixelMidpointX, FRAME_WIDTH)
        val offsetY = getMotorCoordsOffset(pixelMidpointY, FRAME_HEIGHT)

        // Check if offsets exceed tolerance
        if (abs(offsetX) > TOLERANCE_X || abs(offsetY) > TOLERANCE_Y) {
            // Update motor positions
            if (offsetX > TOLERANCE_X && motorX > 0) {
                motorX += min(
                    Math.round(offsetX).toInt().toDouble(),
                    MAX_MOVE_DISTANCE.toDouble()
                ).toInt()
            } else if (offsetX < -TOLERANCE_X && motorX < 180) {
                motorX += max(
                    Math.round(offsetX * 23).toInt().toDouble(),
                    -MAX_MOVE_DISTANCE.toDouble()
                ).toInt()
            }

            if (offsetY > TOLERANCE_Y && motorY > 0) {
                motorY -= min(
                    Math.round(offsetY).toInt().toDouble(),
                    MAX_MOVE_DISTANCE.toDouble()
                ).toInt()
            } else if (offsetY < -TOLERANCE_Y && motorY < 180) {
                motorY -= max(
                    Math.round(offsetY).toInt().toDouble(),
                    -MAX_MOVE_DISTANCE.toDouble()
                ).toInt()
            }

            // Keep motor values within bounds
            motorX = max(0.0, min(180.0, motorX.toDouble())).toInt()
            motorY = max(0.0, min(180.0, motorY.toDouble())).toInt()

            return "$motorX,$motorY" // Return updated motor coordinates
        } else {
            return "DNM" // Do Not Move
        }
    }

    fun extractKeypointsDynamic(
        landmarks: List<NormalizedLandmark>,
        keypoints: List<String>,
        mode: String = "2D"): List<List<Float>> {

        require(mode != "3D" || (frameWidth != null && frameHeight != null)) {
            "Frame width and height must be provided for 3D mode."
        }

        val extractedPoints = mutableListOf<List<Float>>()

        for (kp in keypoints) {
            val landmark = landmarks[helperMap.get(kp)!!]

            if (mode == "3D") {
                val refZ = landmarks[helperMap.get(keypoints[0])!!].z()
                extractedPoints.add(
                    listOf(
                        landmark.x() * frameWidth,
                        landmark.y() * frameHeight,
                        landmark.z() - refZ
                    )
                )
            } else if (mode == "2D") {
                extractedPoints.add(
                    listOf(
                        landmark.x() * frameWidth,
                        landmark.y() * frameHeight
                    )
                )
            }
        }

        return extractedPoints
    }

    fun detectLeadingSide(landmarks: List<NormalizedLandmark>): String {
        val rightKneeZ = landmarks[PoseLandmark.RIGHT_KNEE].z()
        val leftKneeZ = landmarks[PoseLandmark.LEFT_KNEE].z()
        return if (rightKneeZ < leftKneeZ) "right" else "left"
    }

    fun processExerciseMetrics(
        landmarks: List<NormalizedLandmark>,
        exerciseDetail: ExerciseDetail): List<TrackingResult> {

        val trackingResults = mutableListOf<TrackingResult>()

        for (trackingDetail in exerciseDetail.defaultTrackingDetails) {
            val keypoints = if (trackingDetail.symmetric) {
                val side = detectLeadingSide(landmarks)
                trackingDetail.keypoints.map { "${side}_$it" }.also {
                    if (trackingDetail == exerciseDetail.repTracking) {
                        exerciseDetail.repKeypoints = it
                    }
                }
            } else trackingDetail.keypoints

            val extractedLandmarks = extractKeypointsDynamic(
                landmarks, keypoints, trackingDetail.dimensionality
            )

            try {
                // initialize python (Chaquopy)
                val py = Python.getInstance()

                val exerciseAngle = when (trackingDetail.trackingType) {
                    // TODO trackingType is a string rn
                    "Angle of three points" ->
                        py.getModule("processing")
                            .callAttr(
                                "calculate_three_point_angle",
                                extractedLandmarks[0].toTypedArray(),
                                extractedLandmarks[1].toTypedArray(),
                                extractedLandmarks[2].toTypedArray())
                    "Angle with vertical" ->
                        py.getModule("processing")
                            .callAttr(
                                "calculate_two_point_vertical_angle",
                                extractedLandmarks[0].toTypedArray(),
                                extractedLandmarks[1].toTypedArray())
                    "Angle with horizontal" ->
                        py.getModule("processing")
                            .callAttr(
                                "calculate_two_point_horizontal_angle",
                                extractedLandmarks[0].toTypedArray(),
                                extractedLandmarks[1].toTypedArray())
                    else -> throw IllegalArgumentException("Unsupported tracking type: ${trackingDetail.trackingType}")
                }

                trackingResults.add(TrackingResult(trackingDetail, exerciseAngle.toDouble()))

            } catch (e: Exception) {
                // Log the stack trace to Logcat
                Log.e("PythonErrorProcessExerciseMetrics", "An error occurred:", e)
            }
        }

        return trackingResults
    }

    fun getLandmarksVisibility(
        landmarks: List<NormalizedLandmark>): Map<String, Float> {

        val landmarkMap = mapOf(
            "nose" to landmarks[PoseLandmark.NOSE].visibility().orElse(0f),
            "left_eye_inner" to landmarks[PoseLandmark.LEFT_EYE_INNER].visibility().orElse(0f),
            "left_eye" to landmarks[PoseLandmark.LEFT_EYE].visibility().orElse(0f),
            "left_eye_outer" to landmarks[PoseLandmark.LEFT_EYE_OUTER].visibility().orElse(0f),
            "right_eye_inner" to landmarks[PoseLandmark.RIGHT_EYE_INNER].visibility().orElse(0f),
            "right_eye" to landmarks[PoseLandmark.RIGHT_EYE].visibility().orElse(0f),
            "right_eye_inner" to landmarks[PoseLandmark.RIGHT_EYE_OUTER].visibility().orElse(0f),
            "left_ear" to landmarks[PoseLandmark.LEFT_EAR].visibility().orElse(0f),
            "right_ear" to landmarks[PoseLandmark.RIGHT_EAR].visibility().orElse(0f),
            "left_mouth" to landmarks[PoseLandmark.LEFT_MOUTH].visibility().orElse(0f),
            "right_mouth" to landmarks[PoseLandmark.RIGHT_MOUTH].visibility().orElse(0f),
            "left_shoulder" to landmarks[PoseLandmark.LEFT_SHOULDER].visibility().orElse(0f),
            "right_shoulder" to landmarks[PoseLandmark.RIGHT_SHOULDER].visibility().orElse(0f),
            "left_elbow" to landmarks[PoseLandmark.LEFT_ELBOW].visibility().orElse(0f),
            "right_elbow" to landmarks[PoseLandmark.RIGHT_ELBOW].visibility().orElse(0f),
            "left_wrist" to landmarks[PoseLandmark.LEFT_WRIST].visibility().orElse(0f),
            "right_wrist" to landmarks[PoseLandmark.RIGHT_WRIST].visibility().orElse(0f),
            "left_pinky" to landmarks[PoseLandmark.LEFT_PINKY].visibility().orElse(0f),
            "right_pinky" to landmarks[PoseLandmark.RIGHT_PINKY].visibility().orElse(0f),
            "left_index" to landmarks[PoseLandmark.LEFT_INDEX].visibility().orElse(0f),
            "right_index" to landmarks[PoseLandmark.RIGHT_INDEX].visibility().orElse(0f),
            "left_thumb" to landmarks[PoseLandmark.LEFT_THUMB].visibility().orElse(0f),
            "right_thumb" to landmarks[PoseLandmark.RIGHT_THUMB].visibility().orElse(0f),
            "left_hip" to landmarks[PoseLandmark.LEFT_HIP].visibility().orElse(0f),
            "right_hip" to landmarks[PoseLandmark.RIGHT_HIP].visibility().orElse(0f),
            "left_knee" to landmarks[PoseLandmark.LEFT_KNEE].visibility().orElse(0f),
            "right_knee" to landmarks[PoseLandmark.RIGHT_KNEE].visibility().orElse(0f),
            "left_ankle" to landmarks[PoseLandmark.LEFT_ANKLE].visibility().orElse(0f),
            "right_ankle" to landmarks[PoseLandmark.RIGHT_ANKLE].visibility().orElse(0f),
            "left_heel" to landmarks[PoseLandmark.LEFT_HEEL].visibility().orElse(0f),
            "right_heel" to landmarks[PoseLandmark.RIGHT_HEEL].visibility().orElse(0f),
            "left_foot_index" to landmarks[PoseLandmark.LEFT_FOOT_INDEX].visibility().orElse(0f),
            "right_foot_index" to landmarks[PoseLandmark.RIGHT_FOOT_INDEX].visibility().orElse(0f))

        return landmarkMap
    }

    fun resultToPose(results : PoseLandmarkerResult): Pose? {
        if (results.landmarks()[0] != null) {
            val landmarksList = results.landmarks()[0].mapIndexed { i, lm ->
                Landmark(i, lm.x(), lm.y(), lm.z(), lm.visibility().orElse(0f))
            }
            return Pose(landmarksList)
        } else {
            return null
        }
    }

    private fun sendData(data: RoutineData) {
        // create retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl("http://140.238.151.117:8000/api/routine-data/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // send routine-data
        val call = apiService.sendData(data)

        call?.enqueue(object : Callback<RoutineData> {
            override fun onResponse(
                call: Call<RoutineData>,
                response: Response<RoutineData>
            ) {
                if (!response.isSuccessful) {
                    // Handle the error scenario here
                    Log.e(HomeScreen.TAG_API, "Response Code: " + response.code())
                    Log.d(HomeScreen.TAG_API, call.request().url.toString())
                    return
                } else {
                    Log.d(HomeScreen.TAG_API, "Data Sent Successfully")
                }
            }

            override fun onFailure(call: Call<RoutineData>, t: Throwable) {
                Log.e(HomeScreen.TAG_API, t.toString())
            }
        })
    }
}