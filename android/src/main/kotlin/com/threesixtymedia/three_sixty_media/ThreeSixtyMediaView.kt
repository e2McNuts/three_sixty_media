package com.threesixtymedia.three_sixty_media

import android.os.Handler
import android.os.Looper
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
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

        // Initialize the touch controller:
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
            touchController.onTouchEvent(event)
            true
        }

        channel.setMethodCallHandler(this)

        val src = creationParams?.get("source") as? String
        if (src != null && src != "memory") {
            glView.queueEvent { renderer.loadImage(src) }
        }
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
                glView.queueEvent { renderer.setYawPitchRadians(Math.toRadians(yaw), Math.toRadians(pitch)) }
                result.success(null)
            }
            else -> result.success(null)
        }
    }
}
