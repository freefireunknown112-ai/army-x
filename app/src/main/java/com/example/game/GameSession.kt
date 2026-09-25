package com.example.game

import com.example.audio.SoundManager
import com.example.data.models.GameMode
import com.example.data.models.ItemCategory
import com.example.data.models.LootItem
import com.example.data.models.Vehicle
import com.example.data.models.Weapon
import com.example.data.models.WorldLoot
import com.example.game.engine.Vector3
import com.example.game.entities.BotAI
import com.example.game.entities.BotDifficulty
import com.example.game.entities.BulletTracer
import com.example.game.entities.DamageIndicator
import com.example.game.entities.GrenadeEntity
import com.example.game.entities.GrenadeType
import com.example.game.entities.PlayerCharacter
import com.example.game.world.BattleMap
import com.example.game.world.SafeZoneManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.cos
import kotlin.math.sin

data class KillFeedItem(
    val killer: String,
    val victim: String,
    val weaponName: String,
    val isHeadshot: Boolean,
    val isPlayerInvolved: Boolean = false
)

enum class GameState {
    IN_PROGRESS,
    VICTORY,
    DEFEAT
}

class GameSession(
    val soundManager: SoundManager,
    val gameMode: GameMode = GameMode.SOLO_BR
) {
    val map = BattleMap()
    val safeZone = SafeZoneManager()
    val player = PlayerCharacter()

    val bots = mutableListOf<BotAI>()
    val bulletTracers = mutableListOf<BulletTracer>()
    val activeGrenades = mutableListOf<GrenadeEntity>()
    val damageIndicators = mutableListOf<DamageIndicator>()

    private val _killFeed = MutableStateFlow<List<KillFeedItem>>(emptyList())
    val killFeed = _killFeed.asStateFlow()

    private val _aliveCount = MutableStateFlow(20)
    val aliveCount = _aliveCount.asStateFlow()

    private val _playerKills = MutableStateFlow(0)
    val playerKills = _playerKills.asStateFlow()

    private val _gameState = MutableStateFlow(GameState.IN_PROGRESS)
    val gameState = _gameState.asStateFlow()

    private val _nearestLoot = MutableStateFlow<WorldLoot?>(null)
    val nearestLoot = _nearestLoot.asStateFlow()

    private val _nearestVehicle = MutableStateFlow<Vehicle?>(null)
    val nearestVehicle = _nearestVehicle.asStateFlow()

    var totalDamageDealt: Int = 0
    var matchPlacement: Int = 20

    // Input States
    var moveJoystickX: Float = 0f
    var moveJoystickY: Float = 0f
    var isFiring: Boolean = false
    private var fireCooldownTimer: Float = 0f
    private var zoneDamageTimer: Float = 0f

    init {
        spawnInitialBots()
    }

    private fun spawnInitialBots() {
        val botNames = listOf(
            "Viper_99", "ShadowStalker", "KevlarGhost", "OutpostSniper",
            "ApexHunter", "ZeroTactics", "RogueOperative", "DeltaLead",
            "Specter_X", "HavocOperator", "TitanBravo", "SilentEcho",
            "IronClad", "PhantomRecon", "VenomStrike", "StormBreaker",
            "BlazeCommando", "ReaperSquad", "NightCrawler"
        )
        val count = if (gameMode == GameMode.TRAINING) 6 else 19
        _aliveCount.value = count + 1

        val random = java.util.Random(101)
        for (i in 0 until count) {
            val angle = (i.toFloat() / count) * 2f * Math.PI.toFloat()
            val dist = 35f + random.nextFloat() * 45f
            val bx = cos(angle) * dist
            val bz = sin(angle) * dist
            val diff = when {
                i % 4 == 0 -> BotDifficulty.HARD
                i % 2 == 0 -> BotDifficulty.NORMAL
                else -> BotDifficulty.EASY
            }
            val weapon = when (i % 5) {
                0 -> Weapon.createAssaultRifle()
                1 -> Weapon.createSMG()
                2 -> Weapon.createShotgun()
                3 -> Weapon.createSniper()
                else -> Weapon.createPistol()
            }
            bots.add(
                BotAI(
                    id = "bot_$i",
                    name = botNames.getOrElse(i) { "Bot_$i" },
                    position = Vector3(bx, 0.9f, bz),
                    difficulty = diff,
                    weapon = weapon
                )
            )
        }
    }

    fun update(deltaTimeSec: Float) {
        if (_gameState.value != GameState.IN_PROGRESS) return

        fireCooldownTimer -= deltaTimeSec
        zoneDamageTimer += deltaTimeSec

        // 1. Update Safe Zone
        safeZone.update(deltaTimeSec)

        // Safe zone damage tick
        if (zoneDamageTimer >= 1.0f) {
            zoneDamageTimer = 0f
            if (!safeZone.isInsideSafeZone(player.position)) {
                val fatal = player.takeDamage(safeZone.currentPhase.damagePerSec)
                if (fatal) {
                    onPlayerEliminated("The Safe Zone")
                }
            }
            // Damage bots outside safe zone
            for (bot in bots) {
                if (bot.isAlive && !safeZone.isInsideSafeZone(bot.position)) {
                    val fatal = bot.takeDamage(safeZone.currentPhase.damagePerSec)
                    if (fatal) {
                        recordElimination("The Zone", bot.name, "Safe Zone", false)
                    }
                }
            }
        }

        // 2. Update Player Movement
        updatePlayerMovement(deltaTimeSec)

        // 3. Update Player Shooting
        if (isFiring && fireCooldownTimer <= 0f && player.isAlive && !player.isDriving) {
            val weapon = player.currentWeapon
            if (weapon != null && weapon.canFire()) {
                executePlayerShot(weapon)
                fireCooldownTimer = weapon.fireRateMs / 1000f
            } else if (weapon != null && weapon.currentAmmo <= 0) {
                // Auto reload if empty
                player.reloadActiveWeapon()
                soundManager.playSound("reload")
            }
        }

        // 4. Update Bot AI
        for (bot in bots) {
            if (bot.isAlive) {
                bot.update(
                    deltaTimeSec = deltaTimeSec,
                    playerPos = player.position,
                    isPlayerAlive = player.isAlive,
                    map = map,
                    onShoot = { shootingBot, targetPos ->
                        executeBotShot(shootingBot, targetPos)
                    }
                )
            }
        }

        // 5. Update Projectiles
        updateProjectiles(deltaTimeSec)

        // 6. Update Grenades
        val iterator = activeGrenades.iterator()
        while (iterator.hasNext()) {
            val g = iterator.next()
            g.update(deltaTimeSec) { explodedGrenade ->
                detonateGrenade(explodedGrenade)
            }
            if (g.isDetonated && g.type == GrenadeType.FRAG) {
                iterator.remove()
            }
        }

        // 7. Contextual Interactions: Nearest Loot & Nearest Vehicle
        detectContextualInteractions()

        // 8. Update Damage Indicators
        val dIter = damageIndicators.iterator()
        while (dIter.hasNext()) {
            val d = dIter.next()
            d.lifeTime -= deltaTimeSec
            d.alpha = (d.lifeTime / 0.9f).coerceIn(0f, 1f)
            d.worldPos.y += 0.4f * deltaTimeSec
            if (d.lifeTime <= 0f) {
                dIter.remove()
            }
        }

        // 9. Check Victory / Defeat
        checkMatchConditions()
    }

    private fun updatePlayerMovement(dt: Float) {
        if (!player.isAlive) return

        if (player.isDriving) {
            // Player is driving nearest vehicle
            val veh = map.vehicles.firstOrNull { it.driverId == player.id }
            if (veh != null) {
                veh.rotationY += moveJoystickX * 65f * dt
                val accel = -moveJoystickY * 18f
                veh.speed += (accel - veh.speed * 0.5f) * dt
                val rad = Math.toRadians(veh.rotationY.toDouble())
                veh.x -= (sin(rad) * veh.speed * dt).toFloat()
                veh.z += (cos(rad) * veh.speed * dt).toFloat()
                player.position.set(veh.x, veh.y + 0.8f, veh.z)
            }
            return
        }

        // Normal On-Foot Movement
        if (moveJoystickX != 0f || moveJoystickY != 0f) {
            val speed = when {
                player.isSprinting -> 7.8f
                player.isCrouched -> 2.6f
                player.isAimingDownSights -> 3.0f
                else -> 5.2f
            }

            val yawRad = Math.toRadians(player.yaw.toDouble())
            val forwardX = -sin(yawRad).toFloat()
            val forwardZ = cos(yawRad).toFloat()
            val rightX = cos(yawRad).toFloat()
            val rightZ = sin(yawRad).toFloat()

            // Normalized joystick input
            val moveX = (forwardX * -moveJoystickY + rightX * moveJoystickX) * speed * dt
            val moveZ = (forwardZ * -moveJoystickY + rightZ * moveJoystickX) * speed * dt

            val targetPos = Vector3(player.position.x + moveX, player.position.y, player.position.z + moveZ)
            if (!map.checkCollision(targetPos, radius = 0.6f)) {
                player.position.x = targetPos.x
                player.position.z = targetPos.z
            }

            // Footstep audio cadence
            player.footstepTimer += dt
            val interval = if (player.isSprinting) 0.28f else 0.45f
            if (player.footstepTimer >= interval) {
                player.footstepTimer = 0f
                soundManager.playSound("footstep")
            }
        }
    }

    private fun executePlayerShot(weapon: Weapon) {
        weapon.currentAmmo--
        when (weapon.type) {
            com.example.data.models.WeaponType.SNIPER -> soundManager.playSound("shoot_sniper")
            com.example.data.models.WeaponType.SHOTGUN -> soundManager.playSound("shoot_shotgun")
            com.example.data.models.WeaponType.PISTOL -> soundManager.playSound("shoot_pistol")
            else -> soundManager.playSound("shoot_ar")
        }

        val yawRad = Math.toRadians(player.yaw.toDouble())
        val pitchRad = Math.toRadians(player.pitch.toDouble())

        val dirX = (-sin(yawRad) * cos(pitchRad)).toFloat()
        val dirY = -sin(pitchRad).toFloat()
        val dirZ = (cos(yawRad) * cos(pitchRad)).toFloat()

        val shootDir = Vector3(dirX, dirY, dirZ).normalize()
        val spawnPos = Vector3(player.position.x, player.position.y + 1.4f, player.position.z)

        // Hit Detection against Bots
        var hitTarget = false
        for (bot in bots) {
            if (!bot.isAlive) continue
            val dist = spawnPos.distanceTo(bot.position)
            if (dist > weapon.range) continue

            val toBot = bot.position.sub(spawnPos).normalize()
            val dot = shootDir.dot(toBot)
            if (dot > 0.965f) { // Within crosshair cone
                hitTarget = true
                val isHeadshot = dot > 0.995f
                val dmg = if (isHeadshot) weapon.damage * weapon.headshotMultiplier else weapon.damage
                val fatal = bot.takeDamage(dmg)

                totalDamageDealt += dmg.toInt()
                damageIndicators.add(DamageIndicator(dmg.toInt(), isHeadshot, bot.position.copy()))

                if (isHeadshot) {
                    soundManager.playSound("headshot")
                } else {
                    soundManager.playSound("hit")
                }

                if (fatal) {
                    _playerKills.value++
                    recordElimination(player.name, bot.name, weapon.name, isHeadshot)
                    // Spawn drop loot from eliminated bot
                    map.initialLoot.add(
                        WorldLoot(
                            id = "drop_${bot.id}",
                            item = LootItem.medkit(),
                            x = bot.position.x,
                            y = 0.5f,
                            z = bot.position.z
                        )
                    )
                }
                break
            }
        }

        // Spawn visual bullet tracer
        bulletTracers.add(
            BulletTracer(
                startPos = spawnPos.copy(),
                endPos = spawnPos.add(shootDir.mul(weapon.range)),
                currentPos = spawnPos.copy(),
                direction = shootDir,
                speed = weapon.bulletSpeed,
                damage = weapon.damage,
                shooterId = player.id,
                isHeadshot = hitTarget
            )
        )
    }

    private fun executeBotShot(bot: BotAI, targetPos: Vector3) {
        val spawnPos = Vector3(bot.position.x, bot.position.y + 1.4f, bot.position.z)
        val dir = targetPos.sub(spawnPos).normalize()

        soundManager.playSound("shoot_ar")

        // Check if hitting player
        val distToPlayer = spawnPos.distanceTo(player.position)
        if (distToPlayer <= bot.weapon.range) {
            val toPlayer = player.position.sub(spawnPos).normalize()
            if (dir.dot(toPlayer) > 0.96f && player.isAlive) {
                val dmg = bot.weapon.damage * 0.7f // Scaled for fair mobile battle
                val fatal = player.takeDamage(dmg)
                soundManager.playSound("hit")
                if (fatal) {
                    onPlayerEliminated(bot.name)
                }
            }
        }

        bulletTracers.add(
            BulletTracer(
                startPos = spawnPos.copy(),
                endPos = spawnPos.add(dir.mul(bot.weapon.range)),
                currentPos = spawnPos.copy(),
                direction = dir,
                speed = bot.weapon.bulletSpeed,
                damage = bot.weapon.damage,
                shooterId = bot.id,
                isHeadshot = false
            )
        )
    }

    fun throwGrenade(type: GrenadeType) {
        val yawRad = Math.toRadians(player.yaw.toDouble())
        val pitchRad = Math.toRadians(player.pitch.toDouble())
        val forwardX = (-sin(yawRad) * cos(pitchRad)).toFloat()
        val forwardY = -sin(pitchRad).toFloat() + 0.35f // Upward toss angle
        val forwardZ = (cos(yawRad) * cos(pitchRad)).toFloat()

        val vel = Vector3(forwardX, forwardY, forwardZ).normalize().mul(18f)
        val pos = Vector3(player.position.x, player.position.y + 1.5f, player.position.z)

        activeGrenades.add(GrenadeEntity("grenade_${System.currentTimeMillis()}", type, player.id, pos, vel))
    }

    private fun detonateGrenade(g: GrenadeEntity) {
        soundManager.playSound("explosion")
        if (g.type == GrenadeType.FRAG) {
            val blastRadius = 9.0f
            // Damage player if in radius
            val pDist = g.position.distanceTo(player.position)
            if (pDist <= blastRadius && player.isAlive) {
                val dmg = (1.0f - pDist / blastRadius) * 120f
                val fatal = player.takeDamage(dmg)
                if (fatal) onPlayerEliminated("Frag Grenade")
            }
            // Damage bots in radius
            for (bot in bots) {
                if (!bot.isAlive) continue
                val bDist = g.position.distanceTo(bot.position)
                if (bDist <= blastRadius) {
                    val dmg = (1.0f - bDist / blastRadius) * 120f
                    val fatal = bot.takeDamage(dmg)
                    if (fatal) {
                        if (g.throwerId == player.id) _playerKills.value++
                        recordElimination("Frag Blast", bot.name, "M67 Frag", false)
                    }
                }
            }
        }
    }

    private fun updateProjectiles(dt: Float) {
        val iter = bulletTracers.iterator()
        while (iter.hasNext()) {
            val b = iter.next()
            b.update(dt)
            if (!b.isActive) {
                iter.remove()
            }
        }
    }

    private fun detectContextualInteractions() {
        var closestLoot: WorldLoot? = null
        var minLootDist = 3.2f
        for (loot in map.initialLoot) {
            if (!loot.isSpawned) continue
            val dist = player.position.distanceTo2D(loot.x, loot.z)
            if (dist < minLootDist) {
                minLootDist = dist
                closestLoot = loot
            }
        }
        _nearestLoot.value = closestLoot

        var closestVeh: Vehicle? = null
        var minVehDist = 4.5f
        for (v in map.vehicles) {
            val dist = player.position.distanceTo2D(v.x, v.z)
            if (dist < minVehDist) {
                minVehDist = dist
                closestVeh = v
            }
        }
        _nearestVehicle.value = closestVeh
    }

    fun pickUpNearestLoot() {
        val loot = _nearestLoot.value ?: return
        val success = player.equipLoot(loot.item)
        if (success) {
            loot.isSpawned = false
            _nearestLoot.value = null
            soundManager.playSound("click")
        }
    }

    fun toggleVehicleEntry() {
        if (player.isDriving) {
            // Exit vehicle
            val veh = map.vehicles.firstOrNull { it.driverId == player.id }
            if (veh != null) {
                veh.isOccupied = false
                veh.driverId = null
                veh.speed = 0f
            }
            player.isDriving = false
            player.position.x += 2.0f
        } else {
            // Enter vehicle
            val veh = _nearestVehicle.value ?: return
            if (!veh.isOccupied) {
                veh.isOccupied = true
                veh.driverId = player.id
                player.isDriving = true
            }
        }
    }

    private fun recordElimination(killer: String, victim: String, weapon: String, isHeadshot: Boolean) {
        val isPlayer = killer == player.name || victim == player.name
        val item = KillFeedItem(killer, victim, weapon, isHeadshot, isPlayer)
        val feed = _killFeed.value.toMutableList()
        feed.add(0, item)
        if (feed.size > 5) feed.removeAt(feed.size - 1)
        _killFeed.value = feed

        val remaining = bots.count { it.isAlive } + (if (player.isAlive) 1 else 0)
        _aliveCount.value = remaining
    }

    private fun onPlayerEliminated(cause: String) {
        player.isAlive = false
        matchPlacement = _aliveCount.value
        _gameState.value = GameState.DEFEAT
    }

    private fun checkMatchConditions() {
        val aliveBots = bots.count { it.isAlive }
        if (aliveBots == 0 && player.isAlive) {
            matchPlacement = 1
            _gameState.value = GameState.VICTORY
            soundManager.playSound("victory")
        } else if (!player.isAlive && _gameState.value == GameState.IN_PROGRESS) {
            matchPlacement = aliveBots + 1
            _gameState.value = GameState.DEFEAT
        }
    }
}
