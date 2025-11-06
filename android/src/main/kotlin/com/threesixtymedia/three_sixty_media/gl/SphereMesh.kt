package com.threesixtymedia.three_sixty_media.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.*

/**
 * Erstellt eine Kugelgeometrie mit UV-Koordinaten für equirectangular Mapping.
 * Die Normals zeigen nach innen, damit die Kamera "in der Kugel" sitzt.
 */
class SphereMesh(
    stacks: Int = 48, // horizontale Unterteilungen
    slices: Int = 96, // vertikale Unterteilungen
    radius: Float = 1.0f
) {
    private val vertexCount: Int
    private val vertexBuffer: FloatBuffer
    private val texBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val indexCount: Int

    init {
        val vertices = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        for (i in 0..stacks) {
            val v = i.toFloat() / stacks
            val phi = Math.PI * v
            for (j in 0..slices) {
                val u = j.toFloat() / slices
                val theta = 2.0 * Math.PI * u

                // Position
                val x = (sin(phi) * cos(theta) * radius).toFloat()
                val y = (cos(phi) * radius).toFloat()
                val z = (sin(phi) * sin(theta) * radius).toFloat()

                // Normals nach innen zeigen → Vorzeichen umdrehen
                vertices.add(-x)
                vertices.add(-y)
                vertices.add(-z)

                texCoords.add(u.toFloat())      // [0..1]
                texCoords.add((1.0 - v).toFloat()) // [0..1]
            }
        }

        for (i in 0 until stacks) {
            val k1 = i * (slices + 1)
            val k2 = k1 + slices + 1
            for (j in 0 until slices) {
                indices.add((k1 + j).toShort())
                indices.add((k2 + j).toShort())
                indices.add((k1 + j + 1).toShort())
                indices.add((k1 + j + 1).toShort())
                indices.add((k2 + j).toShort())
                indices.add((k2 + j + 1).toShort())
            }
        }

        vertexCount = vertices.size / 3
        indexCount = indices.size

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(vertices.toFloatArray())
                position(0)
            }

        texBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(texCoords.toFloatArray())
                position(0)
            }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer().apply {
                put(indices.toShortArray())
                position(0)
            }
    }

    fun draw(aPos: Int, aTex: Int) {
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(aTex)
        GLES20.glVertexAttribPointer(aTex, 2, GLES20.GL_FLOAT, false, 0, texBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aTex)
    }
}
