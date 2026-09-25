package com.example.game.entities

import com.example.game.engine.Vector3

data class BulletTracer(
    var startPos: Vector3,
    var endPos: Vector3,
    var currentPos: Vector3,
    val direction: Vector3,
    val speed: Float,
    val damage: Float,
    val shooterId: String,
    val isHeadshot: Boolean,
    var lifeTime: Float = 0.6f,
    var isActive: Boolean = true
) {
    fun update(deltaTimeSec: Float) {
        lifeTime -= deltaTimeSec
        if (lifeTime <= 0f) {
            isActive = false
            return
        }
        val move = direction.mul(speed * deltaTimeSec)
        currentPos = currentPos.add(move)
    }
}

data class DamageIndicator(
    val damage: Int,
    val isHeadshot: Boolean,
    var worldPos: Vector3,
    var screenX: Float = 0f,
    var screenY: Float = 0f,
    var lifeTime: Float = 0.9f,
    var alpha: Float = 1.0f
)
