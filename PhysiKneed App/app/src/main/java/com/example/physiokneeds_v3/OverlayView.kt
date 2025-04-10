package com.example.physiokneeds_v3

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.components.containers.Connection
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.lang.Double.sum
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.white)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH*0.6f
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.WHITE
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH*1.6f
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        var sum_x = 0f
        var sum_y = 0f
        results?.let { poseLandmarkerResult ->
            for(landmark in poseLandmarkerResult.landmarks()) {
                for(normalizedLandmark in landmark) {
//                    canvas.drawPoint(
//                        normalizedLandmark.x() * imageWidth * scaleFactor,
//                        normalizedLandmark.y() * imageHeight * scaleFactor,
//                        pointPaint
//                    )

                    sum_x += normalizedLandmark.x()
                    sum_y += normalizedLandmark.y()

                }
                val midpoint_x = sum_x / landmark.size
                val midpoint_y = sum_y / landmark.size

                Log.d("MIDPOINT", midpoint_x.toString())
                Log.d("MIDPOINT", midpoint_y.toString())

//                canvas.drawPoint(
//                        midpoint_x * imageWidth * scaleFactor,
//                        midpoint_y * imageHeight * scaleFactor,
//                        pointPaint
//                    )

                val connections = setOf(Connection.create(23,25), Connection.create(24,26), Connection.create(25,27), Connection.create(26,28))

//                PoseLandmarker.POSE_LANDMARKS.forEach {
                connections.forEach {
                    // only draw the lines if the visibility is over 60%
                    if (poseLandmarkerResult.landmarks().get(0).get(it!!.start()).visibility().orElse(0.0f) > 0.6f
                        && poseLandmarkerResult.landmarks().get(0).get(it!!.end()).visibility().orElse(0.0f) > 0.6f) {
                        canvas.drawLine(
                            poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
                            poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
                            poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
                            poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
                            linePaint)
                        canvas.drawCircle(
                            poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
                            poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
                            LANDMARK_STROKE_WIDTH,
                            pointPaint
                        )
                        canvas.drawCircle(
                            poseLandmarkerResult.landmarks().get(0).get(it!!.end()).x() * imageWidth * scaleFactor,
                            poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
                            LANDMARK_STROKE_WIDTH,
                            pointPaint
                        )
                    }
                }
            }
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}