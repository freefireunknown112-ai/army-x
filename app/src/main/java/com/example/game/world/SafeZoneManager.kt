package com.example.game.world

import com.example.game.engine.Vector3
import kotlin.math.sqrt

data class ZonePhase(
    val phaseNumber: Int,
    val initialRadius: Float,
    val targetRadius: Float,
    val waitTimeSec: Float,
    val shrinkDurationSec: Float,
    val damagePerSec: Float
)

class SafeZoneManager {
    val phases = listOf(
        ZonePhase(1, 110f, 75f, 25f, 35f, 1.5f),
        ZonePhase(2, 75f, 48f, 20f, 30f, 3.0f),
        ZonePhase(3, 48f, 25f, 18f, 25f, 6.0f),
        ZonePhase(4, 25f, 10f, 12f, 20f, 10.0f),
        ZonePhase(5, 10f, 0f, 8f, 15f, 18.0f)
    )

    var currentPhaseIndex: Int = 0
    var isShrinking: Boolean = false
    var phaseTimer: Float = 0f

    val currentCenter = Vector3(0f, 0f, 0f)
    val nextCenter = Vector3(5f, 0f, -10f)

    var currentRadius: Float = 110f
    var targetRadius: Float = 75f

    val currentPhase: ZonePhase get() = phases[currentPhaseIndex.coerceIn(0, phases.size - 1)]

    fun update(deltaTimeSec: Float) {
        if (currentPhaseIndex >= phases.size) return
        val phase = currentPhase
        phaseTimer += deltaTimeSec

        if (!isShrinking) {
            // Waiting before shrink starts
            if (phaseTimer >= phase.waitTimeSec) {
                isShrinking = true
                phaseTimer = 0f
            }
        } else {
            // Active shrinking
            val progress = (phaseTimer / phase.shrinkDurationSec).coerceIn(0f, 1f)
            currentRadius = phase.initialRadius + (phase.targetRadius - phase.initialRadius) * progress

            // Smooth center shift towards next safe zone
            currentCenter.x = currentCenter.x + (nextCenter.x - currentCenter.x) * (progress * 0.05f)
            currentCenter.z = currentCenter.z + (nextCenter.z - currentCenter.z) * (progress * 0.05f)

            if (phaseTimer >= phase.shrinkDurationSec) {
                isShrinking = false
                phaseTimer = 0f
                currentRadius = phase.targetRadius
                if (currentPhaseIndex < phases.size - 1) {
                    currentPhaseIndex++
                    // Pick next random zone center within remaining radius
                    val angle = (Math.random() * 2 * Math.PI).toFloat()
                    val dist = (Math.random() * currentRadius * 0.4f).toFloat()
                    nextCenter.x = currentCenter.x + kotlin.math.cos(angle) * dist
                    nextCenter.z = currentCenter.z + kotlin.math.sin(angle) * dist
                }
            }
        }
    }

    fun isInsideSafeZone(pos: Vector3): Boolean {
        val dx = pos.x - currentCenter.x
        val dz = pos.z - currentCenter.z
        return sqrt(dx * dx + dz * dz) <= currentRadius
    }

    fun getRemainingTime(): Int {
        val phase = currentPhase
        val remaining = if (!isShrinking) {
            (phase.waitTimeSec - phaseTimer).coerceAtLeast(0f)
        } else {
            (phase.shrinkDurationSec - phaseTimer).coerceAtLeast(0f)
        }
        return remaining.toInt()
    }
}
