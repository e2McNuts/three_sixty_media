package com.threesixtymedia.three_sixty_media.gl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.io.InputStream
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.*

/**
 * Renderer360 – OpenGL-Renderer für equirectangular 360°-Bilder.
 *
 * Unterstützt:
 *  • Yaw / Pitch / FOV (Kamerasteuerung)
 *  • Textur-Laden aus Pfad oder Bytes
 *  • SphereMesh-Rendering (innenliegend)
 */
class Renderer360(private val context: Context) : GLSurfaceView.Renderer {

    // Kamera-Parameter
    private var yaw = 0.0
    private var pitch = 0.0
    private var fov = 75.0
    private var minFov = 30.0
    private var maxFov = 100.0

    var onFovChanged: ((Double) -> Unit)? = null


    // OpenGL-Objekte
    private var program = 0
    private var textureId = 0
    private lateinit var sphere: SphereMesh

    // Matrizen
    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val mvp = FloatArray(16)

    // Shader-Handles
    private var uMVP = 0
    private var uTexture = 0
    private var aPos = 0
    private var aTex = 0

    // Breite/Höhe für Aspect Ratio
    private var surfaceWidth = 1
    private var surfaceHeight = 1

    // Bildpfad (nur zur Info)
    private var imagePath: String? = null

    // ---------------------------------------------------------------------
    //  OpenGL-Lifecycle
    // ---------------------------------------------------------------------

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        val vertexShader = """
            attribute vec3 aPos;
            attribute vec2 aTex;
            uniform mat4 uMVP;
            varying vec2 vTex;
            void main() {
              vTex = aTex;
              gl_Position = uMVP * vec4(aPos, 1.0);
            }
        """.trimIndent()

        val fragmentShader = """
            precision mediump float;
            varying vec2 vTex;
            uniform sampler2D uTexture;
            void main() {
              gl_FragColor = texture2D(uTexture, vTex);
            }
        """.trimIndent()

        val vShader = ShaderProgram.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fShader = ShaderProgram.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vShader)
            GLES20.glAttachShader(it, fShader)
            GLES20.glLinkProgram(it)
        }

        aPos = GLES20.glGetAttribLocation(program, "aPos")
        aTex = GLES20.glGetAttribLocation(program, "aTex")
        uMVP = GLES20.glGetUniformLocation(program, "uMVP")
        uTexture = GLES20.glGetUniformLocation(program, "uTexture")

        sphere = SphereMesh(48, 96, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        GLES20.glViewport(0, 0, width, height)
        updateProjection()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)

        // --- Kamera-View berechnen ---
        val yawRad = yaw.toFloat()
        val pitchRad = pitch.toFloat()

        val eyeX = 0f
        val eyeY = 0f
        val eyeZ = 0f

        val dirX = (cos(pitchRad) * sin(yawRad)).toFloat()
        val dirY = sin(pitchRad).toFloat()
        val dirZ = (cos(pitchRad) * cos(yawRad)).toFloat()

        val centerX = eyeX + dirX
        val centerY = eyeY + dirY
        val centerZ = eyeZ + dirZ

        Matrix.setLookAtM(view, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, 0f, 1f, 0f)
        Matrix.multiplyMM(mvp, 0, projection, 0, view, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTexture, 0)
        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0)

        sphere.draw(aPos, aTex)
    }

    // ---------------------------------------------------------------------
    //  Texturen
    // ---------------------------------------------------------------------

    /** Lädt eine Bilddatei (lokal oder Asset). */
    fun loadImage(path: String) {
        imagePath = path
        val bitmap = loadBitmap(path)
        if (bitmap == null) {
            Log.e("Renderer360", "Failed to load bitmap from $path")
            return
        }
        uploadTexture(bitmap)
    }

    /** Lädt ein Bild direkt aus Byte-Daten (vom Flutter-Layer). */
    fun loadImageBytes(data: ByteArray) {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size) ?: return
        uploadTexture(bitmap)
    }

    private fun uploadTexture(bitmap: Bitmap) {
        if (textureId == 0) {
            val tex = IntArray(1)
            GLES20.glGenTextures(1, tex, 0)
            textureId = tex[0]
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
    }

    /** Liest eine Bitmap aus Asset oder Dateipfad. */
    private fun loadBitmap(path: String): Bitmap? {
        return try {
            val stream: InputStream? = when {
                path.startsWith("assets/") -> context.assets.open(path.removePrefix("assets/"))
                path.startsWith("/") -> java.io.FileInputStream(path)
                else -> null
            }
            stream?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ---------------------------------------------------------------------
    //  Kamera-Steuerung
    // ---------------------------------------------------------------------

    fun getYaw() = yaw
    fun getPitch() = pitch
    fun getFov() = fov

    fun setYawPitchRadians(yawRad: Double, pitchRad: Double) {
        yaw = yawRad
        pitch = pitchRad.coerceIn(-Math.PI / 2.0, Math.PI / 2.0)
    }

    fun setFovDegrees(value: Double) {
    fov = value.coerceIn(minFov, maxFov)
    updateProjection()
    onFovChanged?.invoke(fov)
    }

    fun setFovLimits(min: Double, max: Double) {
    minFov = min
    maxFov = max
    fov = fov.coerceIn(minFov, maxFov)
    updateProjection()
    onFovChanged?.invoke(fov)
    }

    fun resetView() {
    yaw = 0.0
    pitch = 0.0
    fov = ((minFov + maxFov) / 2.0)
    updateProjection()
    }

    private fun updateProjection() {
        val aspect = surfaceWidth.toFloat() / surfaceHeight.toFloat()
        Matrix.perspectiveM(projection, 0, fov.toFloat(), aspect, 0.1f, 100f)
    }

    // ---------------------------------------------------------------------
    //  Cleanup
    // ---------------------------------------------------------------------

    fun release() {
        if (textureId != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = 0
        }
    }
}
