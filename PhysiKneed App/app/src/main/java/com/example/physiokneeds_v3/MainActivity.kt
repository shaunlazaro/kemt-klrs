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
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
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
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.UUID


class MainActivity : AppCompatActivity() {

    // global variables
    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var handler:Handler
    lateinit var handlerThread: HandlerThread
    lateinit var overlayView:OverlayView
    lateinit var bitmap: Bitmap

    lateinit var textView_knee: TextView
    lateinit var textView_reps: TextView

    lateinit var reps_text: TextView
//    lateinit var start_button: Button
//    lateinit var camera_button: Button

    lateinit var cameraDevice: CameraDevice
    lateinit var cameraCaptureSession: CameraCaptureSession

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

    lateinit var connectedThreadWrite: ConnectedThread

    var isConnected = false

    var startTracking = false

    val COUNT_MAX = 10
    var counts = 0

    var currentPos = "90,45"

    var isReceiverRegistered = false

    // set which camera the app opens first (FOR DEBUGGING)
    var current_camera = 0

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

        // connect to bluetooth
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter

        search_button = findViewById(R.id.search_button)
        bt_button = findViewById(R.id.bt_button)
        bt_button.isEnabled = false
        connection_text = findViewById(R.id.status_text)
        send_coords = findViewById(R.id.send_coords)

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
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
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
//        textureView.rotation = 90f // Rotate 90 degrees clockwise
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        overlayView = findViewById(R.id.overlayView)
//        overlayView.rotation = 90f // Rotate 90 degrees clockwise
        poseLandmarker = PoseLandmarker.createFromOptions(this, options)

        textView_knee = findViewById(R.id.kneeAngle)
        textView_reps = findViewById(R.id.repsCount)

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
                open_camera(1)
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

                // process each image from the camera feed
                bitmap = textureView.bitmap!!

                val mpImage = BitmapImageBuilder(bitmap).build()
                val result = poseLandmarker?.detect(mpImage)

                // display the keypoints and lines
                result?.let { overlayView.setResults(it, mpImage.height, mpImage.width) }

                // check if any keypoints are detected before processing
                if (result != null && result.landmarks().size > 0 && result.landmarks().get(0).get(0).visibility().orElse(0.0f) > 0.6f) {
                    // process and show results for the specified exercise
                    seated_knee_extension(result)
                    if (counts > COUNT_MAX) {
                        track_user(result, mpImage.height, mpImage.width)
                        counts = 0
                    }
                }

//                textView_reps.text = counts.toString()
                counts += 1
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
            // initialize python (Chaquopy)
            val py = Python.getInstance()
            val motorCoords = py.getModule("processing")
                .callAttr("get_coords", allPoints.toTypedArray(), height, width, currentPos)

            if (motorCoords.toString() != "DNM") {
//                textView_knee.text = motorCoords.toString()
                currentPos = motorCoords.toString()
                Log.d(TAG, currentPos)
                connectedThreadWrite.write(motorCoords.toString())
            }

        } catch (e: Exception) {
            // Log the stack trace to Logcat
            Log.e("PythonError", "An error occurred:", e)
        }
    }

    fun seated_knee_extension(result: PoseLandmarkerResult) {
        val RIGHT_HIP_INDEX = 24
        val RIGHT_KNEE_INDEX = 26
        val RIGHT_ANKLE_INDEX = 28
        val CONFIDENCE_THRESHOLD = 0.6f  // Adjust based on desired accuracy

        // if all points are visible with above the threshold
        if (result.landmarks().get(0).get(RIGHT_HIP_INDEX).visibility()
                .orElse(0.0f) > CONFIDENCE_THRESHOLD &&
            result.landmarks().get(0).get(RIGHT_KNEE_INDEX).visibility()
                .orElse(0.0f) > CONFIDENCE_THRESHOLD &&
            result.landmarks().get(0).get(RIGHT_ANKLE_INDEX).visibility()
                .orElse(0.0f) > CONFIDENCE_THRESHOLD) {

            // get 3d keypoints of the three landmarks
            val rightHip = listOf(result.landmarks().get(0).get(RIGHT_HIP_INDEX).x(),
                result.landmarks().get(0).get(RIGHT_HIP_INDEX).y(),
                result.landmarks().get(0).get(RIGHT_HIP_INDEX).z()).toTypedArray()

            val rightKnee = listOf(result.landmarks().get(0).get(RIGHT_KNEE_INDEX).x(),
                result.landmarks().get(0).get(RIGHT_KNEE_INDEX).y(),
                result.landmarks().get(0).get(RIGHT_KNEE_INDEX).z()).toTypedArray()

            val rightAnkle = listOf(result.landmarks().get(0).get(RIGHT_ANKLE_INDEX).x(),
                result.landmarks().get(0).get(RIGHT_ANKLE_INDEX).y(),
                result.landmarks().get(0).get(RIGHT_ANKLE_INDEX).z()).toTypedArray()

            // calculate the angle and count reps
            try {
                // initialize python (Chaquopy)
                val py = Python.getInstance()
                val kneeAngle = py.getModule("processing")
                    .callAttr("calculate_angle", rightHip, rightKnee, rightAnkle)

                val reps = py.getModule("processing")
                    .callAttr("detect_reps", kneeAngle)

//                textView_knee.text = "Right Knee Angle: " + kneeAngle.toString()
                reps_text.text = "Rep " + reps.toString() + "/ 10"

            } catch (e: Exception) {
                // Log the stack trace to Logcat
                Log.e("PythonError", "An error occurred:", e)
            }
        }
    }

}