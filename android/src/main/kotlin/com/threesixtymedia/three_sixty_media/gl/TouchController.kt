package com.threesixtymedia.three_sixty_media.gl

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.max
import kotlin.math.min

/**
 * Handles touch and pinch gestures, updating yaw, pitch and FOV.
 */
class TouchController(
    context: Context,
    private val onRotationChanged: (yaw: Double, pitch: Double) -> Unit,
    private val onZoomChanged: (fov: Double) -> Unit
) {
    // Current camera state
    private var yaw = 0.0
    private var pitch = 0.0
    private var fov = 75.0

    // Dynamically configurable FOV limits
    private var minFov = 30.0
    private var maxFov = 100.0

    /** Update FOV limits. Called when Flutter invokes setFovLimits(). */
    fun updateFovLimits(min: Double, max: Double) {
        minFov = min
        maxFov = max
        fov = fov.coerceIn(minFov, maxFov)
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Adjust sensitivity
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
                // Adjust FOV based on pinch; clamp to current min/max range
                fov /= detector.scaleFactor.toDouble()
                fov = fov.coerceIn(minFov, maxFov)
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
