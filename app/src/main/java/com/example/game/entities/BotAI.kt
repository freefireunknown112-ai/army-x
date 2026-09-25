package com.example.game.entities

import com.example.data.models.Weapon
import com.example.game.engine.Vector3
import com.example.game.world.BattleMap
import kotlin.math.atan2

enum class BotState {
    PATROL,
    CHASE,
    ATTACK,
    TAKE_COVER,
    RETREAT,
    DEAD
}

enum class BotDifficulty(val detectionRange: Float, val accuracy: Float, val burstDelayMs: Long) {
    EASY(30f, 0.45f, 500L),
    NORMAL(45f, 0.65f, 350L),
    HARD(65f, 0.85f, 200L)
}

class BotAI(
    val id: String,
    val name: String,
    var position: Vector3,
    val difficulty: BotDifficulty = BotDifficulty.NORMAL,
    val weapon: Weapon = Weapon.createAssaultRifle()
) {
    var state: BotState = BotState.PATROL
    var health: Float = 100f
    var shield: Float = 50f
    var isAlive: Boolean = true

    var yaw: Float = (Math.random() * 360f).toFloat()
    var patrolTarget: Vector3 = Vector3(position.x, position.y, position.z)
    var stateTimer: Float = 0f
    var shootCooldownTimer: Float = 0f

    var kills: Int = 0

    fun update(
        deltaTimeSec: Float,
        playerPos: Vector3,
        isPlayerAlive: Boolean,
        map: BattleMap,
        onShoot: (BotAI, Vector3) -> Unit
    ) {
        if (!isAlive) return

        stateTimer += deltaTimeSec
        shootCooldownTimer -= deltaTimeSec

        val distToPlayer = position.distanceTo(playerPos)

        // State Machine Decision
        when (state) {
            BotState.PATROL -> {
                if (isPlayerAlive && distToPlayer <= difficulty.detectionRange) {
                    // Check if player is visible (not obstructed by full wall)
                    state = BotState.ATTACK
                    stateTimer = 0f
                } else {
                    // Move towards patrol target
                    moveTowards(patrolTarget, speed = 3.5f, deltaTimeSec, map)
                    if (position.distanceTo2D(patrolTarget.x, patrolTarget.z) < 2.0f || stateTimer > 6.0f) {
                        pickNewPatrolTarget(map)
                        stateTimer = 0f
                    }
                }
            }

            BotState.CHASE -> {
                if (!isPlayerAlive || distToPlayer > difficulty.detectionRange * 1.4f) {
                    state = BotState.PATROL
                } else if (distToPlayer <= 25f) {
                    state = BotState.ATTACK
                } else {
                    lookAt(playerPos)
                    moveTowards(playerPos, speed = 5.0f, deltaTimeSec, map)
                }
            }

            BotState.ATTACK -> {
                lookAt(playerPos)
                if (health < 30f && Math.random() < 0.3) {
                    state = BotState.RETREAT
                    stateTimer = 0f
                } else if (distToPlayer > difficulty.detectionRange * 1.2f || !isPlayerAlive) {
                    state = BotState.PATROL
                } else {
                    // Tactical strafing while firing
                    val strafeDir = if ((stateTimer.toInt() % 2) == 0) 1f else -1f
                    strafe(strafeDir, deltaTimeSec, map)

                    if (shootCooldownTimer <= 0f && weapon.currentAmmo > 0) {
                        shootCooldownTimer = difficulty.burstDelayMs / 1000f
                        weapon.currentAmmo--

                        // Inaccuracy offset based on bot difficulty
                        val spread = (1.0f - difficulty.accuracy) * 2.5f
                        val aimTarget = Vector3(
                            playerPos.x + (Math.random() * spread - spread / 2).toFloat(),
                            playerPos.y + 0.5f,
                            playerPos.z + (Math.random() * spread - spread / 2).toFloat()
                        )
                        onShoot(this, aimTarget)
                    } else if (weapon.currentAmmo <= 0) {
                        weapon.currentAmmo = weapon.magazineSize
                        shootCooldownTimer = 2.0f // Reload delay
                    }
                }
            }

            BotState.RETREAT -> {
                // Move away from player towards cover
                val awayDir = position.sub(playerPos).normalize()
                val retreatTarget = position.add(awayDir.mul(12f))
                lookAt(retreatTarget)
                moveTowards(retreatTarget, speed = 5.5f, deltaTimeSec, map)

                if (stateTimer > 4.0f) {
                    // Self-heal partially
                    health = (health + 35f).coerceAtMost(100f)
                    state = BotState.ATTACK
                    stateTimer = 0f
                }
            }

            BotState.TAKE_COVER -> {
                if (stateTimer > 3.0f) {
                    state = BotState.ATTACK
                    stateTimer = 0f
                }
            }

            BotState.DEAD -> {}
        }
    }

    private fun lookAt(target: Vector3) {
        val dx = target.x - position.x
        val dz = target.z - position.z
        yaw = Math.toDegrees(atan2(-dx.toDouble(), dz.toDouble())).toFloat()
    }

    private fun moveTowards(target: Vector3, speed: Float, dt: Float, map: BattleMap) {
        val dir = target.sub(position).normalize()
        val nextPos = position.add(dir.mul(speed * dt))
        if (!map.checkCollision(nextPos, radius = 0.8f)) {
            position.x = nextPos.x
            position.z = nextPos.z
        } else {
            // Collision with obstacle, slide around
            position.x += dir.z * speed * dt * 0.7f
            position.z -= dir.x * speed * dt * 0.7f
        }
    }

    private fun strafe(dir: Float, dt: Float, map: BattleMap) {
        val rad = Math.toRadians(yaw.toDouble())
        val sideX = (kotlin.math.cos(rad) * dir).toFloat()
        val sideZ = (kotlin.math.sin(rad) * dir).toFloat()
        val nextPos = Vector3(position.x + sideX * 2.5f * dt, position.y, position.z + sideZ * 2.5f * dt)
        if (!map.checkCollision(nextPos, 0.8f)) {
            position.x = nextPos.x
            position.z = nextPos.z
        }
    }

    private fun pickNewPatrolTarget(map: BattleMap) {
        val angle = (Math.random() * 2 * Math.PI).toFloat()
        val dist = (8f + Math.random() * 20f).toFloat()
        val tx = (position.x + kotlin.math.cos(angle) * dist).coerceIn(-map.mapSize / 2f + 5f, map.mapSize / 2f - 5f)
        val tz = (position.z + kotlin.math.sin(angle) * dist).coerceIn(-map.mapSize / 2f + 5f, map.mapSize / 2f - 5f)
        patrolTarget = Vector3(tx, 0.9f, tz)
        lookAt(patrolTarget)
    }

    fun takeDamage(amount: Float): Boolean {
        if (!isAlive) return false
        var rem = amount
        if (shield > 0f) {
            if (shield >= rem) {
                shield -= rem
                rem = 0f
            } else {
                rem -= shield
                shield = 0f
            }
        }
        if (rem > 0f) {
            health = (health - rem).coerceAtLeast(0f)
        }
        if (health <= 0f) {
            isAlive = false
            state = BotState.DEAD
            return true
        }
        if (state == BotState.PATROL) {
            state = BotState.ATTACK
        }
        return false
    }
}
