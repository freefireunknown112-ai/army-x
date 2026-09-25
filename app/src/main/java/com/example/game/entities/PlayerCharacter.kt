package com.example.game.entities

import com.example.data.models.AmmoType
import com.example.data.models.LootItem
import com.example.data.models.Weapon
import com.example.game.engine.Vector3

class PlayerCharacter(
    val id: String = "player_main",
    var name: String = "Operative",
    var position: Vector3 = Vector3(0f, 0.9f, 20f)
) {
    var velocity = Vector3(0f, 0f, 0f)
    var yaw: Float = 0f
    var pitch: Float = 0f

    var health: Float = 100f
    val maxHealth: Float = 100f

    var shield: Float = 50f
    var maxShield: Float = 100f
    var armorLevel: Int = 1

    var isAlive: Boolean = true
    var isSprinting: Boolean = false
    var isCrouched: Boolean = false
    var isAimingDownSights: Boolean = false
    var isGrounded: Boolean = true
    var isDriving: Boolean = false

    // Weapons inventory slots (up to 3 weapons)
    val weapons = mutableListOf<Weapon>(
        Weapon.createAssaultRifle(),
        Weapon.createPistol()
    )
    var currentWeaponIndex: Int = 0
    val currentWeapon: Weapon?
        get() = if (weapons.isNotEmpty() && currentWeaponIndex in weapons.indices) weapons[currentWeaponIndex] else null

    // Backpack inventory for medical and tactical items
    val inventory = mutableListOf<LootItem>()

    // Healing timer
    var isUsingMedicalItem: Boolean = false
    var medicalItemTimerMs: Long = 0L
    var activeMedicalItem: LootItem? = null

    // Footstep audio timing
    var footstepTimer: Float = 0f

    fun takeDamage(amount: Float): Boolean {
        if (!isAlive) return false
        var remaining = amount

        if (shield > 0f) {
            if (shield >= remaining) {
                shield -= remaining
                remaining = 0f
            } else {
                remaining -= shield
                shield = 0f
            }
        }

        if (remaining > 0f) {
            health = (health - remaining).coerceAtLeast(0f)
        }

        if (health <= 0f) {
            isAlive = false
            return true // Fatal
        }
        return false
    }

    fun heal(hpAmount: Float, shieldAmount: Float = 0f) {
        health = (health + hpAmount).coerceAtMost(maxHealth)
        shield = (shield + shieldAmount).coerceAtMost(maxShield)
    }

    fun startUsingItem(item: LootItem): Boolean {
        if (isUsingMedicalItem) return false
        isUsingMedicalItem = true
        activeMedicalItem = item
        medicalItemTimerMs = item.useDurationMs
        return true
    }

    fun updateMedicalProgress(deltaTimeMs: Long): Boolean {
        if (!isUsingMedicalItem || activeMedicalItem == null) return false
        medicalItemTimerMs -= deltaTimeMs
        if (medicalItemTimerMs <= 0L) {
            val item = activeMedicalItem!!
            heal(item.healAmount, item.shieldAmount)
            // Consume item
            inventory.remove(item)
            isUsingMedicalItem = false
            activeMedicalItem = null
            return true // Finished healing
        }
        return false
    }

    fun cancelMedicalItem() {
        isUsingMedicalItem = false
        activeMedicalItem = null
        medicalItemTimerMs = 0L
    }

    fun switchWeapon(index: Int) {
        if (index in weapons.indices && index != currentWeaponIndex) {
            currentWeaponIndex = index
            cancelMedicalItem()
        }
    }

    fun reloadActiveWeapon(): Boolean {
        val w = currentWeapon ?: return false
        if (w.currentAmmo >= w.magazineSize) return false
        val needed = w.magazineSize - w.currentAmmo
        val toLoad = needed.coerceAtMost(w.reserveAmmo)
        if (toLoad <= 0) return false

        w.currentAmmo += toLoad
        w.reserveAmmo -= toLoad
        return true
    }

    fun equipLoot(item: LootItem): Boolean {
        when {
            item.weaponData != null -> {
                if (weapons.size < 3) {
                    weapons.add(item.weaponData)
                    currentWeaponIndex = weapons.size - 1
                } else {
                    weapons[currentWeaponIndex] = item.weaponData
                }
                return true
            }
            item.ammoType != null -> {
                // Add to matching weapon reserve ammo
                for (w in weapons) {
                    if (w.ammoType == item.ammoType) {
                        w.reserveAmmo += item.quantity
                    }
                }
                return true
            }
            item.armorLevel > armorLevel -> {
                armorLevel = item.armorLevel
                maxShield = item.armorLevel * 50f
                shield = maxShield
                return true
            }
            else -> {
                if (inventory.size < 8) {
                    inventory.add(item)
                    return true
                }
                return false
            }
        }
    }
}
