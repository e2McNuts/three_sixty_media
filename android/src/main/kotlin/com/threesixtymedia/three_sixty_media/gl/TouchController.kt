package com.threesixtymedia.three_sixty_media.gl

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.max
import kotlin.math.min

/**
 * Kümmert sich um Touch- und Pinch-Gesten und ändert Yaw/Pitch/FOV.
 */
class TouchController(
    context: Context,
    private val onRotationChanged: (yaw: Double, pitch: Double) -> Unit,
    private val onZoomChanged: (fov: Double) -> Unit
) {
    private var yaw = 0.0
    private var pitch = 0.0
    private var fov = 75.0

    private var lastYaw = 0.0
    private var lastPitch = 0.0

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Empfindlichkeit anpassen:
            yaw += distanceX * 0.005
            pitch += distanceY * 0.005
            pitch = min(max(pitch, -Math.PI / 2.0), Math.PI / 2.0)
            onRotationChanged(yaw, pitch)
            return true
        }
    })

    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                fov /= detector.scaleFactor.toDouble()
                onZoomChanged(fov)
                return true
            }
        })


    fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }
}
