package com.example.game.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) {
    fun set(nx: Float, ny: Float, nz: Float): Vector3 {
        x = nx
        y = ny
        z = nz
        return this
    }

    fun add(v: Vector3): Vector3 = Vector3(x + v.x, y + v.y, z + v.z)
    fun add(dx: Float, dy: Float, dz: Float): Vector3 = Vector3(x + dx, y + dy, z + dz)
    fun sub(v: Vector3): Vector3 = Vector3(x - v.x, y - v.y, z - v.z)
    fun mul(s: Float): Vector3 = Vector3(x * s, y * s, z * s)

    fun length(): Float = sqrt(x * x + y * y + z * z)
    fun lengthSq(): Float = x * x + y * y + z * z

    fun distanceTo(other: Vector3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun distanceTo(ox: Float, oy: Float, oz: Float): Float {
        val dx = x - ox
        val dy = y - oy
        val dz = z - oz
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun distanceTo2D(ox: Float, oz: Float): Float {
        val dx = x - ox
        val dz = z - oz
        return sqrt(dx * dx + dz * dz)
    }

    fun normalize(): Vector3 {
        val len = length()
        return if (len > 0.0001f) Vector3(x / len, y / len, z / len) else Vector3(0f, 0f, 0f)
    }

    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z

    companion object {
        fun fromYaw(yawDeg: Float): Vector3 {
            val rad = Math.toRadians(yawDeg.toDouble())
            return Vector3(-sin(rad).toFloat(), 0f, cos(rad).toFloat())
        }
    }
}

data class AABB(
    val minX: Float,
    val minY: Float,
    val minZ: Float,
    val maxX: Float,
    val maxY: Float,
    val maxZ: Float
) {
    fun contains(x: Float, y: Float, z: Float): Boolean {
        return x in minX..maxX && y in minY..maxY && z in minZ..maxZ
    }

    fun intersects(other: AABB): Boolean {
        return (minX <= other.maxX && maxX >= other.minX) &&
                (minY <= other.maxY && maxY >= other.minY) &&
                (minZ <= other.maxZ && maxZ >= other.minZ)
    }

    fun collidesWithSphere(center: Vector3, radius: Float): Boolean {
        val closestX = center.x.coerceIn(minX, maxX)
        val closestY = center.y.coerceIn(minY, maxY)
        val closestZ = center.z.coerceIn(minZ, maxZ)
        val distSq = (center.x - closestX) * (center.x - closestX) +
                (center.y - closestY) * (center.y - closestY) +
                (center.z - closestZ) * (center.z - closestZ)
        return distSq <= radius * radius
    }
}
