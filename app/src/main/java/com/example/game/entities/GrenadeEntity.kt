package com.example.game.entities

import com.example.game.engine.Vector3

enum class GrenadeType {
    FRAG,
    SMOKE
}

class GrenadeEntity(
    val id: String,
    val type: GrenadeType,
    val throwerId: String,
    var position: Vector3,
    var velocity: Vector3,
    var fuseTimeSec: Float = 3.2f
) {
    var isDetonated: Boolean = false
    var smokeDurationSec: Float = 15.0f

    fun update(deltaTimeSec: Float, onExplode: (GrenadeEntity) -> Unit) {
        if (!isDetonated) {
            // Apply gravity
            velocity.y -= 9.8f * deltaTimeSec
            position = position.add(velocity.mul(deltaTimeSec))

            // Ground bounce
            if (position.y <= 0.2f) {
                position.y = 0.2f
                velocity.y = -velocity.y * 0.45f
                velocity.x *= 0.7f
                velocity.z *= 0.7f
            }

            fuseTimeSec -= deltaTimeSec
            if (fuseTimeSec <= 0f) {
                isDetonated = true
                onExplode(this)
            }
        } else if (type == GrenadeType.SMOKE) {
            smokeDurationSec -= deltaTimeSec
        }
    }
}
