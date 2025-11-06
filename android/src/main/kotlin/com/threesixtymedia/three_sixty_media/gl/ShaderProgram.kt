package com.threesixtymedia.three_sixty_media.gl

import android.opengl.GLES20
import android.util.Log

/**
 * Hilfsklasse zum Kompilieren von Vertex/Fragment-Shadern.
 * V0.1.0: nur Stub – echter Shadercode folgt in Schritt 2.
 */
object ShaderProgram {
    fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e("ShaderProgram", "Shader compile error: ${GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Shader compile error")
        }
        return shader
    }
}
