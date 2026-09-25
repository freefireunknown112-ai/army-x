package com.example.game.engine

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Geometry3D {

    fun createFloatBuffer(data: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(data.size * 4)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(data)
        fb.position(0)
        return fb
    }

    fun createShortBuffer(data: ShortArray): ShortBuffer {
        val bb = ByteBuffer.allocateDirect(data.size * 2)
        bb.order(ByteOrder.nativeOrder())
        val sb = bb.asShortBuffer()
        sb.put(data)
        sb.position(0)
        return sb
    }

    class Mesh(
        val vertexBuffer: FloatBuffer,
        val normalBuffer: FloatBuffer,
        val colorBuffer: FloatBuffer,
        val indexBuffer: ShortBuffer,
        val indexCount: Int
    )

    // Unit Cube centered at origin (-0.5 to 0.5)
    fun buildCubeMesh(r: Float, g: Float, b: Float, a: Float = 1.0f): Mesh {
        val vertices = floatArrayOf(
            // Front face
            -0.5f, -0.5f,  0.5f,   0.5f, -0.5f,  0.5f,   0.5f,  0.5f,  0.5f,  -0.5f,  0.5f,  0.5f,
            // Back face
            -0.5f, -0.5f, -0.5f,  -0.5f,  0.5f, -0.5f,   0.5f,  0.5f, -0.5f,   0.5f, -0.5f, -0.5f,
            // Top face
            -0.5f,  0.5f, -0.5f,  -0.5f,  0.5f,  0.5f,   0.5f,  0.5f,  0.5f,   0.5f,  0.5f, -0.5f,
            // Bottom face
            -0.5f, -0.5f, -0.5f,   0.5f, -0.5f, -0.5f,   0.5f, -0.5f,  0.5f,  -0.5f, -0.5f,  0.5f,
            // Right face
             0.5f, -0.5f, -0.5f,   0.5f,  0.5f, -0.5f,   0.5f,  0.5f,  0.5f,   0.5f, -0.5f,  0.5f,
            // Left face
            -0.5f, -0.5f, -0.5f,  -0.5f, -0.5f,  0.5f,  -0.5f,  0.5f,  0.5f,  -0.5f,  0.5f, -0.5f
        )

        val normals = floatArrayOf(
            0f, 0f, 1f,   0f, 0f, 1f,   0f, 0f, 1f,   0f, 0f, 1f,
            0f, 0f, -1f,  0f, 0f, -1f,  0f, 0f, -1f,  0f, 0f, -1f,
            0f, 1f, 0f,   0f, 1f, 0f,   0f, 1f, 0f,   0f, 1f, 0f,
            0f, -1f, 0f,  0f, -1f, 0f,  0f, -1f, 0f,  0f, -1f, 0f,
            1f, 0f, 0f,   1f, 0f, 0f,   1f, 0f, 0f,   1f, 0f, 0f,
           -1f, 0f, 0f,  -1f, 0f, 0f,  -1f, 0f, 0f,  -1f, 0f, 0f
        )

        val colors = FloatArray(24 * 4)
        for (i in 0 until 24) {
            colors[i * 4 + 0] = r
            colors[i * 4 + 1] = g
            colors[i * 4 + 2] = b
            colors[i * 4 + 3] = a
        }

        val indices = shortArrayOf(
            0,  1,  2,      0,  2,  3,    // front
            4,  5,  6,      4,  6,  7,    // back
            8,  9,  10,     8,  10, 11,   // top
            12, 13, 14,     12, 14, 15,   // bottom
            16, 17, 18,     16, 18, 19,   // right
            20, 21, 22,     20, 22, 23    // left
        )

        return Mesh(
            createFloatBuffer(vertices),
            createFloatBuffer(normals),
            createFloatBuffer(colors),
            createShortBuffer(indices),
            indices.size
        )
    }

    // Cylindrical safe-zone energy shield or silo
    fun buildCylinderMesh(segments: Int, radius: Float, height: Float, r: Float, g: Float, b: Float, a: Float = 0.5f): Mesh {
        val vertList = mutableListOf<Float>()
        val normList = mutableListOf<Float>()
        val colorList = mutableListOf<Float>()
        val indList = mutableListOf<Short>()

        val halfH = height / 2f

        for (i in 0..segments) {
            val theta = (i.toFloat() / segments) * 2f * PI.toFloat()
            val cosT = cos(theta)
            val sinT = sin(theta)

            val x = radius * cosT
            val z = radius * sinT

            // Bottom vertex
            vertList.add(x); vertList.add(-halfH); vertList.add(z)
            normList.add(cosT); normList.add(0f); normList.add(sinT)
            colorList.add(r); colorList.add(g); colorList.add(b); colorList.add(a)

            // Top vertex
            vertList.add(x); vertList.add(halfH); vertList.add(z)
            normList.add(cosT); normList.add(0f); normList.add(sinT)
            colorList.add(r); colorList.add(g); colorList.add(b); colorList.add(a * 0.7f)
        }

        for (i in 0 until segments) {
            val b1 = (i * 2).toShort()
            val t1 = (i * 2 + 1).toShort()
            val b2 = ((i + 1) * 2).toShort()
            val t2 = ((i + 1) * 2 + 1).toShort()

            indList.add(b1); indList.add(t1); indList.add(b2)
            indList.add(b2); indList.add(t1); indList.add(t2)
        }

        return Mesh(
            createFloatBuffer(vertList.toFloatArray()),
            createFloatBuffer(normList.toFloatArray()),
            createFloatBuffer(colorList.toFloatArray()),
            createShortBuffer(indList.toShortArray()),
            indList.size
        )
    }

    // Terrain Mesh with gentle rolling hills, riverbed and roads
    fun buildTerrainMesh(gridSize: Int, totalSize: Float): Mesh {
        val vertList = mutableListOf<Float>()
        val normList = mutableListOf<Float>()
        val colorList = mutableListOf<Float>()
        val indList = mutableListOf<Short>()

        val halfSize = totalSize / 2f
        val step = totalSize / gridSize

        for (iz in 0..gridSize) {
            val z = -halfSize + iz * step
            for (ix in 0..gridSize) {
                val x = -halfSize + ix * step

                // Procedural terrain elevation: gentle hills, military valleys, river depression
                var y = sin(x * 0.04f) * cos(z * 0.04f) * 2.5f +
                        sin(x * 0.015f + 1.2f) * 3.5f

                // Road flattening through center
                if (kotlin.math.abs(x) < 5f || kotlin.math.abs(z - 10f) < 4f) {
                    y = 0.1f
                }

                // River depression at east border
                if (x > 35f && x < 55f) {
                    y = -1.2f
                }

                vertList.add(x); vertList.add(y); vertList.add(z)
                normList.add(0f); normList.add(1f); normList.add(0f)

                // Terrain coloring: Grass / Dirt / Road
                if (kotlin.math.abs(x) < 5f || kotlin.math.abs(z - 10f) < 4f) {
                    // Asphalt road
                    colorList.add(0.22f); colorList.add(0.24f); colorList.add(0.26f); colorList.add(1f)
                } else if (y < -0.2f) {
                    // River water / mud
                    colorList.add(0.15f); colorList.add(0.35f); colorList.add(0.45f); colorList.add(1f)
                } else if (y > 3.0f) {
                    // Rocky hilltop
                    colorList.add(0.42f); colorList.add(0.40f); colorList.add(0.38f); colorList.add(1f)
                } else {
                    // Tactical military terrain / dry grass
                    colorList.add(0.28f); colorList.add(0.36f); colorList.add(0.22f); colorList.add(1f)
                }
            }
        }

        val rowStride = gridSize + 1
        for (iz in 0 until gridSize) {
            for (ix in 0 until gridSize) {
                val topLeft = (iz * rowStride + ix).toShort()
                val topRight = (iz * rowStride + ix + 1).toShort()
                val bottomLeft = ((iz + 1) * rowStride + ix).toShort()
                val bottomRight = ((iz + 1) * rowStride + ix + 1).toShort()

                indList.add(topLeft); indList.add(bottomLeft); indList.add(topRight)
                indList.add(topRight); indList.add(bottomLeft); indList.add(bottomRight)
            }
        }

        return Mesh(
            createFloatBuffer(vertList.toFloatArray()),
            createFloatBuffer(normList.toFloatArray()),
            createFloatBuffer(colorList.toFloatArray()),
            createShortBuffer(indList.toShortArray()),
            indList.size
        )
    }
}
