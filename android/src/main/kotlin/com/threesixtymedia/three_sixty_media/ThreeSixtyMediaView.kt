package com.threesixtymedia.three_sixty_media

import android.os.Handler
import android.os.Looper
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.View
import com.threesixtymedia.three_sixty_media.gl.Renderer360
import com.threesixtymedia.three_sixty_media.gl.TouchController
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

/**
 * PlatformView that hosts a GLSurfaceView to render 360° content.
 *
 * Coordinates between Flutter (via MethodChannel) and the native Renderer360.
 */
class ThreeSixtyMediaView(
    context: Context,
    messenger: BinaryMessenger,
    viewId: Int,
    creationParams: Map<String, Any?>?
) : PlatformView, MethodChannel.MethodCallHandler {

    private val glView = GLSurfaceView(context)
    private val renderer = Renderer360(context)
    private val touchController: TouchController
    private val mainHandler = Handler(Looper.getMainLooper())
    private val channel = MethodChannel(messenger, "com.threesixtymedia/core/$viewId")

    init {
        glView.setEGLContextClientVersion(2)
        glView.setRenderer(renderer)
        glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        // Extract initial parameters from creationParams
        val initialYaw = creationParams?.get("initialYaw") as? Double ?: 0.0
        val initialPitch = creationParams?.get("initialPitch") as? Double ?: 0.0
        val initialFov = creationParams?.get("initialFov") as? Double ?: 75.0
        val gestureEnabled = creationParams?.get("gestureEnabled") as? Boolean ?: true

        // Apply initial camera settings to the renderer
        renderer.setYawPitchRadians(initialYaw, initialPitch)
        renderer.setFovDegrees(initialFov)

        // Initialize the touch controller
        touchController = TouchController(
            context,
            onRotationChanged = { yaw, pitch ->
                glView.queueEvent {
                    renderer.setYawPitchRadians(yaw, pitch)
                }
            },
            onZoomChanged = { newFov ->
                glView.queueEvent {
                    renderer.setFovDegrees(newFov)
                }
            }
        )
        touchController.setGestureEnabled(gestureEnabled)
        touchController.setFov(initialFov) // Set initial FOV for touch controller

        glView.post {
            touchController.updateViewSize(glView.width, glView.height)
        }

        renderer.onFovChanged = { fov ->
            // Ensure this runs on the main/UI thread
            mainHandler.post {
                channel.invokeMethod("onFovChanged", mapOf("fov" to fov))
            }
        }

        renderer.onError = { message: String ->
            mainHandler.post {
                channel.invokeMethod("onError", mapOf("message" to message))
            }
        }

        glView.setOnTouchListener { _, event ->
            if (gestureEnabled) {
                touchController.onTouchEvent(event)
            }
            true
        }

        channel.setMethodCallHandler(this)
    }

    override fun getView(): View = glView

    override fun dispose() {
        channel.setMethodCallHandler(null)
        glView.queueEvent { renderer.release() }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "loadImageBytes" -> {
                val data = call.argument<ByteArray>("bytes")
                if (data != null) glView.queueEvent { renderer.loadImageBytes(data) }
                result.success(null)
            }
            "setFovLimits" -> {
                val min = call.argument<Double>("min") ?: 30.0
                val max = call.argument<Double>("max") ?: 100.0
                glView.queueEvent { renderer.setFovLimits(min, max) }
                mainHandler.post { touchController.updateFovLimits(min, max) }
                result.success(null)
            }
            "resetView" -> {
                glView.queueEvent { renderer.resetView() }
                result.success(null)
            }
            "setFov" -> {
                val fov = call.argument<Double>("fov") ?: 75.0
                glView.queueEvent { renderer.setFovDegrees(fov) }
                result.success(null)
            }
            "setYawPitch" -> {
                val yaw = call.argument<Double>("yaw") ?: 0.0
                val pitch = call.argument<Double>("pitch") ?: 0.0
                glView.queueEvent { renderer.setYawPitchRadians(yaw, pitch) } // Yaw and Pitch are already in radians from Flutter
                result.success(null)
            }
            "setGestureEnabled" -> {
                val enabled = call.argument<Boolean>("enabled") ?: true
                mainHandler.post { touchController.setGestureEnabled(enabled) }
                result.success(null)
            }
            else -> result.notImplemented() // Use notImplemented for unhandled methods
        }
    }
}
