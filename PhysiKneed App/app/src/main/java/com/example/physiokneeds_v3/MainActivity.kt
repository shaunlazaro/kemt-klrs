package com.example.physiokneeds_v3

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chaquo.python.Python
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker

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
    lateinit var start_button: Button
    lateinit var camera_button: Button

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

    // set which camera the app opens first (FOR DEBUGGING)
    var current_camera = 0

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

        // get camera permissions from the user
        get_permissions()

        // define global variables
        textureView = findViewById(R.id.camera_feed)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        overlayView = findViewById(R.id.overlayView)
        poseLandmarker = PoseLandmarker.createFromOptions(this, options)

        textView_knee = findViewById(R.id.kneeAngle)
        textView_reps = findViewById(R.id.repsCount)

        start_button = findViewById(R.id.start_button)

        // set up switch camera button (FOR DEBUGGING)
        camera_button = findViewById(R.id.camera_button)
        camera_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                close_camera()
                if (current_camera == 0) {
                    open_camera(1)
                    current_camera = 1
                } else {
                    open_camera(0)
                    current_camera = 0
                }
            }
        })

        // adjust camera preview
        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                open_camera(0)
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

                // process and show knee angle and count reps using processing.py
                if (result != null && result.landmarks().size > 0) {
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

                            textView_knee.text = "Right Knee Angle: " + kneeAngle.toString()
                            textView_reps.text = "Reps: " + reps.toString()

                        } catch (e: Exception) {
                            // Log the stack trace to Logcat
                            Log.e("PythonError", "An error occurred:", e)
                        }
                    }
                }
            }

        }
    }

    fun get_permissions() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(kotlin.arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

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

}