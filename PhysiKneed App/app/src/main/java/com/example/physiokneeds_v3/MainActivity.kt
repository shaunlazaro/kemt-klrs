package com.example.physiokneeds_v3

import ConnectThread
import ConnectedThread
import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Range
import android.view.Gravity
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
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
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
import java.io.IOException
import java.util.Locale
import java.util.UUID
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

    lateinit var cameraDevice: CameraDevice
    lateinit var cameraCaptureSession: CameraCaptureSession
    val FRONT_CAMERA = 1

    val CONFIDENCE_THRESHOLD = 0.6f

    // set which pose estimation model being used (lite/full/heavy)
    val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath("pose_landmarker_full.task")

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
    val connectingDelay = 5000
    var arduinoBTModule: BluetoothDevice? = null
    var arduinoUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothSocket: BluetoothSocket? = null // Shared socket reference
    var btButtonCount = 0

    lateinit var search_button: Button
    lateinit var bt_button: Button
    lateinit var connection_text: TextView

    lateinit var titleText: TextView
    lateinit var popupMenu: FrameLayout
    lateinit var exerciseTitle: TextView
    lateinit var exerciseNumberText: TextView
    lateinit var progressBarPopup: ProgressBar
    lateinit var leftImage: ImageView
    lateinit var alertBox: TextView
    lateinit var repTimer: TextView
    lateinit var tipText: TextView
    lateinit var loadingBar: ProgressBar
    lateinit var getPosText: TextView
    lateinit var completeText: TextView
    lateinit var angleBar: ProgressBar

    private lateinit var soundPool: SoundPool
    var playSound = false

    lateinit var connectedThreadWrite: ConnectedThread

    var startTime = 0.0.toLong()

    var isConnected = false

    val COUNT_MAX = 2
    val TRACK_COUNT_MAX = 3 *(10/COUNT_MAX)

    var counts = 0

    var isReceiverRegistered = false

    // frame height and width
    var frameWidth by Delegates.notNull<Int>()
    var frameHeight by Delegates.notNull<Int>()

    // For exercise flow
    var state = 100
    var trackUserCount = 0
    var currentExerciseIndex = 0

    // FPS verification test
    var fpsData = mutableListOf<Int>()
    var firstFPS = true
    var countFPS = 0
    var startTimeFPS = System.currentTimeMillis()

    var exerciseSkipped = false

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
            }
            if (intent?.action == "com.example.CLOSE_EXTERNAL_ACTIVITY") {
                state = -1
                Handler(Looper.getMainLooper()).postDelayed({
                    if (::connectedThreadWrite.isInitialized) {
                        connectedThreadWrite?.write("RESET")
                    }
                    finish()
                }, 1000)
            }
            if (intent?.action == "com.example.SKIP_EXERCISE") {
                exerciseSkipped = true
                if (currentExerciseIndex < routineConfig.exercises.size - 1) {
                    currentExerciseIndex++ // update index
                    val intentIndex = Intent("com.example.VIEW_INSTRUCTIONS") // send broadcast that it is the next exercise
                    intentIndex.putExtra("EXERCISE_INDEX", currentExerciseIndex)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentIndex)
                    state = 3 // next exercise
                } else {
                    state = 4 // done workout
                }
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

        // connect to bluetooth
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter

        search_button = findViewById(R.id.search_button)
        bt_button = findViewById(R.id.bt_button)
        bt_button.isEnabled = false
        connection_text = findViewById(R.id.status_text)

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
                bluetoothSocket = connectThread.mmSocket
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
        val closeFilter = IntentFilter("com.example.CLOSE_EXTERNAL_ACTIVITY")
        val skipWorkoutFilter = IntentFilter("com.example.SKIP_EXERCISE")


        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, searchFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, connectFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, closeFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressReceiver, skipWorkoutFilter)

        isReceiverRegistered = true

        // connect to ESP32
        bt_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (arduinoBTModule != null && btButtonCount < 1) {
                    Log.d(TAG, "Arduino Device Found")
                    isConnected = true
                    btButtonCount++

                    state = 1 // start next state

                    // We subscribe to the observable until the onComplete() is called
                    // We also define control the thread management with
                    // subscribeOn:  the thread in which you want to execute the action
                    // observeOn: the thread in which you want to get the response
                    connectToBTObservable.observeOn(AndroidSchedulers.mainThread()).
                    subscribeOn(Schedulers.io()).
                    subscribe({ valueRead -> })
                } else if (arduinoBTModule == null) {
                    Toast.makeText(
                        applicationContext,
                        "BT Mount Could Not Connect",
                        Toast.LENGTH_SHORT
                    ).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        bt_button.performClick()
                        titleText.text = "Reconnecting To Mount..."
                    }, connectingDelay.toLong())

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

        // define global variables
        textureView = findViewById(R.id.camera_feed)
        textureView.isOpaque = false
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        overlayView = findViewById(R.id.overlayView)
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
        tipText = findViewById(R.id.tips_text)
        leftImage = findViewById(R.id.left_image)
        loadingBar = findViewById(R.id.progressBarMain)
        getPosText = findViewById(R.id.getPositionedBox)
        completeText = findViewById(R.id.exercise_subtext)
        angleBar = findViewById(R.id.angle_bar)

        val videoFeed = findViewById<VideoView>(R.id.video)

        // for sound effects
        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Max number of simultaneous sounds
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        val beginRepSound = soundPool.load(applicationContext, R.raw.begin_rep, 1)
        val errorSound = soundPool.load(applicationContext, R.raw.error, 1)
        val singleRepSound = soundPool.load(applicationContext, R.raw.single_rep, 1)
        val repsCompleteSound = soundPool.load(applicationContext, R.raw.reps_complete, 1)

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

                if (state == 100) {
                    titleText.text = "Connecting To Mount..."

                    popupMenu.visibility = View.VISIBLE
                    exerciseTitle.text = "Connecting To Mount..."
                    exerciseNumberText.text = "Please Wait..."

                    state = -1
                    progressBarPopup.isIndeterminate = true
                }
                else if (state == 0) {
                    loadingBar.visibility = View.VISIBLE
                    getPosText.visibility = View.VISIBLE

                    if (result != null && result.landmarks().size > 0 && counts > COUNT_MAX) {
                        val landmarks = result.landmarks()[0]
                        val visibility = getLandmarksVisibility(landmarks)
                        val exerciseDetail = routineConfig.exercises[currentExerciseIndex].exercise

                        val keypointVisible = exerciseDetail.repKeypoints.all { kp ->
                            (visibility[kp] ?: 0f) > CONFIDENCE_THRESHOLD
                        }

                        val trackingKeypoints = listOf("left_hip", "right_hip", "left_shoulder", "right_shoulder")

                        val keypointVisibleForTracking = trackingKeypoints.all { kp ->
                            (visibility[kp] ?: 0f) > CONFIDENCE_THRESHOLD
                        }

                        if (keypointVisible) {
                            track_user(result, mpImage.height, mpImage.width, true)
                        } else if (keypointVisibleForTracking) {
                            track_user(result, mpImage.height, mpImage.width, false)
                        }
                        counts = 0
                    }
                    // update counter
                    counts += 1
                }
                else if (state == 1) {
                    reps_text.visibility = View.GONE
                    repTimer.visibility = View.GONE

                    angleBar.visibility = View.GONE

                    titleText.text = routineConfig.exercises[currentExerciseIndex].exercise.displayName

                    val htmlText = "<b>TIPS</b><br>" + "<br>" +
                            routineConfig.exercises[currentExerciseIndex].exercise.instruction.replace("\\n", "<br>")

                    val styledText = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    leftText.gravity = Gravity.NO_GRAVITY

                    videoFeed.visibility = View.VISIBLE
                    // update text
                    leftText.visibility = View.VISIBLE

                    leftText.text = styledText
                    if (routineConfig.exercises[currentExerciseIndex].exercise.displayName == "Seated Leg Extension (Right)") {
                        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.seated_leg_extension)
                        videoFeed.setVideoURI(videoUri)
                        videoFeed.start()
                        videoFeed.setOnCompletionListener {
                            videoFeed.start()
                        }
                    } else if (routineConfig.exercises[currentExerciseIndex].exercise.displayName == "Squat") {
                        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.squat_01)
                        videoFeed.setVideoURI(videoUri)
                        videoFeed.start()
                        videoFeed.setOnCompletionListener {
                            videoFeed.start()
                        }
                    } else if (routineConfig.exercises[currentExerciseIndex].exercise.displayName == "Hamstring Curl (Right)") {
                        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.hamstring_curl)
                        videoFeed.setVideoURI(videoUri)
                        videoFeed.start()
                        videoFeed.setOnCompletionListener {
                            videoFeed.start()
                        }
                    } else {
                        leftImage.visibility = View.VISIBLE
                        videoFeed.visibility = View.GONE
                        leftImage.setImageResource(R.drawable.baseline_fitness_center_24)
                    }

                    popupMenu.visibility = View.VISIBLE
                    exerciseTitle.text = routineConfig.exercises[currentExerciseIndex].exercise.displayName
                    exerciseNumberText.text = "Exercise #" + (currentExerciseIndex+1)

                    state = -1
                    val loadingDuration = 5000

                    progressBarPopup.isIndeterminate = false;
                    moveProgressBar(0,progressBarPopup.max,loadingDuration.toLong())

                    Handler(Looper.getMainLooper()).postDelayed({
                        state = 0 // start next state
                        popupMenu.visibility = View.GONE // hide pop up
                    }, loadingDuration.toLong())  // update progress

                    playSound = true

                }
                else if (state == 2) {

                    loadingBar.visibility = View.GONE
                    getPosText.visibility = View.GONE

                    reps_text.visibility = View.VISIBLE
                    repTimer.visibility = View.VISIBLE

                    angleBar.visibility = View.VISIBLE
                    val angleBarMin = exerciseTrackers[currentExerciseIndex].minForBar?.toInt()!!
                    angleBar.max = exerciseTrackers[currentExerciseIndex].maxForBar?.toInt()!! - angleBarMin

                    // FPS verification test during exercise state
                    if (firstFPS) {
                        startTimeFPS = System.nanoTime()
                        firstFPS = false
                    } else if ((System.nanoTime() - startTimeFPS) >= 1000000000) {
                        fpsData.add(countFPS)
                        startTimeFPS = System.nanoTime()
                        countFPS = 0
                    } else {
                        countFPS++
                    }

                    if (playSound) {
                        soundPool.play(beginRepSound, 1f, 1f, 0, 0, 1f)
                    }

                    playSound = false

                    // update rep timer
                    updateRepTimer()

                    // perform exercise
                    if (result != null && result.landmarks().size > 0) {
                        val landmarks = result.landmarks()[0]
                        val visibility = getLandmarksVisibility(landmarks)
                        val exerciseDetail = routineConfig.exercises[currentExerciseIndex].exercise

                        val keypointVisible = exerciseDetail.repKeypoints.all { kp ->
                            (visibility[kp] ?: 0f) > CONFIDENCE_THRESHOLD
                        }

                        if (keypointVisible) {
                            val trackingResults = processExerciseMetrics(
                                landmarks, exerciseDetail
                            )

                            // angle bar
                            var angleForBar = exerciseTrackers[currentExerciseIndex]
                                .getAngleBarVal(trackingResults, exerciseDetail)
                                ?.toInt()!!
                            angleBar.progress = angleForBar - angleBarMin

                            if (angleForBar >= exerciseTrackers[currentExerciseIndex].maxForBar) {
                                angleBar.progressDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.rep_angle_bar_full)
                            }
                            else if (angleForBar >= exerciseTrackers[currentExerciseIndex].angleValGreen) {
                                angleBar.progressDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.rep_angle_bar_gr)
                            } else if (angleForBar >= exerciseTrackers[currentExerciseIndex].maxForBar / 4) {
                                angleBar.progressDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.rep_angle_bar_y)
                            } else {
                                angleBar.progressDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.rep_angle_bar)
                            }

                            val repData = exerciseTrackers[currentExerciseIndex]
                                .detectReps(trackingResults, exerciseDetail, resultToPose(result))

                            // update rep data
                            if (repData != null) {
                                soundPool.play(singleRepSound, 1f, 1f, 0, 0, 1f)

                                Log.d("POSE_FRAME_DEBUG", "Total Time: " + repData.totalTime.toString())

                                Log.d("SYMMETRIC_DEBUG", "Rep Number: " + repData.repNumber)

                                repDataLists[currentExerciseIndex].add(repData)

                                // show alerts from repData (kinda jank)
                                Log.d("ALERT_LOG", "Rep Number: " + repData.repNumber)
                                Log.d("ALERT_LOG", "Max Angle: " + repData.maxExtension)
                                Log.d("ALERT_LOG", "Alert Size: " + repData.alerts.size.toString())
                            }
                        }
                    }

                    // even when keypoints aren't visible we will still update UI
                    reps_text.text =
                        exerciseTrackers[currentExerciseIndex].repCount.toString() +
                                " / " + routineConfig.exercises[currentExerciseIndex].reps + " reps     "

                    // real time alerts
                    if (exerciseTrackers[currentExerciseIndex].alertTriggers.size > 0) {
                        soundPool.play(errorSound, 1f, 1f, 0, 0, 1f)
                        var alertText = ""
                        var alertCount = 1
                        for (alert in exerciseTrackers[currentExerciseIndex].alertTriggers) {
                            if (alertCount == exerciseTrackers[currentExerciseIndex].alertTriggers.size) {
                                if (exerciseTrackers[currentExerciseIndex].alertTriggers.size == 1){
                                    alertText = alertText + alert
                                } else {
                                    alertText = alertText + alert.lowercase(Locale.ROOT)
                                }
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
                        }, 3000)  // update progress
                    }

                    // check if exercise is done
                    if (exerciseTrackers[currentExerciseIndex].repCount == routineConfig.exercises[currentExerciseIndex].reps) {
                        // done exercise

                        Log.d("FPS_VERIFICATION", fpsData.toString())

                        if (currentExerciseIndex < routineConfig.exercises.size - 1) {
                            currentExerciseIndex++ // update index
                            val intentIndex = Intent("com.example.VIEW_INSTRUCTIONS") // send broadcast that it is the next exercise
                            intentIndex.putExtra("EXERCISE_INDEX", currentExerciseIndex)
                            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentIndex)
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

                    if (!exerciseSkipped) {
                        soundPool.play(repsCompleteSound, 1f, 1f, 1, 0, 1f)
                        exerciseTitle.text = "Exercise Completed"
                    } else {
                        exerciseTitle.text = "Exercise Skipped"
                        exerciseSkipped = false
                    }

                    state = -1
                    val loadingDuration = 3000
                    moveProgressBar(0,progressBarPopup.max,loadingDuration.toLong())

                    Handler(Looper.getMainLooper()).postDelayed({
                        state = 1 // start next state
                        exerciseNumberText.visibility = View.VISIBLE
                    }, loadingDuration.toLong())  // update progress
                }
                else if (state == 4) {

                    soundPool.play(repsCompleteSound, 1f, 1f, 1, 0, 1f)

                    // exit state machine
                    state = -1

                    popupMenu.visibility = View.VISIBLE
                    exerciseTitle.text = "Workout Complete"
                    exerciseNumberText.text = "Nice Work!"

                    progressBarPopup.visibility = View.GONE
                    completeText.visibility = View.VISIBLE

                    if (::connectedThreadWrite.isInitialized) {
                        connectedThreadWrite.write("RESET")
                    }

                    // send data format
                    var routineDataList = mutableListOf<RoutineComponentDataUpload>()
                    var index = 0

                    val gson = Gson()
                    val jsonDataFull = gson.toJson(repDataLists) // Serialize the data
                    Log.d("JSON_REPDATA", "jsonFull " + jsonDataFull)

                    for (repDataList in repDataLists) {
                        val routineComponentData = RoutineComponentDataUpload(routineConfig.exercises[index].exercise.id, repDataList)
                        routineDataList.add(routineComponentData)
                        index++
//                        val gson = Gson()
//                        val jsonData = gson.toJson(repDataList) // Serialize the data

//                        for (repData in repDataList) {
//                            Log.d("NICK_AHHHH", "Poses data: " + Gson().toJson(repData.poses))
//                        }
//
//                        Log.d("JSON_REPDATA", "repData Pose size: " + repDataList[0].poses.size)
//                        Log.d("JSON_REPDATA", "json " + jsonData)
                    }

                    val routineData = RoutineDataUpload(routineConfig.id, routineDataList)

                    // send routine data to next screen
                    val intentRoutine = Intent("com.example.ROUTINE_DATA_SEND")
                    intentRoutine.putExtra("RoutineData", routineData)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentRoutine)

                    // switch in progress buttons
                    val intent = Intent("com.example.END_WORKOUT")
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
            }
        }
    }

    fun moveProgressBar(startVal: Int, endVal: Int, duration: Long) {
        val animator: ObjectAnimator =
            ObjectAnimator.ofInt(progressBarPopup, "progress", startVal, endVal)
        animator.setDuration(duration)
        animator.start()
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

    private fun disconnectBluetooth() {
        Log.d("NICK_BT", "disconnectBluetooth Called")

        try {
            bluetoothSocket?.let { socket ->
                if (socket.isConnected) {
                    socket.close()
                    Log.d("NICK_BT", "Bluetooth socket closed successfully")
                } else {
                    Log.d("NICK_BT", "Socket already disconnected")
                }
            } ?: Log.d("NICK_BT", "No active Bluetooth socket to close")
        } catch (e: IOException) {
            Log.e("NICK_BT", "Error closing socket: ${e.message}")
        } finally {
            isConnected = false
        }
    }


    override fun onDestroy() {
        Log.d("TAG1234", "onDestroyCalled")

        // Unregister the receiver to avoid memory leaks
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(buttonPressReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            isReceiverRegistered = false
        }

        disconnectBluetooth()

        val intent = Intent("com.example.CLOSE_EXTERNAL_ACTIVITY")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        super.onDestroy()
        Log.d("TAG1234", "onDestroyCalledAfter")

    }

    @SuppressLint("MissingPermission")
    fun open_camera(camera_index: Int) {
        cameraManager.openCamera(cameraManager.cameraIdList[camera_index], object:CameraDevice.StateCallback() {
            override fun onOpened(p0: CameraDevice) {

                val fpsRanges: Array<out Range<Int>>? =
                    cameraManager.getCameraCharacteristics(cameraManager.cameraIdList[camera_index]).get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                if (fpsRanges != null) {
                    for (range in fpsRanges) {
                        Log.d(
                            "FPS_RANGE",
                            ("Min: " + range.getLower()).toString() + " Max: " + range.getUpper()
                        )
                    }
                }

                cameraDevice = p0
                val captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                    fpsRanges?.get(fpsRanges.size -1) ?: Range(30, 30)
                )

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

    fun track_user(result: PoseLandmarkerResult, height: Int, width: Int, changeState: Boolean) {
        // Get coordinates of all points
        val allPoints = mutableListOf<Array<Float>>()
        for (landmark in result.landmarks()[0]) {
            allPoints.add(arrayOf(landmark.x(), landmark.y()))
        }
        // Get coords to send
        try {
            val py = Python.getInstance()
            val motorCoords = py.getModule("processing")
                .callAttr("get_coords", allPoints.toTypedArray(), height, width).toString()

            if (motorCoords != "DNM") {
                if (::connectedThreadWrite.isInitialized) {
                    connectedThreadWrite.write(motorCoords)
                    connectedThreadWrite.flush()
                    Log.d("COORDS_TAG_NEW", "Coords Sent: " + motorCoords)
                }
                trackUserCount -= 1
                loadingBar.progress = trackUserCount
                getPosText.text = "Get Positioned in Frame to Begin\n"
            } else if (trackUserCount >= TRACK_COUNT_MAX && changeState) {
                // if the camera hasn't moved for 3 iterations, stop moving camera
                state = 2
                startTime = (System.currentTimeMillis() / 1000)
                trackUserCount = 0
            } else if (changeState) {
                // increase trackUserCount if not moved and less than 10
                loadingBar.max = TRACK_COUNT_MAX
                loadingBar.progress = trackUserCount
                trackUserCount += 1
                if (trackUserCount >= TRACK_COUNT_MAX/3) {
                    getPosText.text = "Hold Still\n"
                }
            }
        } catch (e: Exception) {
            Log.e("PythonErrorTrackUser", "An error occurred:", e)
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
            Log.d("HAMSTRING", "kp: " + kp)
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
                Log.d("SYMMETRIC_DEBUG", "Leading Side: " + side)

                trackingDetail.keypoints.map { "${side}_$it" }.also {
                    if (trackingDetail == exerciseDetail.repTracking) {
                        exerciseDetail.repKeypoints = it
                    }
                }

            } else trackingDetail.keypoints

            Log.d("SYMMETRIC_DEBUG", keypoints.toString())

            val extractedLandmarks = extractKeypointsDynamic(
                landmarks, keypoints, trackingDetail.dimensionality
            )

            try {
                // initialize python (Chaquopy)
                val py = Python.getInstance()

                val exerciseAngle = when (trackingDetail.trackingType) {
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
}