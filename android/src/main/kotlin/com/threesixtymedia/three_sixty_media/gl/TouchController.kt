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

    private var viewWidth = 1
    private var viewHeight = 1
    private var gestureEnabled = true

    /** Set the current view dimensions. Called from ThreeSixtyMediaView. */
    fun updateViewSize(width: Int, height: Int) {
        viewWidth = if (width > 0) width else 1
        viewHeight = if (height > 0) height else 1
    }

    /** Update FOV limits. Called when Flutter invokes setFovLimits(). */
    fun updateFovLimits(min: Double, max: Double) {
        minFov = min
        maxFov = max
        fov = fov.coerceIn(minFov, maxFov)
    }

    /** Set the initial FOV. */
    fun setFov(initialFov: Double) {
        fov = initialFov
    }

    /** Enable or disable gesture recognition. */
    fun setGestureEnabled(enabled: Boolean) {
        gestureEnabled = enabled
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Calculate vertical FOV (in radians) from the current FOV value
            val vFovRad = Math.toRadians(fov)
            // Determine aspect ratio
            val aspect = viewWidth.toDouble() / viewHeight.toDouble()
            // Calculate horizontal FOV (in radians) based on aspect
            val hFovRad = 2.0 * Math.atan(Math.tan(vFovRad / 2.0) * aspect)

            // Convert pixel delta to angle delta; negative signs for "canvas drags with finger"
            val yawDelta = (distanceX / viewWidth) * hFovRad
            val pitchDelta = (distanceY / viewHeight) * vFovRad

            yaw -= yawDelta
            pitch -= pitchDelta
            pitch = pitch.coerceIn(-Math.PI / 2.0, Math.PI / 2.0)
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
        if (gestureEnabled) {
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
        }
        return true
    }
}
